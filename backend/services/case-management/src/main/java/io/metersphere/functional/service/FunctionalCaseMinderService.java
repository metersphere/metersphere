package io.metersphere.functional.service;

import io.metersphere.functional.constants.FunctionalCaseReviewStatus;
import io.metersphere.functional.constants.FunctionalCaseTypeConstants;
import io.metersphere.functional.domain.*;
import io.metersphere.functional.dto.*;
import io.metersphere.functional.mapper.*;
import io.metersphere.functional.request.*;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ExtBaseProjectVersionMapper;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.service.ProjectTemplateService;
import io.metersphere.project.utils.NodeSortUtils;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.CustomField;
import io.metersphere.system.domain.CustomFieldExample;
import io.metersphere.system.domain.User;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.sdk.TemplateCustomFieldDTO;
import io.metersphere.system.dto.sdk.TemplateDTO;
import io.metersphere.system.dto.sdk.enums.MoveTypeEnum;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import io.metersphere.system.mapper.CustomFieldMapper;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.service.CommonNoticeSendService;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.jetbrains.annotations.NotNull;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 功能用例脑图
 *
 * @date : 2023-5-17
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class FunctionalCaseMinderService {

    protected static final long LIMIT_POS = NodeSortUtils.DEFAULT_NODE_INTERVAL_POS;


    @Resource
    private FunctionalCaseMapper functionalCaseMapper;

    @Resource
    private ExtFunctionalCaseMapper extFunctionalCaseMapper;

    @Resource
    private FunctionalCaseBlobMapper functionalCaseBlobMapper;

    @Resource
    private FunctionalCaseModuleMapper functionalCaseModuleMapper;

    @Resource
    private CustomFieldMapper customFieldMapper;

    @Resource
    private FunctionalCaseCustomFieldMapper functionalCaseCustomFieldMapper;

    @Resource
    private FunctionalCaseTrashService functionalCaseTrashService;

    @Resource
    private FunctionalCaseService functionalCaseService;

    @Resource
    private FunctionalCaseModuleService functionalCaseModuleService;

    @Resource
    private ExtBaseProjectVersionMapper extBaseProjectVersionMapper;

    @Resource
    private CommonNoticeSendService commonNoticeSendService;

    @Resource
    private OperationLogService operationLogService;

    @Resource
    private UserMapper userMapper;

    @Resource
    private ProjectMapper projectMapper;

    @Resource
    SqlSessionFactory sqlSessionFactory;

    @Resource
    private ExtFunctionalCaseModuleMapper extFunctionalCaseModuleMapper;

    @Resource
    private FunctionalCaseNoticeService functionalCaseNoticeService;

    @Resource
    private MindAdditionalNodeMapper mindAdditionalNodeMapper;

    @Resource
    private ProjectTemplateService projectTemplateService;
    @Resource
    private FunctionalCaseModuleLogService functionalCaseModuleLogService;


    /**
     * 功能用例-脑图用例列表查询
     *
     * @param deleted 用例是否删除
     * @return FunctionalMinderTreeDTO
     */
    public List<FunctionalMinderTreeDTO> getMindFunctionalCase(FunctionalCaseMindRequest request, boolean deleted) {
        List<FunctionalMinderTreeDTO> list = new ArrayList<>();
        //查出当前模块下的所有用例
        if (StringUtils.isBlank(request.getModuleId())) {
            return new ArrayList<>();
        }
        List<FunctionalCaseMindDTO> functionalCaseMindDTOList = extFunctionalCaseMapper.getMinderCaseList(request, deleted);
        List<String> fieldIds = getFieldIds(request.getProjectId());
        List<FunctionalCaseCustomField> caseCustomFieldList = extFunctionalCaseMapper.getCaseCustomFieldList(request, deleted, fieldIds);

        Map<String, String> priorityMap = caseCustomFieldList.stream().collect(Collectors.toMap(FunctionalCaseCustomField::getCaseId, FunctionalCaseCustomField::getValue));

        //构造父子级数据
        buildList(functionalCaseMindDTOList, list, priorityMap, "FUNCTIONAL");
        return list;
    }

    @NotNull
    private List<String> getFieldIds(String projectId) {
        TemplateDTO defaultTemplateDTO = projectTemplateService.getDefaultTemplateDTO(projectId, TemplateScene.FUNCTIONAL.toString());
        List<TemplateCustomFieldDTO> customFields = defaultTemplateDTO.getCustomFields();
        return customFields.stream().map(TemplateCustomFieldDTO::getFieldId).toList();
    }

    private void buildList(List<FunctionalCaseMindDTO> functionalCaseMindDTOList, List<FunctionalMinderTreeDTO> list, Map<String, String> priorityMap, String sourceType) {
        //构造父子级数据
        for (FunctionalCaseMindDTO functionalCaseMindDTO : functionalCaseMindDTOList) {
            FunctionalMinderTreeDTO root = new FunctionalMinderTreeDTO();
            FunctionalMinderTreeNodeDTO rootData = new FunctionalMinderTreeNodeDTO();
            rootData.setId(functionalCaseMindDTO.getId());
            rootData.setPos(functionalCaseMindDTO.getPos());
            rootData.setText(functionalCaseMindDTO.getName());
            rootData.setCaseId(functionalCaseMindDTO.getCaseId());
            rootData.setPriority(StringUtils.isNotBlank(priorityMap.get(functionalCaseMindDTO.getCaseId())) ? Integer.parseInt(priorityMap.get(functionalCaseMindDTO.getCaseId()).substring(1)) + 1 : 1);
            rootData.setStatus(functionalCaseMindDTO.getReviewStatus());
            if (StringUtils.equalsIgnoreCase(sourceType, "FUNCTIONAL")) {
                rootData.setResource(List.of(Translator.get("minder_extra_node.case")));
            }
            List<FunctionalMinderTreeDTO> children = buildChildren(functionalCaseMindDTO, sourceType);
            root.setChildren(children);
            root.setData(rootData);
            list.add(root);
        }
    }

    private List<FunctionalMinderTreeDTO> buildChildren(FunctionalCaseMindDTO functionalCaseMindDTO, String sourceType) {
        List<FunctionalMinderTreeDTO> children = new ArrayList<>();
        if (functionalCaseMindDTO.getPrerequisite() != null) {
            String prerequisiteText = new String(functionalCaseMindDTO.getPrerequisite(), StandardCharsets.UTF_8);
            if (StringUtils.isNotBlank(prerequisiteText)) {
                FunctionalMinderTreeDTO prerequisiteFunctionalMinderTreeDTO = getFunctionalMinderTreeDTO(prerequisiteText, Translator.get("minder_extra_node.prerequisite"), 0L);
                children.add(prerequisiteFunctionalMinderTreeDTO);
            }
        }

        if (StringUtils.equalsIgnoreCase(functionalCaseMindDTO.getCaseEditType(), FunctionalCaseTypeConstants.CaseEditType.TEXT.name()) && functionalCaseMindDTO.getTextDescription() != null) {
            String textDescription = new String(functionalCaseMindDTO.getTextDescription(), StandardCharsets.UTF_8);
            FunctionalMinderTreeDTO stepFunctionalMinderTreeDTO = getFunctionalMinderTreeDTO(textDescription, Translator.get("minder_extra_node.text_description"), 1L);
            String expectedResultText = "";
            if (functionalCaseMindDTO.getExpectedResult() != null) {
                expectedResultText = new String(functionalCaseMindDTO.getExpectedResult(), StandardCharsets.UTF_8);
                FunctionalMinderTreeDTO expectedResultFunctionalMinderTreeDTO = getFunctionalMinderTreeDTO(expectedResultText, Translator.get("minder_extra_node.text_expected_result"), 1L);
                stepFunctionalMinderTreeDTO.getChildren().add(expectedResultFunctionalMinderTreeDTO);
            } else {
                FunctionalMinderTreeDTO expectedResultFunctionalMinderTreeDTO = getFunctionalMinderTreeDTO(expectedResultText, Translator.get("minder_extra_node.text_expected_result"), 1L);
                stepFunctionalMinderTreeDTO.getChildren().add(expectedResultFunctionalMinderTreeDTO);
            }
            if (StringUtils.isNotBlank(textDescription) || StringUtils.isNotBlank(expectedResultText)) {
                children.add(stepFunctionalMinderTreeDTO);
            }
        }

        int i = 1;
        if (StringUtils.equalsIgnoreCase(functionalCaseMindDTO.getCaseEditType(), FunctionalCaseTypeConstants.CaseEditType.STEP.name()) && functionalCaseMindDTO.getSteps() != null) {
            String stepText = new String(functionalCaseMindDTO.getSteps(), StandardCharsets.UTF_8);
            if (StringUtils.isNotBlank(stepText)) {
                List<FunctionalCaseStepDTO> functionalCaseStepDTOS = JSON.parseArray(stepText, FunctionalCaseStepDTO.class);
                if (StringUtils.equalsIgnoreCase(sourceType, "TEST_PLAN")) {
                    compareStep(functionalCaseMindDTO.getExecuteSteps(), functionalCaseStepDTOS);
                }
                for (FunctionalCaseStepDTO functionalCaseStepDTO : functionalCaseStepDTOS) {
                    i = i + 1;
                    String desc = functionalCaseStepDTO.getDesc();
                    FunctionalMinderTreeDTO stepFunctionalMinderTreeDTO = getFunctionalMinderTreeDTO(desc, Translator.get("minder_extra_node.steps"), Long.valueOf(functionalCaseStepDTO.getNum()));
                    stepFunctionalMinderTreeDTO.getData().setId(functionalCaseStepDTO.getId());
                    FunctionalMinderTreeDTO expectedResultFunctionalMinderTreeDTO;
                    String result = "";
                    if (functionalCaseMindDTO.getExpectedResult() != null) {
                        result = functionalCaseStepDTO.getResult();
                        expectedResultFunctionalMinderTreeDTO = getFunctionalMinderTreeDTO(result, Translator.get("minder_extra_node.steps_expected_result"), Long.valueOf(functionalCaseStepDTO.getNum()));
                    } else {
                        expectedResultFunctionalMinderTreeDTO = getFunctionalMinderTreeDTO(result, Translator.get("minder_extra_node.steps_expected_result"), Long.valueOf(functionalCaseStepDTO.getNum()));
                    }
                    if (StringUtils.equalsIgnoreCase(sourceType, "TEST_PLAN")) {
                        Map<String, String> statusMap = new HashMap<>();
                        statusMap.put(ResultStatus.SUCCESS.name(), Translator.get("case.minder.status.success"));
                        statusMap.put(ResultStatus.ERROR.name(), Translator.get("case.minder.status.error"));
                        statusMap.put(ResultStatus.BLOCKED.name(), Translator.get("case.minder.status.blocked"));
                        String actualResult = "";
                        FunctionalMinderTreeDTO actualResultFunctionalMinderTreeDTO;
                        if (StringUtils.isNotBlank(functionalCaseStepDTO.getActualResult())) {
                            actualResult = functionalCaseStepDTO.getActualResult();
                            actualResultFunctionalMinderTreeDTO = getFunctionalMinderTreeDTO(actualResult, Translator.get("minder_extra_node.steps_actual_result"), Long.valueOf(functionalCaseStepDTO.getNum()));
                        } else {
                            actualResultFunctionalMinderTreeDTO = getFunctionalMinderTreeDTO(actualResult, Translator.get("minder_extra_node.steps_actual_result"), Long.valueOf(functionalCaseStepDTO.getNum()));
                        }
                        expectedResultFunctionalMinderTreeDTO.getChildren().add(actualResultFunctionalMinderTreeDTO);
                        if (StringUtils.isNotBlank(functionalCaseStepDTO.getExecuteResult())) {
                            List<String> resource = stepFunctionalMinderTreeDTO.getData().getResource();
                            List<String> list = new ArrayList<>(resource);
                            list.add(0, statusMap.get(functionalCaseStepDTO.getExecuteResult()));
                            stepFunctionalMinderTreeDTO.getData().setResource(list);
                        }
                    }
                    stepFunctionalMinderTreeDTO.getChildren().add(expectedResultFunctionalMinderTreeDTO);
                    if (StringUtils.isNotBlank(desc) || StringUtils.isNotBlank(result)) {
                        children.add(stepFunctionalMinderTreeDTO);
                    }
                }
            }
        }

        if (functionalCaseMindDTO.getDescription() != null) {
            String descriptionText = new String(functionalCaseMindDTO.getDescription(), StandardCharsets.UTF_8);
            if (StringUtils.isNotBlank(descriptionText)) {
                FunctionalMinderTreeDTO descriptionFunctionalMinderTreeDTO = getFunctionalMinderTreeDTO(descriptionText, Translator.get("minder_extra_node.description"), (long) (i + 1));
                children.add(descriptionFunctionalMinderTreeDTO);
            }
        }

        if (StringUtils.equalsIgnoreCase(sourceType, "TEST_PLAN")) {
            String contentText = StringUtils.EMPTY;
            if (functionalCaseMindDTO.getContent() != null) {
                contentText = new String(functionalCaseMindDTO.getContent(), StandardCharsets.UTF_8);
            }
            if (StringUtils.isNotBlank(contentText)) {
                FunctionalMinderTreeDTO contentFunctionalMinderTreeDTO = getFunctionalMinderTreeDTO(contentText, Translator.get("minder_extra_node.steps_actual_result"), (long) (i + 1));
                children.add(contentFunctionalMinderTreeDTO);
            }
        }

        return children;
    }

    @NotNull
    private static FunctionalMinderTreeDTO getFunctionalMinderTreeDTO(String text, String resource, Long pos) {
        FunctionalMinderTreeDTO functionalMinderTreeDTO = new FunctionalMinderTreeDTO();
        FunctionalMinderTreeNodeDTO rootData = new FunctionalMinderTreeNodeDTO();
        rootData.setText(text);
        rootData.setPos(pos);
        //最子节点默认收起
        rootData.setExpandState("collapse");
        rootData.setResource(List.of(resource));
        functionalMinderTreeDTO.setChildren(new ArrayList<>());
        functionalMinderTreeDTO.setData(rootData);
        return functionalMinderTreeDTO;
    }

    public void editFunctionalCaseBatch(FunctionalCaseMinderEditRequest request, String userId) {
        //处理删除的模块和用例和空白节点
        User user = userMapper.selectByPrimaryKey(userId);
        FunctionalMinderUpdateDTO functionalMinderUpdateDTO = new FunctionalMinderUpdateDTO(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        deleteResource(request, user, functionalMinderUpdateDTO);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        FunctionalCaseModuleMapper moduleMapper = sqlSession.getMapper(FunctionalCaseModuleMapper.class);
        MindAdditionalNodeMapper additionalNodeMapper = sqlSession.getMapper(MindAdditionalNodeMapper.class);

        //处理模块
        Map<String, String> sourceIdAndInsertModuleIdMap = dealModule(request, userId, moduleMapper, functionalMinderUpdateDTO);
        List<String> needToTurnModuleIds = sourceIdAndInsertModuleIdMap.keySet().stream().distinct().toList();
        //删除用例
        if (CollectionUtils.isNotEmpty(needToTurnModuleIds)) {
            List<LogDTO> logDTOS = deleteLog(needToTurnModuleIds, userId);
            functionalMinderUpdateDTO.getDeleteLogDTOS().addAll(logDTOS);
            functionalCaseTrashService.deleteByIds(request.getProjectId(), needToTurnModuleIds, userId);
            functionalCaseNoticeService.batchSendNotice(request.getProjectId(), needToTurnModuleIds, user, NoticeConstants.Event.DELETE);
        }

        //处理用例
        Map<String, String> sourceIdAndInsertCaseIdMap = dealCase(request, userId, sqlSession, sourceIdAndInsertModuleIdMap, functionalMinderUpdateDTO);
        List<String> needToTurnCaseIds = new ArrayList<>(sourceIdAndInsertCaseIdMap.keySet().stream().distinct().toList());
        //删除模块
        if (CollectionUtils.isNotEmpty(needToTurnCaseIds)) {
            FunctionalCaseModuleExample functionalCaseModuleExample = new FunctionalCaseModuleExample();
            functionalCaseModuleExample.createCriteria().andIdIn(needToTurnCaseIds);
            moduleMapper.deleteByExample(functionalCaseModuleExample);
        }
        //删除已转为模块或用例的空白节点
        needToTurnCaseIds.addAll(needToTurnModuleIds);
        if (CollectionUtils.isNotEmpty(needToTurnCaseIds)) {
            dealMindAdditionalMode(needToTurnCaseIds, additionalNodeMapper);
        }
        //处理空白节点
        Map<String, String> sourceIdAndInsertTextIdMap = dealAdditionalNode(request, userId, additionalNodeMapper, sourceIdAndInsertModuleIdMap, functionalMinderUpdateDTO);

        //替换targetId
        Map<String, String> sourceIdAndTargetIdMap = functionalMinderUpdateDTO.getSourceIdAndTargetIdsMap();
        //新的target
        Map<String, String> sourceIdAndInsertTargetIdMap = new HashMap<>();
        sourceIdAndTargetIdMap.forEach((sourceId, targetId) -> {
            if (StringUtils.isNotBlank(sourceIdAndInsertModuleIdMap.get(targetId))) {
                sourceIdAndInsertTargetIdMap.put(sourceId, sourceIdAndInsertModuleIdMap.get(targetId));
            } else if (StringUtils.isNotBlank(sourceIdAndInsertCaseIdMap.get(targetId))) {
                sourceIdAndInsertTargetIdMap.put(sourceId, sourceIdAndInsertCaseIdMap.get(targetId));

            } else if (StringUtils.isNotBlank(sourceIdAndInsertTextIdMap.get(targetId))) {
                sourceIdAndInsertTargetIdMap.put(sourceId, sourceIdAndInsertTextIdMap.get(targetId));
            } else {
                sourceIdAndInsertTargetIdMap.put(sourceId, targetId);
            }
        });
        //排序
        List<String> targetIds = sourceIdAndInsertTargetIdMap.values().stream().distinct().toList();
        if (CollectionUtils.isNotEmpty(targetIds)) {
            FunctionalCaseModuleExample functionalCaseModuleExample = new FunctionalCaseModuleExample();
            functionalCaseModuleExample.createCriteria().andIdIn(targetIds);
            List<FunctionalCaseModule> targetModuleIds = moduleMapper.selectByExample(functionalCaseModuleExample);
            Map<String, String> targetModuleMap = new HashMap<>();
            List<String> targetModuleParentIds = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(targetModuleIds)) {
                targetModuleMap = targetModuleIds.stream().collect(Collectors.toMap(FunctionalCaseModule::getId, FunctionalCaseModule::getParentId));
                targetModuleParentIds = targetModuleIds.stream().map(FunctionalCaseModule::getParentId).distinct().toList();
            }
            List<FunctionalCaseModule> allChildrenInDB = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(targetModuleParentIds)) {
                functionalCaseModuleExample = new FunctionalCaseModuleExample();
                functionalCaseModuleExample.createCriteria().andParentIdIn(targetModuleParentIds);
                allChildrenInDB = moduleMapper.selectByExample(functionalCaseModuleExample);
            }

            FunctionalCaseExample functionalCaseExample = new FunctionalCaseExample();
            functionalCaseExample.createCriteria().andIdIn(targetIds);
            List<FunctionalCase> functionalCases = functionalCaseMapper.selectByExample(functionalCaseExample);

            Map<String, String> targetCaseMap = new HashMap<>();
            List<String> targetCaseParentIds = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(functionalCases)) {
                targetCaseMap = functionalCases.stream().collect(Collectors.toMap(FunctionalCase::getId, FunctionalCase::getModuleId));
                targetCaseParentIds = new ArrayList<>(functionalCases.stream().map(FunctionalCase::getModuleId).toList());
            }

            targetCaseParentIds.addAll(targetModuleParentIds);
            List<String> targetCaseParentIdsNoRepeat = targetCaseParentIds.stream().distinct().toList();
            List<FunctionalCase> allChildrenCaseInDB = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(targetCaseParentIdsNoRepeat)) {
                functionalCaseExample = new FunctionalCaseExample();
                functionalCaseExample.createCriteria().andModuleIdIn(targetCaseParentIdsNoRepeat).andProjectIdEqualTo(request.getProjectId());
                allChildrenCaseInDB = functionalCaseMapper.selectByExample(functionalCaseExample);
            }

            List<String> finalTargetModuleParentIds = targetModuleParentIds;
            List<String> caseModuleIds = targetCaseParentIds.stream().filter(t -> !finalTargetModuleParentIds.contains(t)).distinct().toList();
            List<FunctionalCaseModule> allChildrenByCaseInDB = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(caseModuleIds)) {
                functionalCaseModuleExample = new FunctionalCaseModuleExample();
                functionalCaseModuleExample.createCriteria().andParentIdIn(caseModuleIds).andProjectIdEqualTo(request.getProjectId());
                ;
                allChildrenByCaseInDB = moduleMapper.selectByExample(functionalCaseModuleExample);
            }
            allChildrenInDB.addAll(allChildrenByCaseInDB);


            MindAdditionalNodeExample mindAdditionalNodeExample = new MindAdditionalNodeExample();
            mindAdditionalNodeExample.createCriteria().andIdIn(targetIds).andProjectIdEqualTo(request.getProjectId());
            ;
            List<MindAdditionalNode> mindAdditionalNodes = additionalNodeMapper.selectByExample(mindAdditionalNodeExample);
            Map<String, String> targetTextMap = new HashMap<>();
            List<String> targetTextParentIds = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(mindAdditionalNodes)) {
                targetTextMap = mindAdditionalNodes.stream().collect(Collectors.toMap(MindAdditionalNode::getId, MindAdditionalNode::getParentId));
                //空白节点的父亲可能是空白节点，也可能是模块
                targetTextParentIds = new ArrayList<>(mindAdditionalNodes.stream().map(MindAdditionalNode::getParentId).toList());
            }

            targetTextParentIds.addAll(targetModuleParentIds);
            targetTextParentIds.addAll(targetCaseParentIds);
            List<String> targetTextParentNoRepeatIds = targetTextParentIds.stream().distinct().toList();
            List<MindAdditionalNode> allChildrenTextInDB;
            Map<String, List<MindAdditionalNode>> parentChildrenTextMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(targetTextParentNoRepeatIds)) {
                mindAdditionalNodeExample = new MindAdditionalNodeExample();
                mindAdditionalNodeExample.createCriteria().andParentIdIn(targetTextParentNoRepeatIds);
                allChildrenTextInDB = additionalNodeMapper.selectByExample(mindAdditionalNodeExample);
                parentChildrenTextMap = allChildrenTextInDB.stream().collect(Collectors.groupingBy(MindAdditionalNode::getParentId));
            }

            List<String> finalTargetModuleParentIds1 = targetModuleParentIds;
            List<String> textModuleIds = targetTextParentIds.stream().filter(t -> !finalTargetModuleParentIds1.contains(t)).distinct().toList();

            if (CollectionUtils.isNotEmpty(textModuleIds)) {
                functionalCaseModuleExample = new FunctionalCaseModuleExample();
                functionalCaseModuleExample.createCriteria().andParentIdIn(textModuleIds);
                List<FunctionalCaseModule> allChildrenByTextInDB = moduleMapper.selectByExample(functionalCaseModuleExample);
                allChildrenInDB.addAll(allChildrenByTextInDB);
            }
            List<String> textModuleFilterCaseIds = targetTextParentIds.stream().filter(t -> !targetCaseParentIdsNoRepeat.contains(t)).distinct().toList();

            if (CollectionUtils.isNotEmpty(textModuleFilterCaseIds)) {
                functionalCaseExample = new FunctionalCaseExample();
                functionalCaseExample.createCriteria().andModuleIdIn(textModuleFilterCaseIds).andProjectIdEqualTo(request.getProjectId());
                ;
                List<FunctionalCase> allChildrenCaseByTextInDB = functionalCaseMapper.selectByExample(functionalCaseExample);
                allChildrenCaseInDB.addAll(allChildrenCaseByTextInDB);
            }
            Map<String, List<FunctionalCaseModule>> parentChildrenMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(allChildrenInDB)) {
                parentChildrenMap = allChildrenInDB.stream().collect(Collectors.groupingBy(FunctionalCaseModule::getParentId));

            }
            Map<String, List<FunctionalCase>> parentChildrenCaseMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(allChildrenCaseInDB)) {
                parentChildrenCaseMap = allChildrenCaseInDB.stream().collect(Collectors.groupingBy(FunctionalCase::getModuleId));
            }

            //更新模块顺序
            if (CollectionUtils.isNotEmpty(request.getUpdateModuleList())) {
                for (FunctionalCaseModuleEditRequest functionalCaseModuleEditRequest : request.getUpdateModuleList()) {
                    //找出替换后的target
                    String targetId = sourceIdAndInsertTargetIdMap.get(functionalCaseModuleEditRequest.getId());
                    //先看看相邻点是否是模块,不是模块可能是用例，否则是空白节点
                    String moduleMapKey = getModuleMapKey(targetModuleMap, targetId, targetCaseMap, targetTextMap);
                    String moduleId = sourceIdAndInsertModuleIdMap.get(functionalCaseModuleEditRequest.getId());
                    if (StringUtils.isBlank(moduleId)) {
                        moduleId = functionalCaseModuleEditRequest.getId();
                    }
                    List<FunctionalCaseModule> functionalCaseModules = parentChildrenMap.get(moduleMapKey);
                    if (CollectionUtils.isNotEmpty(functionalCaseModules)) {
                        List<FunctionalCaseModule> finallyModules = sortMindCases(functionalCaseModuleEditRequest.getMoveMode(), targetId, moduleId, functionalCaseModules, FunctionalCaseModule::getId, FunctionalCaseModule::getPos);
                        for (int i = 0; i < finallyModules.size(); i++) {
                            finallyModules.get(i).setPos(LIMIT_POS * i);
                            moduleMapper.updateByPrimaryKey(finallyModules.get(i));
                        }
                    }
                }
            }

            //更新用例顺序
            if (CollectionUtils.isNotEmpty(request.getUpdateCaseList())) {
                for (FunctionalCaseChangeRequest caseChangeRequest : request.getUpdateCaseList()) {
                    String caseId = sourceIdAndInsertCaseIdMap.get(caseChangeRequest.getId());
                    if (StringUtils.isBlank(caseId)) {
                        caseId = caseChangeRequest.getId();
                    }
                    //找出替换后的target
                    String targetId = sourceIdAndInsertTargetIdMap.get(caseChangeRequest.getId());
                    //先看看相邻点是否是模块,不是模块可能是用例，否则是空白节点
                    String moduleMapKey = getModuleMapKey(targetModuleMap, targetId, targetCaseMap, targetTextMap);
                    List<FunctionalCase> functionalCasesList = parentChildrenCaseMap.get(moduleMapKey);
                    if (CollectionUtils.isNotEmpty(functionalCasesList)) {
                        List<FunctionalCase> finallyCases = sortMindCases(caseChangeRequest.getMoveMode(), targetId, caseId, functionalCasesList, FunctionalCase::getId, FunctionalCase::getPos);
                        FunctionalCaseMapper caseMapper = sqlSession.getMapper(FunctionalCaseMapper.class);
                        for (int i = 0; i < finallyCases.size(); i++) {
                            finallyCases.get(i).setPos(LIMIT_POS * i);
                            caseMapper.updateByPrimaryKey(finallyCases.get(i));
                        }
                    }
                }
            }

            //更新空白节点顺序
            if (CollectionUtils.isNotEmpty(request.getAdditionalNodeList())) {
                for (MindAdditionalNodeRequest mindAdditionalNodeRequest : request.getAdditionalNodeList()) {

                    String textId = sourceIdAndInsertTextIdMap.get(mindAdditionalNodeRequest.getId());
                    if (StringUtils.isBlank(textId)) {
                        textId = mindAdditionalNodeRequest.getId();
                    }
                    //找出替换后的target
                    String targetId = sourceIdAndInsertTargetIdMap.get(mindAdditionalNodeRequest.getId());
                    //先看看相邻点是否是模块,不是模块可能是用例，否则是空白节点
                    String moduleMapKey = getModuleMapKey(targetModuleMap, targetId, targetCaseMap, targetTextMap);
                    List<MindAdditionalNode> mindAdditionalNodeList = parentChildrenTextMap.get(moduleMapKey);
                    if (CollectionUtils.isNotEmpty(mindAdditionalNodeList)) {
                        List<MindAdditionalNode> finallyNode = sortMindCases(mindAdditionalNodeRequest.getMoveMode(), targetId, textId, mindAdditionalNodeList, MindAdditionalNode::getId, MindAdditionalNode::getPos);
                        for (int i = 0; i < finallyNode.size(); i++) {
                            finallyNode.get(i).setPos(LIMIT_POS * i);
                            additionalNodeMapper.updateByPrimaryKey(finallyNode.get(i));
                        }
                    }
                }
            }
        }
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);

        dealLogAndNotice(request, userId, functionalMinderUpdateDTO);
    }

    private List<LogDTO> deleteLog(List<String> needToTurnModuleIds, String userId) {
        List<LogDTO> dtoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(needToTurnModuleIds)) {
            List<FunctionalCase> functionalCases = extFunctionalCaseMapper.getLogInfo(needToTurnModuleIds, false);
            functionalCases.forEach(functionalCase -> {
                LogDTO dto = new LogDTO(
                        functionalCase.getProjectId(),
                        null,
                        functionalCase.getId(),
                        userId,
                        OperationLogType.DELETE.name(),
                        OperationLogModule.CASE_MANAGEMENT_CASE_CASE,
                        functionalCase.getName());

                dto.setPath("/functional/mind/case/edit");
                dto.setMethod(HttpMethodConstants.POST.name());
                dto.setOriginalValue(JSON.toJSONBytes(functionalCase));
                dtoList.add(dto);
            });
        }
        return dtoList;
    }

    /**
     * @param moveMode 移动方式 前后
     * @param targetId 要移动到的目标元素标识
     * @param moveId   要移动的元素标识
     * @param sources  sources
     * @return List<?>
     */
    private <T> List<T> sortMindCases(String moveMode, String targetId, String moveId, List<T> sources, Function<T, String> idExtractor, Function<T, Long> posExtractor) {
        int targetIndex = 0;
        int nodeIndex = 0;
        boolean findTarget = false;
        boolean findNode = false;
        T currentNode = null;
        sources = sources.stream().sorted(Comparator.comparing(posExtractor)).collect(Collectors.toList());
        for (int i = 0; i < sources.size(); i++) {
            if (StringUtils.equalsIgnoreCase(targetId, idExtractor.apply(sources.get(i)))) {
                targetIndex = i;
                findTarget = true;
            }
            if (StringUtils.equalsIgnoreCase(moveId, idExtractor.apply(sources.get(i)))) {
                nodeIndex = i;
                findNode = true;
                currentNode = sources.get(i);
            }
            if (findTarget && findNode) {
                break;
            }
        }
        sources.remove(nodeIndex);
        List<T> beforeNode;
        List<T> afterNode;
        //证明相邻点不是同种类型，放到1位
        if (targetIndex == 0 && !findTarget) {
            beforeNode = new ArrayList<>();
            afterNode = sources;
        } else {
            if (StringUtils.equals(moveMode, MoveTypeEnum.AFTER.name())) {
                if (targetIndex > sources.size()) {
                    beforeNode = sources;
                    afterNode = new ArrayList<>();
                }  else if (targetIndex == 0){
                    beforeNode = new ArrayList<>();
                    afterNode = sources;
                }
                else {
                    beforeNode = sources.subList(0, targetIndex - 1);
                    afterNode = sources.subList(targetIndex - 1, sources.size());
                }
            } else {
                beforeNode = sources.subList(0, targetIndex);
                afterNode = sources.subList(targetIndex, sources.size());
            }
        }
        List<T> finallyNode = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(beforeNode)) {
            finallyNode.addAll(beforeNode);
        }
        if (currentNode != null) {
            finallyNode.add(currentNode);
        }
        if (CollectionUtils.isNotEmpty(afterNode)) {
            finallyNode.addAll(afterNode);
        }
        return finallyNode;
    }

    private static String getModuleMapKey(Map<String, String> targetModuleMap, String targetId, Map<String, String> targetCaseMap, Map<String, String> targetTextMap) {
        String moduleMapKey;
        String parentId = targetModuleMap.get(targetId);
        String caseModuleId = targetCaseMap.get(targetId);
        String textParentId = targetTextMap.get(targetId);
        if (StringUtils.isNotBlank(parentId)) {
            moduleMapKey = parentId;
        } else if (StringUtils.isNotBlank(caseModuleId)) {
            moduleMapKey = caseModuleId;
        } else {
            moduleMapKey = textParentId;
        }
        return moduleMapKey;
    }

    private void dealLogAndNotice(FunctionalCaseMinderEditRequest request, String userId, FunctionalMinderUpdateDTO functionalMinderUpdateDTO) {
        Project project = projectMapper.selectByPrimaryKey(request.getProjectId());
        for (LogDTO addLogDTO : functionalMinderUpdateDTO.getAddLogDTOS()) {
            addLogDTO.setOrganizationId(project.getOrganizationId());
        }
        for (LogDTO updateLogDTO : functionalMinderUpdateDTO.getUpdateLogDTOS()) {
            updateLogDTO.setOrganizationId(project.getOrganizationId());
        }
        for (LogDTO updateLogDTO : functionalMinderUpdateDTO.getDeleteLogDTOS()) {
            updateLogDTO.setOrganizationId(project.getOrganizationId());
        }
        operationLogService.batchAdd(functionalMinderUpdateDTO.getAddLogDTOS());
        operationLogService.batchAdd(functionalMinderUpdateDTO.getUpdateLogDTOS());
        operationLogService.batchAdd(functionalMinderUpdateDTO.getDeleteLogDTOS());
        User user = userMapper.selectByPrimaryKey(userId);
        List<Map> resources = new ArrayList<>();
        resources.addAll(JSON.parseArray(JSON.toJSONString(functionalMinderUpdateDTO.getNoticeList()), Map.class));
        commonNoticeSendService.sendNotice(NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK, NoticeConstants.Event.CREATE, resources, user, request.getProjectId());
        resources = new ArrayList<>();
        resources.addAll(JSON.parseArray(JSON.toJSONString(functionalMinderUpdateDTO.getUpdateNoticeList()), Map.class));
        commonNoticeSendService.sendNotice(NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK, NoticeConstants.Event.UPDATE, resources, user, request.getProjectId());
    }

    private Map<String, String> dealCase(FunctionalCaseMinderEditRequest request, String userId, SqlSession sqlSession, Map<String, String> sourceIdAndInsertModuleIdMap, FunctionalMinderUpdateDTO functionalMinderUpdateDTO) {
        Map<String, String> sourceIdAndInsertCaseIdMap = new HashMap<>();
        Map<String, String> sourceIdAndTargetIdsMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(request.getUpdateCaseList())) {
            FunctionalCaseMapper caseMapper = sqlSession.getMapper(FunctionalCaseMapper.class);
            FunctionalCaseBlobMapper caseBlobMapper = sqlSession.getMapper(FunctionalCaseBlobMapper.class);
            FunctionalCaseCustomFieldMapper caseCustomFieldMapper = sqlSession.getMapper(FunctionalCaseCustomFieldMapper.class);
            // 获取页面改动的自定义字段值
            Map<String, String> customFieldNameMap = getCustomFieldNameMap(request);
            //获取自定义字段的默认值
            Map<String, Object> defaultCustomFieldValueMap = getDefaultCustomFieldValueMap(request);
            Map<String, List<FunctionalCaseChangeRequest>> resourceMap = request.getUpdateCaseList().stream().collect(Collectors.groupingBy(FunctionalCaseChangeRequest::getType));
            //处理新增
            List<FunctionalCaseChangeRequest> addList = resourceMap.get(OperationLogType.ADD.toString());
            if (CollectionUtils.isNotEmpty(addList)) {
                for (FunctionalCaseChangeRequest functionalCaseChangeRequest : addList) {
                    //基本信息
                    FunctionalCase functionalCase = addCase(request, userId, functionalCaseChangeRequest, caseMapper, sourceIdAndInsertModuleIdMap);
                    String caseId = functionalCase.getId();
                    sourceIdAndInsertCaseIdMap.put(functionalCaseChangeRequest.getId(), caseId);
                    //附属表
                    FunctionalCaseBlob functionalCaseBlob = addCaseBlob(functionalCaseChangeRequest, caseId, caseBlobMapper);
                    //保存自定义字段
                    List<FunctionalCaseCustomField> functionalCaseCustomFields = addCustomFields(functionalCaseChangeRequest, caseId, caseCustomFieldMapper, defaultCustomFieldValueMap);
                    //保存用例等级
                    FunctionalCaseCustomField customField = new FunctionalCaseCustomField();
                    customField.setCaseId(caseId);
                    customField.setFieldId(defaultCustomFieldValueMap.get("priorityFieldId").toString());
                    customField.setValue("P" + (functionalCaseChangeRequest.getPriority() == 0 ? 0 : functionalCaseChangeRequest.getPriority() - 1));
                    caseCustomFieldMapper.insertSelective(customField);
                    //日志
                    FunctionalCaseHistoryLogDTO historyLogDTO = new FunctionalCaseHistoryLogDTO(functionalCase, functionalCaseBlob, functionalCaseCustomFields, new ArrayList<>(), new ArrayList<>());
                    LogDTO logDTO = addLog(request, userId, caseId, historyLogDTO, null, OperationLogType.ADD);
                    functionalMinderUpdateDTO.getAddLogDTOS().add(logDTO);
                    //消息通知
                    FunctionalCaseDTO functionalCaseDTO = getFunctionalCaseDTO(functionalCase, functionalCaseCustomFields, customFieldNameMap);
                    functionalMinderUpdateDTO.getNoticeList().add(functionalCaseDTO);
                    updateTargetIdsMap(functionalCaseChangeRequest.getId(), functionalCaseChangeRequest.getTargetId(), sourceIdAndTargetIdsMap);
                }
            }
            //处理更新
            List<FunctionalCaseChangeRequest> updateList = resourceMap.get(OperationLogType.UPDATE.toString());
            if (CollectionUtils.isNotEmpty(updateList)) {
                List<String> caseIds = updateList.stream().map(FunctionalCaseChangeRequest::getId).toList();
                //获取已存在的自定义字段值
                Map<String, List<FunctionalCaseCustomField>> oldCaseCustomFieldMap = getOldCaseCustomFieldMap(caseIds);

                FunctionalCaseExample functionalCaseExample = new FunctionalCaseExample();
                functionalCaseExample.createCriteria().andIdIn(caseIds);
                List<FunctionalCase> oldCase = functionalCaseMapper.selectByExample(functionalCaseExample);
                Map<String, FunctionalCase> oldCaseMap = oldCase.stream().collect(Collectors.toMap(FunctionalCase::getId, t -> t));
                FunctionalCaseBlobExample functionalCaseBlobExample = new FunctionalCaseBlobExample();
                functionalCaseBlobExample.createCriteria().andIdIn(caseIds);
                List<FunctionalCaseBlob> functionalCaseBlobs = functionalCaseBlobMapper.selectByExample(functionalCaseBlobExample);
                Map<String, FunctionalCaseBlob> oldBlobMap = functionalCaseBlobs.stream().collect(Collectors.toMap(FunctionalCaseBlob::getId, t -> t));

                for (FunctionalCaseChangeRequest functionalCaseChangeRequest : updateList) {
                    //基本信息
                    String caseId = functionalCaseChangeRequest.getId();
                    FunctionalCase functionalCase = updateCase(functionalCaseChangeRequest, userId, caseMapper);
                    //更新附属表信息
                    FunctionalCaseBlob functionalCaseBlob = updateBlob(functionalCaseChangeRequest, caseId, caseBlobMapper);
                    //更新自定义字段
                    String fieldId = defaultCustomFieldValueMap.get("priorityFieldId").toString();
                    List<FunctionalCaseCustomField> functionalCaseCustomFields = updateCustomFields(functionalCaseChangeRequest, oldCaseCustomFieldMap, caseId, fieldId, caseCustomFieldMapper);
                    //日志
                    FunctionalCaseHistoryLogDTO historyLogDTO = new FunctionalCaseHistoryLogDTO(functionalCase, functionalCaseBlob, oldCaseCustomFieldMap.get(caseId), new ArrayList<>(), new ArrayList<>());
                    FunctionalCaseHistoryLogDTO old = new FunctionalCaseHistoryLogDTO(oldCaseMap.get(caseId), oldBlobMap.get(caseId), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
                    LogDTO logDTO = addLog(request, userId, caseId, historyLogDTO, old, OperationLogType.UPDATE);
                    functionalMinderUpdateDTO.getUpdateLogDTOS().add(logDTO);
                    //通知
                    FunctionalCaseDTO functionalCaseDTO = getFunctionalCaseDTO(functionalCase, functionalCaseCustomFields, customFieldNameMap);
                    functionalMinderUpdateDTO.getUpdateNoticeList().add(functionalCaseDTO);
                    updateTargetIdsMap(functionalCaseChangeRequest.getId(), functionalCaseChangeRequest.getTargetId(), sourceIdAndTargetIdsMap);
                }
            }
        }
        setDTOTargetMap(functionalMinderUpdateDTO, sourceIdAndTargetIdsMap);
        return sourceIdAndInsertCaseIdMap;
    }

    /**
     * 根据Ids获取已存在的自定义字段值
     *
     * @param caseIds caseIds
     * @return Map<String, List < FunctionalCaseCustomField>>
     */
    private Map<String, List<FunctionalCaseCustomField>> getOldCaseCustomFieldMap(List<String> caseIds) {
        FunctionalCaseCustomFieldExample example = new FunctionalCaseCustomFieldExample();
        example.createCriteria().andCaseIdIn(caseIds);
        List<FunctionalCaseCustomField> allFields = functionalCaseCustomFieldMapper.selectByExample(example);
        return allFields.stream().collect(Collectors.groupingBy(FunctionalCaseCustomField::getCaseId));
    }

    @NotNull
    private Map<String, Object> getDefaultCustomFieldValueMap(FunctionalCaseMinderEditRequest request) {
        TemplateDTO defaultTemplateDTO = projectTemplateService.getDefaultTemplateDTO(request.getProjectId(), TemplateScene.FUNCTIONAL.toString());
        List<TemplateCustomFieldDTO> customFields = defaultTemplateDTO.getCustomFields();
        Map<String, Object> defaultValueMap = new HashMap<>();
        String priorityFieldId = null;
        for (TemplateCustomFieldDTO field : customFields) {
            if (StringUtils.equalsIgnoreCase(field.getFieldName(), Translator.get("custom_field.functional_priority"))) {
                priorityFieldId = field.getFieldId();
            }
            if (field.getDefaultValue() != null && !StringUtils.equalsIgnoreCase(field.getFieldName(), Translator.get("custom_field.functional_priority"))) {
                defaultValueMap.put(field.getFieldId(), field.getDefaultValue());
            }
        }
        defaultValueMap.put("priorityFieldId", priorityFieldId);
        return defaultValueMap;
    }

    private Map<String, String> dealAdditionalNode(FunctionalCaseMinderEditRequest request, String userId, MindAdditionalNodeMapper additionalNodeMapper, Map<String, String> sourceIdAndInsertModuleIdMap, FunctionalMinderUpdateDTO functionalMinderUpdateDTO) {
        Map<String, String> sourceIdAndInsertTextIdMap = new HashMap<>();
        Map<String, String> sourceIdAndTargetIdsMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(request.getAdditionalNodeList())) {
            Map<String, List<MindAdditionalNodeRequest>> resourceMap = request.getAdditionalNodeList().stream().collect(Collectors.groupingBy(MindAdditionalNodeRequest::getType));
            List<MindAdditionalNodeRequest> addList = resourceMap.get(OperationLogType.ADD.toString());
            //空白节点的父节点不一定是空白节点，有可能是模块
            if (CollectionUtils.isNotEmpty(addList)) {
                List<MindAdditionalNode> nodes = new ArrayList<>();
                for (MindAdditionalNodeRequest mindAdditionalNodeRequest : addList) {
                    MindAdditionalNode mindAdditionalNode = buildNode(request, userId, mindAdditionalNodeRequest, additionalNodeMapper);
                    nodes.add(mindAdditionalNode);
                    sourceIdAndInsertTextIdMap.put(mindAdditionalNodeRequest.getId(), mindAdditionalNode.getId());
                    updateTargetIdsMap(mindAdditionalNodeRequest.getId(), mindAdditionalNodeRequest.getTargetId(), sourceIdAndTargetIdsMap);
                }
                for (MindAdditionalNode node : nodes) {
                    if (StringUtils.isNotBlank(sourceIdAndInsertTextIdMap.get(node.getParentId()))) {
                        node.setParentId(sourceIdAndInsertTextIdMap.get(node.getParentId()));
                    }
                    if (StringUtils.isNotBlank(sourceIdAndInsertModuleIdMap.get(node.getParentId()))) {
                        node.setParentId(sourceIdAndInsertModuleIdMap.get(node.getParentId()));
                    }
                    additionalNodeMapper.insert(node);
                }
            }
            //处理更新
            List<MindAdditionalNodeRequest> updateList = resourceMap.get(OperationLogType.UPDATE.toString());
            if (CollectionUtils.isNotEmpty(updateList)) {
                List<MindAdditionalNode> nodes = new ArrayList<>();
                for (MindAdditionalNodeRequest mindAdditionalNodeRequest : updateList) {
                    MindAdditionalNode updateModule = updateNode(userId, mindAdditionalNodeRequest, additionalNodeMapper);
                    nodes.add(updateModule);
                    updateTargetIdsMap(mindAdditionalNodeRequest.getId(), mindAdditionalNodeRequest.getTargetId(), sourceIdAndTargetIdsMap);
                }
                for (MindAdditionalNode node : nodes) {
                    if (StringUtils.isNotBlank(sourceIdAndInsertTextIdMap.get(node.getParentId()))) {
                        node.setParentId(sourceIdAndInsertTextIdMap.get(node.getParentId()));
                    }
                    if (StringUtils.isNotBlank(sourceIdAndInsertModuleIdMap.get(node.getParentId()))) {
                        node.setParentId(sourceIdAndInsertModuleIdMap.get(node.getParentId()));
                    }
                    additionalNodeMapper.updateByPrimaryKeySelective(node);
                }
            }
        }
        setDTOTargetMap(functionalMinderUpdateDTO, sourceIdAndTargetIdsMap);
        return sourceIdAndInsertTextIdMap;
    }

    private static void setDTOTargetMap(FunctionalMinderUpdateDTO functionalMinderUpdateDTO, Map<String, String> sourceIdAndTargetIdsMap) {
        Map<String, String> existMap = functionalMinderUpdateDTO.getSourceIdAndTargetIdsMap();
        if (MapUtils.isEmpty(existMap)) {
            existMap = new HashMap<>();
        }
        existMap.putAll(sourceIdAndTargetIdsMap);
        functionalMinderUpdateDTO.setSourceIdAndTargetIdsMap(existMap);
    }

    private MindAdditionalNode updateNode(String userId, MindAdditionalNodeRequest mindAdditionalNodeRequest, MindAdditionalNodeMapper mindAdditionalNodeMapper) {
        MindAdditionalNode mindAdditionalNode = new MindAdditionalNode();
        mindAdditionalNode.setId(mindAdditionalNodeRequest.getId());
        if (StringUtils.isBlank(mindAdditionalNodeRequest.getName())) {
            throw new MSException(Translator.get("minder_extra_node.text_node_empty"));
        }
        if (mindAdditionalNodeRequest.getName().length() > 255) {
            mindAdditionalNodeRequest.setName(mindAdditionalNodeRequest.getName().substring(0, 249));
        }
        mindAdditionalNode.setName(mindAdditionalNodeRequest.getName());
        mindAdditionalNode.setParentId(mindAdditionalNodeRequest.getParentId());
        mindAdditionalNode.setUpdateTime(System.currentTimeMillis());
        mindAdditionalNode.setUpdateUser(userId);
        mindAdditionalNode.setCreateUser(null);
        mindAdditionalNode.setCreateTime(null);
        return mindAdditionalNode;
    }

    private MindAdditionalNode buildNode(FunctionalCaseMinderEditRequest request, String userId, MindAdditionalNodeRequest mindAdditionalNodeRequest, MindAdditionalNodeMapper additionalNodeMapper) {
        MindAdditionalNode mindAdditionalNode = new MindAdditionalNode();
        mindAdditionalNode.setId(mindAdditionalNodeRequest.getId());
        if (StringUtils.isBlank(mindAdditionalNodeRequest.getName())) {
            throw new MSException(Translator.get("minder_extra_node.text_node_empty"));
        }
        if (mindAdditionalNodeRequest.getName().length() > 255) {
            mindAdditionalNodeRequest.setName(mindAdditionalNodeRequest.getName().substring(0, 249));
        }
        mindAdditionalNode.setName(mindAdditionalNodeRequest.getName());
        mindAdditionalNode.setParentId(mindAdditionalNodeRequest.getParentId());
        mindAdditionalNode.setProjectId(request.getProjectId());
        mindAdditionalNode.setCreateTime(System.currentTimeMillis());
        mindAdditionalNode.setUpdateTime(mindAdditionalNode.getCreateTime());
        mindAdditionalNode.setPos(LIMIT_POS);
        mindAdditionalNode.setCreateUser(userId);
        mindAdditionalNode.setUpdateUser(userId);
        return mindAdditionalNode;
    }

    private Map<String, String> dealModule(FunctionalCaseMinderEditRequest request, String userId, FunctionalCaseModuleMapper moduleMapper, FunctionalMinderUpdateDTO functionalMinderUpdateDTO) {
        //页面传来的模块ids 与系统内新增的ID的map
        Map<String, String> sourceIdAndInsertIdMap = new HashMap<>();
        Map<String, String> sourceIdAndTargetIdsMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(request.getUpdateModuleList())) {
            //处理新增
            Map<String, List<FunctionalCaseModuleEditRequest>> resourceMap = request.getUpdateModuleList().stream().collect(Collectors.groupingBy(FunctionalCaseModuleEditRequest::getType));
            List<FunctionalCaseModuleEditRequest> addList = resourceMap.get(OperationLogType.ADD.toString());
            if (CollectionUtils.isNotEmpty(addList)) {
                List<FunctionalCaseModule> modules = new ArrayList<>();
                //查出已存在同层级的节点
                Map<String, List<FunctionalCaseModule>> parentIdInDBMap = getParentIdInDBMap(addList, request.getProjectId());
                for (FunctionalCaseModuleEditRequest functionalCaseModuleEditRequest : addList) {
                    //加限制
                    if (StringUtils.equalsIgnoreCase(functionalCaseModuleEditRequest.getParentId(), "root")) {
                        throw new MSException(Translator.get("functional_case.module.default.name.add_error"));
                    }
                    if (StringUtils.equalsIgnoreCase(functionalCaseModuleEditRequest.getName(), Translator.get("functional_case.module.default.name")) && StringUtils.equalsIgnoreCase(functionalCaseModuleEditRequest.getParentId(), "NONE")) {
                        throw new MSException(Translator.get("node.name.repeat"));
                    }
                    FunctionalCaseModule functionalCaseModule = buildModule(request, userId, functionalCaseModuleEditRequest);
                    modules.add(functionalCaseModule);
                    sourceIdAndInsertIdMap.put(functionalCaseModuleEditRequest.getId(), functionalCaseModule.getId());
                    updateTargetIdsMap(functionalCaseModuleEditRequest.getId(), functionalCaseModuleEditRequest.getTargetId(), sourceIdAndTargetIdsMap);
                }
                for (FunctionalCaseModule module : modules) {
                    if (StringUtils.isNotBlank(sourceIdAndInsertIdMap.get(module.getParentId()))) {
                        module.setParentId(sourceIdAndInsertIdMap.get(module.getParentId()));
                    }
                    checkModules(module, parentIdInDBMap, OperationLogType.ADD.toString());
                    moduleMapper.insert(module);
                }
                functionalCaseModuleLogService.handleModuleLog(modules, request.getProjectId(), userId, "/functional/mind/case/edit", OperationLogType.ADD.name(), "");
            }
            //处理更新（更新的情况是可能换数据本身，可能换父节点，可能换顺序）
            List<FunctionalCaseModuleEditRequest> updateList = resourceMap.get(OperationLogType.UPDATE.toString());
            if (CollectionUtils.isNotEmpty(updateList)) {
                List<FunctionalCaseModule> modules = new ArrayList<>();
                Map<String, List<FunctionalCaseModule>> parentIdInDBMap = getParentIdInDBMap(updateList, request.getProjectId());
                for (FunctionalCaseModuleEditRequest functionalCaseModuleEditRequest : updateList) {
                    //加限制
                    if (StringUtils.equalsIgnoreCase(functionalCaseModuleEditRequest.getParentId(), "root")) {
                        throw new MSException(Translator.get("functional_case.module.default.name.add_error"));
                    }
                    FunctionalCaseModule updateModule = updateModule(userId, functionalCaseModuleEditRequest);
                    modules.add(updateModule);
                    updateTargetIdsMap(functionalCaseModuleEditRequest.getId(), functionalCaseModuleEditRequest.getTargetId(), sourceIdAndTargetIdsMap);
                }
                for (FunctionalCaseModule module : modules) {
                    if (StringUtils.isNotBlank(sourceIdAndInsertIdMap.get(module.getParentId()))) {
                        module.setParentId(sourceIdAndInsertIdMap.get(module.getParentId()));
                    }
                    checkModules(module, parentIdInDBMap, OperationLogType.UPDATE.toString());
                    moduleMapper.updateByPrimaryKeySelective(module);
                }
                functionalCaseModuleLogService.handleModuleLog(modules, request.getProjectId(), userId, "/functional/mind/case/edit", OperationLogType.UPDATE.name(), "");
            }
        }
        setDTOTargetMap(functionalMinderUpdateDTO, sourceIdAndTargetIdsMap);
        return sourceIdAndInsertIdMap;
    }

    private static void updateTargetIdsMap(String sourceId, String targetId, Map<String, String> sourceIdAndTargetIdsMap) {
        sourceIdAndTargetIdsMap.put(sourceId, targetId);
    }

    @NotNull
    private Map<String, List<FunctionalCaseModule>> getParentIdInDBMap(List<FunctionalCaseModuleEditRequest> functionalCaseModuleEditRequests, String projectId) {
        List<String> parentIds = functionalCaseModuleEditRequests.stream().map(FunctionalCaseModuleEditRequest::getParentId).toList();
        FunctionalCaseModuleExample functionalCaseModuleExample = new FunctionalCaseModuleExample();
        functionalCaseModuleExample.createCriteria().andParentIdIn(parentIds).andProjectIdEqualTo(projectId);
        List<FunctionalCaseModule> sameParentListInDB = functionalCaseModuleMapper.selectByExample(functionalCaseModuleExample);
        return sameParentListInDB.stream().collect(Collectors.groupingBy(FunctionalCaseModule::getParentId));
    }

    /**
     * 检查同一个父模块下不能有同名子集
     *
     * @param parentIdMap 同一父模块的其他子集
     * @param type        保存类型
     */
    private static void checkModules(FunctionalCaseModule module, Map<String, List<FunctionalCaseModule>> parentIdMap, String type) {
        List<FunctionalCaseModule> functionalCaseModules = parentIdMap.get(module.getParentId());
        if (CollectionUtils.isEmpty(functionalCaseModules)) {
            //新增的算上
            List<FunctionalCaseModule> modules = new ArrayList<>();
            modules.add(module);
            parentIdMap.put(module.getParentId(), modules);
            return;
        }
        if (StringUtils.equalsIgnoreCase(type, OperationLogType.ADD.toString())) {
            List<FunctionalCaseModule> sameNameList = functionalCaseModules.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getName(), module.getName())).toList();
            if (CollectionUtils.isNotEmpty(sameNameList)) {
                throw new MSException(Translator.get("node.name.repeat"));
            } else {
                functionalCaseModules.add(module);
                parentIdMap.put(module.getParentId(), functionalCaseModules);
            }
        } else {
            List<FunctionalCaseModule> sameNameList = functionalCaseModules.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getName(), module.getName()) && !StringUtils.equalsIgnoreCase(t.getId(), module.getId())).toList();
            if (CollectionUtils.isNotEmpty(sameNameList)) {
                throw new MSException(Translator.get("node.name.repeat"));
            } else {
                functionalCaseModules.add(module);
                parentIdMap.put(module.getParentId(), functionalCaseModules);
            }
        }
    }

    /**
     * 获取页面改动的自定义字段
     */
    @NotNull
    private Map<String, String> getCustomFieldNameMap(FunctionalCaseMinderEditRequest request) {
        List<CaseCustomFieldDTO> caseCustomFieldDTOS = new ArrayList<>();
        for (FunctionalCaseChangeRequest caseChangeRequest : request.getUpdateCaseList()) {
            if (CollectionUtils.isNotEmpty(caseChangeRequest.getCustomFields())) {
                caseCustomFieldDTOS.addAll(caseChangeRequest.getCustomFields());
            }
        }
        if (CollectionUtils.isEmpty(caseCustomFieldDTOS)) {
            return new HashMap<>();
        }
        List<String> caseCustomFields = caseCustomFieldDTOS.stream().map(CaseCustomFieldDTO::getFieldId).toList();
        CustomFieldExample customFieldExample = new CustomFieldExample();
        customFieldExample.createCriteria().andIdIn(caseCustomFields);

        List<CustomField> customFields = customFieldMapper.selectByExample(customFieldExample);
        return customFields.stream().collect(Collectors.toMap(CustomField::getId, CustomField::getName));
    }

    @NotNull
    private static FunctionalCaseDTO getFunctionalCaseDTO(FunctionalCase functionalCase, List<FunctionalCaseCustomField> functionalCaseCustomFields, Map<String, String> customFieldNameMap) {
        FunctionalCaseDTO functionalCaseDTO = new FunctionalCaseDTO();
        BeanUtils.copyBean(functionalCaseDTO, functionalCase);
        functionalCaseDTO.setTriggerMode(NoticeConstants.TriggerMode.MANUAL_EXECUTION);
        List<OptionDTO> fields = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(functionalCaseCustomFields)) {
            for (FunctionalCaseCustomField customFieldDTO : functionalCaseCustomFields) {
                OptionDTO optionDTO = new OptionDTO();
                String name = customFieldNameMap.get(customFieldDTO.getFieldId());
                if (StringUtils.isBlank(name)) {
                    continue;
                }
                optionDTO.setId(name);
                optionDTO.setName(customFieldDTO.getValue());
                fields.add(optionDTO);
            }
        }
        functionalCaseDTO.setFields(fields);
        return functionalCaseDTO;
    }

    private LogDTO addLog(FunctionalCaseMinderEditRequest request, String userId, String caseId, FunctionalCaseHistoryLogDTO historyLogDTO, FunctionalCaseHistoryLogDTO old, OperationLogType operationLogType) {
        LogDTO dto = new LogDTO(
                request.getProjectId(),
                null,
                caseId,
                userId,
                operationLogType.name(),
                OperationLogModule.CASE_MANAGEMENT_CASE_CASE,
                historyLogDTO.getFunctionalCase().getName());
        dto.setHistory(true);
        dto.setPath("/functional/mind/case/edit");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setModifiedValue(JSON.toJSONBytes(historyLogDTO));
        dto.setOriginalValue(JSON.toJSONBytes(old));
        return dto;
    }

    @NotNull
    private FunctionalCaseModule buildModule(FunctionalCaseMinderEditRequest request, String userId, FunctionalCaseModuleEditRequest functionalCaseModuleEditRequest) {
        FunctionalCaseModule functionalCaseModule = new FunctionalCaseModule();
        functionalCaseModule.setId(functionalCaseModuleEditRequest.getId());
        if (StringUtils.isBlank(functionalCaseModuleEditRequest.getName())) {
            throw new MSException(Translator.get("api_definition_module.name.not_blank"));
        }
        if (functionalCaseModuleEditRequest.getName().length() > 255) {
            functionalCaseModuleEditRequest.setName(functionalCaseModuleEditRequest.getName().substring(0, 249));
        }
        functionalCaseModule.setName(functionalCaseModuleEditRequest.getName());
        functionalCaseModule.setParentId(functionalCaseModuleEditRequest.getParentId());
        functionalCaseModule.setProjectId(request.getProjectId());
        functionalCaseModule.setCreateTime(System.currentTimeMillis());
        functionalCaseModule.setUpdateTime(functionalCaseModule.getCreateTime());
        functionalCaseModule.setPos(LIMIT_POS);
        functionalCaseModule.setCreateUser(userId);
        functionalCaseModule.setUpdateUser(userId);
        return functionalCaseModule;
    }

    @NotNull
    private FunctionalCaseModule updateModule(String userId, FunctionalCaseModuleEditRequest functionalCaseModuleEditRequest) {
        FunctionalCaseModule updateModule = new FunctionalCaseModule();
        updateModule.setId(functionalCaseModuleEditRequest.getId());
        if (StringUtils.isBlank(functionalCaseModuleEditRequest.getName())) {
            throw new MSException(Translator.get("api_definition_module.name.not_blank"));
        }
        if (functionalCaseModuleEditRequest.getName().length() > 255) {
            functionalCaseModuleEditRequest.setName(functionalCaseModuleEditRequest.getName().substring(0, 249));
        }
        updateModule.setName(functionalCaseModuleEditRequest.getName());
        updateModule.setParentId(functionalCaseModuleEditRequest.getParentId());
        updateModule.setUpdateTime(System.currentTimeMillis());
        updateModule.setUpdateUser(userId);
        updateModule.setCreateUser(null);
        updateModule.setCreateTime(null);
        return updateModule;
    }

    private FunctionalCase updateCase(FunctionalCaseChangeRequest request, String userId, FunctionalCaseMapper caseMapper) {
        FunctionalCase functionalCase = new FunctionalCase();
        BeanUtils.copyBean(functionalCase, request);
        if (StringUtils.isBlank(functionalCase.getName())) {
            throw new MSException(Translator.get("minder_extra_node.case_node_empty"));
        }
        if (functionalCase.getName().length() > 255) {
            functionalCase.setName(functionalCase.getName().substring(0, 249));
        }
        functionalCase.setUpdateUser(userId);
        functionalCase.setUpdateTime(System.currentTimeMillis());
        functionalCase.setCreateUser(null);
        functionalCase.setCreateTime(null);
        functionalCase.setTags(null);
        //更新用例
        caseMapper.updateByPrimaryKeySelective(functionalCase);
        return caseMapper.selectByPrimaryKey(functionalCase.getId());
    }

    private FunctionalCaseBlob updateBlob(FunctionalCaseChangeRequest functionalCaseChangeRequest, String caseId, FunctionalCaseBlobMapper caseBlobMapper) {
        FunctionalCaseBlob functionalCaseBlob = new FunctionalCaseBlob();
        functionalCaseBlob.setId(caseId);
        functionalCaseBlob.setSteps(StringUtils.defaultIfBlank(functionalCaseChangeRequest.getSteps(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        functionalCaseBlob.setTextDescription(StringUtils.defaultIfBlank(functionalCaseChangeRequest.getTextDescription(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        functionalCaseBlob.setExpectedResult(StringUtils.defaultIfBlank(functionalCaseChangeRequest.getExpectedResult(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        functionalCaseBlob.setPrerequisite(StringUtils.defaultIfBlank(functionalCaseChangeRequest.getPrerequisite(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        functionalCaseBlob.setDescription(StringUtils.defaultIfBlank(functionalCaseChangeRequest.getDescription(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        caseBlobMapper.updateByPrimaryKeyWithBLOBs(functionalCaseBlob);
        return functionalCaseBlob;
    }

    private List<FunctionalCaseCustomField> updateCustomFields(FunctionalCaseChangeRequest functionalCaseChangeRequest, Map<String, List<FunctionalCaseCustomField>> caseCustomFieldMap, String caseId, String fieldId, FunctionalCaseCustomFieldMapper caseCustomFieldMapper) {
        List<FunctionalCaseCustomField> total = new ArrayList<>();
        List<FunctionalCaseCustomField> functionalCaseCustomFields = caseCustomFieldMap.get(caseId);
        if (CollectionUtils.isEmpty(functionalCaseCustomFields)) {
            functionalCaseCustomFields = new ArrayList<>();
        }
        List<CaseCustomFieldDTO> customFields = functionalCaseChangeRequest.getCustomFields();
        //更新用例等级
        CaseCustomFieldDTO customFieldDTO = new CaseCustomFieldDTO();
        customFieldDTO.setFieldId(fieldId);
        customFieldDTO.setValue("P" + (functionalCaseChangeRequest.getPriority() == 0 ? 0 : functionalCaseChangeRequest.getPriority() - 1));
        customFields.add(customFieldDTO);
        if (CollectionUtils.isNotEmpty(customFields)) {
            customFields = customFields.stream().distinct().collect(Collectors.toList());
            List<String> fieldIds = customFields.stream().map(CaseCustomFieldDTO::getFieldId).toList();
            Map<String, FunctionalCaseCustomField> collect = functionalCaseCustomFields.stream().filter(t -> fieldIds.contains(t.getFieldId())).collect(Collectors.toMap(FunctionalCaseCustomField::getFieldId, (item) -> item));
            List<CaseCustomFieldDTO> addFields = new ArrayList<>();
            List<CaseCustomFieldDTO> updateFields = new ArrayList<>();
            customFields.forEach(customField -> {
                if (collect.containsKey(customField.getFieldId())) {
                    updateFields.add(customField);
                } else {
                    addFields.add(customField);
                }
            });
            if (CollectionUtils.isNotEmpty(addFields)) {
                List<FunctionalCaseCustomField> caseCustomFields = saveCustomField(caseId, caseCustomFieldMapper, addFields);
                total.addAll(caseCustomFields);
            }
            if (CollectionUtils.isNotEmpty(updateFields)) {
                List<FunctionalCaseCustomField> caseCustomFields = updateField(updateFields, caseId, caseCustomFieldMapper);
                total.addAll(caseCustomFields);
            }
        }
        return total;
    }

    private List<FunctionalCaseCustomField> updateField(List<CaseCustomFieldDTO> updateFields, String caseId, FunctionalCaseCustomFieldMapper caseCustomFieldMapper) {
        List<FunctionalCaseCustomField> caseCustomFields = new ArrayList<>();
        updateFields.forEach(custom -> {
            FunctionalCaseCustomField customField = new FunctionalCaseCustomField();
            customField.setCaseId(caseId);
            customField.setFieldId(custom.getFieldId());
            customField.setValue(custom.getValue());
            caseCustomFieldMapper.updateByPrimaryKeySelective(customField);
            caseCustomFields.add(customField);
        });
        return caseCustomFields;
    }

    private List<FunctionalCaseCustomField> addCustomFields(FunctionalCaseChangeRequest functionalCaseChangeRequest, String caseId, FunctionalCaseCustomFieldMapper caseCustomFieldMapper, Map<String, Object> defaultValueMap) {
        List<CaseCustomFieldDTO> customFields = functionalCaseChangeRequest.getCustomFields();
        List<String> list = customFields.stream().map(CaseCustomFieldDTO::getFieldId).toList();
        List<CaseCustomFieldDTO> defaultCustomFieldDTOs = new ArrayList<>();
        defaultValueMap.forEach((k, v) -> {
            if (!list.contains(k) && !StringUtils.equalsIgnoreCase(k, "priorityFieldId")) {
                CaseCustomFieldDTO customFieldDTO = new CaseCustomFieldDTO();
                customFieldDTO.setFieldId(k);
                customFieldDTO.setValue(v.toString());
                defaultCustomFieldDTOs.add(customFieldDTO);
            }
        });
        List<CaseCustomFieldDTO> customFieldDTOs = new ArrayList<>();
        customFields.forEach(t -> {
            if (!StringUtils.equalsIgnoreCase(t.getFieldId(), defaultValueMap.get("priorityFieldId").toString())) {
                CaseCustomFieldDTO customFieldDTO = new CaseCustomFieldDTO();
                customFieldDTO.setFieldId(t.getFieldId());
                customFieldDTO.setValue(t.getValue());
                customFieldDTOs.add(customFieldDTO);
            }
        });
        customFieldDTOs.addAll(defaultCustomFieldDTOs);
        return saveCustomField(caseId, caseCustomFieldMapper, customFieldDTOs.stream().distinct().collect(Collectors.toList()));
    }

    private List<FunctionalCaseCustomField> saveCustomField(String caseId, FunctionalCaseCustomFieldMapper caseCustomFieldMapper, List<CaseCustomFieldDTO> customFields) {
        List<FunctionalCaseCustomField> caseCustomFields = new ArrayList<>();
        customFields.forEach(custom -> {
            FunctionalCaseCustomField customField = new FunctionalCaseCustomField();
            customField.setCaseId(caseId);
            customField.setFieldId(custom.getFieldId());
            customField.setValue(custom.getValue());
            caseCustomFieldMapper.insertSelective(customField);
            caseCustomFields.add(customField);
        });
        return caseCustomFields;
    }

    private FunctionalCaseBlob addCaseBlob(FunctionalCaseChangeRequest functionalCaseChangeRequest, String caseId, FunctionalCaseBlobMapper caseBlobMapper) {
        FunctionalCaseBlob functionalCaseBlob = new FunctionalCaseBlob();
        functionalCaseBlob.setId(caseId);
        functionalCaseBlob.setSteps(StringUtils.defaultIfBlank(functionalCaseChangeRequest.getSteps(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        functionalCaseBlob.setTextDescription(StringUtils.defaultIfBlank(functionalCaseChangeRequest.getTextDescription(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        functionalCaseBlob.setExpectedResult(StringUtils.defaultIfBlank(functionalCaseChangeRequest.getExpectedResult(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        functionalCaseBlob.setPrerequisite(StringUtils.defaultIfBlank(functionalCaseChangeRequest.getPrerequisite(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        functionalCaseBlob.setDescription(StringUtils.defaultIfBlank(functionalCaseChangeRequest.getDescription(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        caseBlobMapper.insertSelective(functionalCaseBlob);
        return functionalCaseBlob;
    }

    @NotNull
    private FunctionalCase addCase(FunctionalCaseMinderEditRequest request, String userId, FunctionalCaseChangeRequest functionalCaseChangeRequest, FunctionalCaseMapper caseMapper, Map<String, String> sourceIdAndInsertModuleIdMap) {
        FunctionalCase functionalCase = new FunctionalCase();
        BeanUtils.copyBean(functionalCase, functionalCaseChangeRequest);
        if (StringUtils.isNotBlank(sourceIdAndInsertModuleIdMap.get(functionalCaseChangeRequest.getModuleId()))) {
            functionalCase.setModuleId(sourceIdAndInsertModuleIdMap.get(functionalCaseChangeRequest.getModuleId()));
        }
        if (StringUtils.isBlank(functionalCase.getName())) {
            throw new MSException(Translator.get("minder_extra_node.case_node_empty"));
        }
        if (functionalCase.getName().length() > 255) {
            functionalCase.setName(functionalCase.getName().substring(0, 249));
        }
        functionalCase.setProjectId(request.getProjectId());
        functionalCase.setVersionId(request.getVersionId());
        functionalCase.setNum(functionalCaseService.getNextNum(request.getProjectId()));
        functionalCase.setReviewStatus(FunctionalCaseReviewStatus.UN_REVIEWED.name());
        functionalCase.setPos(LIMIT_POS);
        functionalCase.setRefId(functionalCaseChangeRequest.getId());
        functionalCase.setLastExecuteResult(ExecStatus.PENDING.name());
        functionalCase.setLatest(true);
        functionalCase.setCreateUser(userId);
        functionalCase.setUpdateUser(userId);
        functionalCase.setCreateTime(System.currentTimeMillis());
        functionalCase.setUpdateTime(System.currentTimeMillis());
        functionalCase.setVersionId(StringUtils.defaultIfBlank(request.getVersionId(), extBaseProjectVersionMapper.getDefaultVersion(request.getProjectId())));
        functionalCase.setTags(functionalCaseChangeRequest.getTags());
        caseMapper.insertSelective(functionalCase);
        return functionalCase;
    }

    private void deleteResource(FunctionalCaseMinderEditRequest request, User user, FunctionalMinderUpdateDTO functionalMinderUpdateDTO) {
        if (CollectionUtils.isNotEmpty(request.getDeleteResourceList())) {
            Map<String, List<MinderOptionDTO>> resourceMap = request.getDeleteResourceList().stream().collect(Collectors.groupingBy(MinderOptionDTO::getType));
            List<MinderOptionDTO> caseOptionDTOS = resourceMap.get(Translator.get("minder_extra_node.case"));
            List<String> caseIds = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(caseOptionDTOS)) {
                caseIds = caseOptionDTOS.stream().map(MinderOptionDTO::getId).toList();
                List<LogDTO> logDTOS = deleteLog(caseIds, user.getId());
                functionalMinderUpdateDTO.getDeleteLogDTOS().addAll(logDTOS);
                functionalCaseService.handDeleteFunctionalCase(caseIds, false, user.getId(), request.getProjectId());
            }
            List<MinderOptionDTO> caseModuleOptionDTOS = resourceMap.get(Translator.get("minder_extra_node.module"));
            if (CollectionUtils.isNotEmpty(caseModuleOptionDTOS)) {
                List<String> moduleIds = new ArrayList<>(caseModuleOptionDTOS.stream().map(MinderOptionDTO::getId).toList());
                if (moduleIds.contains("NONE")) {
                    throw new MSException(Translator.get("all.module.default.name.cut_error"));
                }
                if (moduleIds.contains("root")) {
                    throw new MSException(Translator.get("functional_case.module.default.name.cut_error"));
                }
                //模块日志
                FunctionalCaseModuleExample moduleExample = new FunctionalCaseModuleExample();
                moduleExample.createCriteria().andIdIn(moduleIds);
                List<FunctionalCaseModule> modules = functionalCaseModuleMapper.selectByExample(moduleExample);
                functionalCaseModuleLogService.handleModuleLog(modules, request.getProjectId(), user.getId(), "/functional/mind/case/edit", OperationLogType.DELETE.name(), " " + Translator.get("log.delete_module"));

                List<FunctionalCase> functionalCases = functionalCaseModuleService.deleteModuleByIds(moduleIds, new ArrayList<>(), user.getId());
                functionalCaseModuleLogService.batchDelLog(functionalCases, request.getProjectId(), user.getId(), "/functional/mind/case/edit");
                List<String> finalCaseIds = caseIds;
                List<String> caseIdList = functionalCases.stream().map(FunctionalCase::getId).filter(id -> !finalCaseIds.contains(id)).toList();
                if (CollectionUtils.isNotEmpty(caseIdList)) {
                    functionalCaseNoticeService.batchSendNotice(request.getProjectId(), caseIdList, user, NoticeConstants.Event.DELETE);
                }
            }
            List<MinderOptionDTO> additionalOptionDTOS = resourceMap.get(ModuleConstants.ROOT_NODE_PARENT_ID);
            if (CollectionUtils.isNotEmpty(additionalOptionDTOS)) {
                List<String> mindAdditionalNodeIds = additionalOptionDTOS.stream().map(MinderOptionDTO::getId).toList();
                dealMindAdditionalMode(mindAdditionalNodeIds, mindAdditionalNodeMapper);
            }
        }
    }

    private void dealMindAdditionalMode(List<String> mindAdditionalNodeIds, MindAdditionalNodeMapper additionalNodeMapper) {
        MindAdditionalNodeExample mindAdditionalNodeExample = new MindAdditionalNodeExample();
        mindAdditionalNodeExample.createCriteria().andIdIn(mindAdditionalNodeIds);
        additionalNodeMapper.deleteByExample(mindAdditionalNodeExample);
    }

    public List<FunctionalMinderTreeDTO> getReviewMindFunctionalCase(FunctionalCaseReviewMindRequest request, boolean deleted, String userId, String viewStatusUserId) {
        List<FunctionalMinderTreeDTO> list = new ArrayList<>();
        //查出当前模块下的所有用例
        List<FunctionalCaseMindDTO> functionalCaseMindDTOList = extFunctionalCaseMapper.getMinderCaseReviewList(request, deleted, userId, viewStatusUserId, request.getSortString());
        List<String> fieldIds = getFieldIds(request.getProjectId());
        List<FunctionalCaseCustomField> caseCustomFieldList = extFunctionalCaseMapper.getCaseCustomFieldList(request, deleted, fieldIds);
        Map<String, String> priorityMap = caseCustomFieldList.stream().collect(Collectors.toMap(FunctionalCaseCustomField::getCaseId, FunctionalCaseCustomField::getValue));
        //构造父子级数据
        buildList(functionalCaseMindDTOList, list, priorityMap, "REVIEW");
        return list;
    }

    public List<FunctionalMinderTreeDTO> getPlanMindFunctionalCase(FunctionalCasePlanMindRequest request, boolean deleted) {
        List<FunctionalMinderTreeDTO> list = new ArrayList<>();
        //查出当前模块下的所有用例,模块可能是根节点，这里做跨项目的处理
        if (request.getModuleId().contains("root")) {
            int i = request.getModuleId().indexOf("_");
            String substring = request.getModuleId().substring(i + 1);
            request.setModuleId(substring);
        }
        List<FunctionalCaseMindDTO> functionalCaseMindDTOList = extFunctionalCaseMapper.getMinderTestPlanList(request, deleted, request.getSortString());
        List<String> fieldIds = getFieldIds(request.getProjectId());
        List<FunctionalCaseCustomField> caseCustomFieldList = extFunctionalCaseMapper.getCaseCustomFieldList(request, deleted, fieldIds);
        Map<String, String> priorityMap = caseCustomFieldList.stream().collect(Collectors.toMap(FunctionalCaseCustomField::getCaseId, FunctionalCaseCustomField::getValue));
        //构造父子级数据
        buildList(functionalCaseMindDTOList, list, priorityMap, "TEST_PLAN");
        return list;
    }


    public List<BaseTreeNode> getTree(FunctionalCaseMindTreeRequest request) {
        List<BaseTreeNode> functionalModuleList = extFunctionalCaseModuleMapper.selectBaseByProjectId(request.getProjectId());
        List<BaseTreeNode> baseTreeNodes = extFunctionalCaseMapper.selectBaseMindNodeByProjectId(request.getProjectId());
        functionalModuleList.addAll(baseTreeNodes);
        return buildTreeAndCountResource(functionalModuleList, true, Translator.get("functional_case.module.default.name"), request.getModuleId());
    }

    public List<BaseTreeNode> buildTreeAndCountResource(List<BaseTreeNode> traverseList, boolean haveVirtualRootNode, String virtualRootName, String moduleId) {

        List<BaseTreeNode> baseTreeNodeList = new ArrayList<>();
        if (haveVirtualRootNode) {
            BaseTreeNode defaultNode = this.getDefaultModule(virtualRootName);
            defaultNode.genModulePath(null);
            baseTreeNodeList.add(defaultNode);
        }
        int lastSize = 0;
        Map<String, BaseTreeNode> baseTreeNodeMap = new HashMap<>();
        while (CollectionUtils.isNotEmpty(traverseList) && traverseList.size() != lastSize) {
            lastSize = traverseList.size();
            List<BaseTreeNode> notMatchedList = new ArrayList<>();
            for (BaseTreeNode treeNode : traverseList) {
                if (!baseTreeNodeMap.containsKey(treeNode.getParentId()) && !StringUtils.equalsIgnoreCase(treeNode.getParentId(), ModuleConstants.ROOT_NODE_PARENT_ID)) {
                    notMatchedList.add(treeNode);
                    continue;
                }
                BaseTreeNode node = new BaseTreeNode(treeNode.getId(), treeNode.getName(), treeNode.getType(), treeNode.getParentId());
                node.genModulePath(baseTreeNodeMap.get(treeNode.getParentId()));
                baseTreeNodeMap.put(treeNode.getId(), node);

                if (StringUtils.equalsIgnoreCase(treeNode.getParentId(), ModuleConstants.ROOT_NODE_PARENT_ID)) {
                    baseTreeNodeList.add(node);
                } else if (baseTreeNodeMap.containsKey(treeNode.getParentId())) {
                    baseTreeNodeMap.get(treeNode.getParentId()).addChild(node);
                }
            }
            traverseList = notMatchedList;
        }

        if (StringUtils.isNotBlank(moduleId)) {
            List<BaseTreeNode> filterList = new ArrayList<>();
            getFilterList(moduleId, baseTreeNodeList, filterList);
            return filterList;
        } else {
            return baseTreeNodeList;
        }
    }

    private static void getFilterList(String moduleId, List<BaseTreeNode> baseTreeNodeList, List<BaseTreeNode> filterList) {
        for (BaseTreeNode baseTreeNode : baseTreeNodeList) {
            if (StringUtils.equalsIgnoreCase(baseTreeNode.getId(), moduleId)) {
                filterList.add(baseTreeNode);
                break;
            } else {
                getFilterList(moduleId, baseTreeNode.getChildren(), filterList);
            }
        }
    }

    public BaseTreeNode getDefaultModule(String name) {
        //默认模块下不允许创建子模块。  它本身也就是叶子节点。
        return new BaseTreeNode(ModuleConstants.DEFAULT_NODE_ID, name, ModuleConstants.NODE_TYPE_DEFAULT, ModuleConstants.ROOT_NODE_PARENT_ID);
    }

    private static void compareStep(byte[] steps, List<FunctionalCaseStepDTO> newCaseSteps) {
        if (steps != null) {
            String historyStepStr = new String(steps, StandardCharsets.UTF_8);
            if (StringUtils.isNotBlank(historyStepStr)) {
                List<FunctionalCaseStepDTO> historySteps = JSON.parseArray(historyStepStr, FunctionalCaseStepDTO.class);
                Map<String, FunctionalCaseStepDTO> historyStepMap = historySteps.stream().collect(Collectors.toMap(FunctionalCaseStepDTO::getId, t -> t));
                newCaseSteps.forEach(newCaseStep -> {
                    setHistoryInfo(newCaseStep, historyStepMap);
                });
            }
        }
    }

    private static void setHistoryInfo(FunctionalCaseStepDTO newCaseStep, Map<String, FunctionalCaseStepDTO> historyStepMap) {
        FunctionalCaseStepDTO historyStep = historyStepMap.get(newCaseStep.getId());
        if (historyStep != null && StringUtils.equals(historyStep.getDesc(), newCaseStep.getDesc()) && StringUtils.equals(historyStep.getResult(), newCaseStep.getResult())) {
            newCaseStep.setExecuteResult(historyStep.getExecuteResult());
            newCaseStep.setActualResult(historyStep.getActualResult());
        }
    }


    public List<FunctionalMinderTreeDTO> getCollectionMindFunctionalCase(FunctionalCaseCollectionMindRequest request, boolean deleted) {
        List<FunctionalMinderTreeDTO> list = new ArrayList<>();
        List<FunctionalCaseMindDTO> functionalCaseMindDTOList = extFunctionalCaseMapper.getMinderCollectionList(request, deleted, request.getSortString());
        List<String> projectIds = functionalCaseMindDTOList.stream().map(FunctionalCaseMindDTO::getProjectId).distinct().toList();
        List<FunctionalCaseCustomField> caseCustomFieldList = new ArrayList<>();
        for (String projectId : projectIds) {
            List<String> fieldIds = getFieldIds(projectId);
            List<FunctionalCaseCustomField> caseCustomFieldListByProjectId = extFunctionalCaseMapper.getCaseCustomFieldListByCollection(request, deleted, fieldIds);
            caseCustomFieldList.addAll(caseCustomFieldListByProjectId);
        }
        Map<String, String> priorityMap = caseCustomFieldList.stream().collect(Collectors.toMap(FunctionalCaseCustomField::getCaseId, FunctionalCaseCustomField::getValue));
        //构造父子级数据
        buildList(functionalCaseMindDTOList, list, priorityMap, "TEST_PLAN");
        return list;
    }
}
