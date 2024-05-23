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
import io.metersphere.project.utils.NodeSortUtils;
import io.metersphere.sdk.constants.ExecStatus;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.CustomField;
import io.metersphere.system.domain.CustomFieldExample;
import io.metersphere.system.domain.User;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.sdk.enums.MoveTypeEnum;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import io.metersphere.system.mapper.CustomFieldMapper;
import io.metersphere.system.mapper.ExtCheckOwnerMapper;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.service.CommonNoticeSendService;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
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
    private ExtCheckOwnerMapper extCheckOwnerMapper;

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
    private FunctionalCaseLogService functionalCaseLogService;

    @Resource
    private FunctionalCaseNoticeService functionalCaseNoticeService;

    private static final String FUNCTIONAL_CASE = "functional_case";
    private static final String FUNCTIONAL_CASE_MODULE = "functional_case_module";
    private static final String CHECK_OWNER_CASE = "check_owner_case";

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
        //构造父子级数据
        buildList(functionalCaseMindDTOList, list);
        return list;
    }

    private void buildList(List<FunctionalCaseMindDTO> functionalCaseMindDTOList, List<FunctionalMinderTreeDTO> list) {
        //构造父子级数据
        for (FunctionalCaseMindDTO functionalCaseMindDTO : functionalCaseMindDTOList) {
            FunctionalMinderTreeDTO root = new FunctionalMinderTreeDTO();
            FunctionalMinderTreeNodeDTO rootData = new FunctionalMinderTreeNodeDTO();
            rootData.setId(functionalCaseMindDTO.getId());
            rootData.setPos(functionalCaseMindDTO.getPos());
            rootData.setText(functionalCaseMindDTO.getName());
            rootData.setPriority(functionalCaseMindDTO.getPriority());
            rootData.setStatus(functionalCaseMindDTO.getReviewStatus());
            rootData.setResource(List.of(Translator.get("minder_extra_node.case")));
            List<FunctionalMinderTreeDTO> children = buildChildren(functionalCaseMindDTO);
            root.setChildren(children);
            root.setData(rootData);
            list.add(root);
        }
    }

    private List<FunctionalMinderTreeDTO> buildChildren(FunctionalCaseMindDTO functionalCaseMindDTO) {
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
            if (StringUtils.isNotBlank(textDescription) ||  StringUtils.isNotBlank(expectedResultText)) {
                children.add(stepFunctionalMinderTreeDTO);
            }
        }

        int i = 1;
        if (StringUtils.equalsIgnoreCase(functionalCaseMindDTO.getCaseEditType(), FunctionalCaseTypeConstants.CaseEditType.STEP.name()) && functionalCaseMindDTO.getSteps() != null) {
            String stepText = new String(functionalCaseMindDTO.getSteps(), StandardCharsets.UTF_8);
            if(StringUtils.isNotBlank(stepText)){
                List<FunctionalCaseStepDTO> functionalCaseStepDTOS = JSON.parseArray(stepText, FunctionalCaseStepDTO.class);
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
        return children;
    }

    @NotNull
    private static FunctionalMinderTreeDTO getFunctionalMinderTreeDTO(String text, String resource, Long pos) {
        FunctionalMinderTreeDTO functionalMinderTreeDTO = new FunctionalMinderTreeDTO();
        FunctionalMinderTreeNodeDTO rootData = new FunctionalMinderTreeNodeDTO();
        rootData.setText(text);
        rootData.setPos(pos);
        rootData.setResource(List.of(resource));
        functionalMinderTreeDTO.setChildren(new ArrayList<>());
        functionalMinderTreeDTO.setData(rootData);
        return functionalMinderTreeDTO;
    }

    private void checkPermission(List<String> sourceIds, String tableName, String userId) {
        if (CollectionUtils.isNotEmpty(sourceIds)) {
            if (!extCheckOwnerMapper.checkoutOwner(tableName, userId, sourceIds)) {
                throw new MSException(Translator.get(CHECK_OWNER_CASE));
            }
        }
    }

    public void editFunctionalCaseBatch(FunctionalCaseMinderEditRequest request, String userId) {
        //处理删除的模块和用例
        deleteResource(request, userId);

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        FunctionalCaseMapper caseMapper = sqlSession.getMapper(FunctionalCaseMapper.class);
        FunctionalCaseBlobMapper caseBlobMapper = sqlSession.getMapper(FunctionalCaseBlobMapper.class);
        FunctionalCaseCustomFieldMapper caseCustomFieldMapper = sqlSession.getMapper(FunctionalCaseCustomFieldMapper.class);
        FunctionalCaseModuleMapper moduleMapper = sqlSession.getMapper(FunctionalCaseModuleMapper.class);

        List<LogDTO> addLogDTOS = new ArrayList<>();
        List<FunctionalCaseDTO> noticeList = new ArrayList<>();
        List<FunctionalCaseDTO> updateNoticeList = new ArrayList<>();
        List<LogDTO> updateLogDTOS = new ArrayList<>();
        //处理用例
        if (CollectionUtils.isNotEmpty(request.getUpdateCaseList())) {
            Map<String, List<FunctionalCaseChangeRequest>> resourceMap = request.getUpdateCaseList().stream().collect(Collectors.groupingBy(FunctionalCaseChangeRequest::getType));
            //处理新增
            Map<String, String> customFieldNameMap = getCustomFieldNameMap(request);
            List<FunctionalCaseChangeRequest> addList = resourceMap.get(OperationLogType.ADD.toString());
            List<FunctionalCase> updatePosList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(addList)) {
                Map<String, List<FunctionalCase>> moduleCaseMap = getModuleCaseMap(addList);
                for (FunctionalCaseChangeRequest functionalCaseChangeRequest : addList) {
                    FunctionalCase functionalCase = addCase(request, userId, functionalCaseChangeRequest, caseMapper);
                    String caseId = functionalCase.getId();
                    //附属表
                    FunctionalCaseBlob functionalCaseBlob = addCaseBlob(functionalCaseChangeRequest, caseId, caseBlobMapper);
                    //保存自定义字段
                    List<FunctionalCaseCustomField> functionalCaseCustomFields = addCustomFields(functionalCaseChangeRequest, caseId, caseCustomFieldMapper);
                    //排序
                    reSetMap(functionalCaseChangeRequest, moduleCaseMap, functionalCase);
                    FunctionalCaseHistoryLogDTO historyLogDTO = new FunctionalCaseHistoryLogDTO(functionalCase, functionalCaseBlob, functionalCaseCustomFields, new ArrayList<>(), new ArrayList<>());
                    addLog(request, userId, caseId, historyLogDTO, addLogDTOS, null);
                    FunctionalCaseDTO functionalCaseDTO = getFunctionalCaseDTO(functionalCase, functionalCaseCustomFields, customFieldNameMap);
                    noticeList.add(functionalCaseDTO);
                }
                moduleCaseMap.forEach((k, v) -> {
                    updatePosList.addAll(v);
                });
            }
            //处理更新
            List<FunctionalCaseChangeRequest> updateList = resourceMap.get(OperationLogType.UPDATE.toString());
            if (CollectionUtils.isNotEmpty(updateList)) {
                List<String> caseIds = updateList.stream().map(FunctionalCaseChangeRequest::getId).toList();
                FunctionalCaseCustomFieldExample example = new FunctionalCaseCustomFieldExample();
                example.createCriteria().andCaseIdIn(caseIds);
                List<FunctionalCaseCustomField> allFields = functionalCaseCustomFieldMapper.selectByExample(example);
                Map<String, List<FunctionalCaseCustomField>> caseCustomFieldMap = allFields.stream().collect(Collectors.groupingBy(FunctionalCaseCustomField::getCaseId));
                Map<String, List<FunctionalCase>> moduleCaseMap = getModuleCaseMap(updateList);
                FunctionalCaseExample functionalCaseExample = new FunctionalCaseExample();
                functionalCaseExample.createCriteria().andIdIn(caseIds);
                List<FunctionalCase> oldCase = functionalCaseMapper.selectByExample(functionalCaseExample);
                Map<String, FunctionalCase> oldCaseMap = oldCase.stream().collect(Collectors.toMap(FunctionalCase::getId, t -> t));
                FunctionalCaseBlobExample functionalCaseBlobExample = new FunctionalCaseBlobExample();
                List<FunctionalCaseBlob> functionalCaseBlobs = functionalCaseBlobMapper.selectByExample(functionalCaseBlobExample);
                Map<String, FunctionalCaseBlob> oldBlobMap = functionalCaseBlobs.stream().collect(Collectors.toMap(FunctionalCaseBlob::getId, t -> t));

                for (FunctionalCaseChangeRequest functionalCaseChangeRequest : updateList) {
                    //基本信息
                    String caseId = functionalCaseChangeRequest.getId();
                    FunctionalCase functionalCase = updateCase(request, userId, caseMapper);
                    //更新附属表信息
                    FunctionalCaseBlob functionalCaseBlob = updateBlob(functionalCaseChangeRequest, caseId, caseBlobMapper);
                    //更新自定义字段
                    List<FunctionalCaseCustomField> functionalCaseCustomFields = updateCustomFields(functionalCaseChangeRequest, caseCustomFieldMap, caseId, caseCustomFieldMapper);
                    //排序
                    if (StringUtils.isNotBlank(functionalCaseChangeRequest.getMoveMode())) {
                        reSetMap(functionalCaseChangeRequest, moduleCaseMap, functionalCase);
                    }
                    FunctionalCaseHistoryLogDTO historyLogDTO = new FunctionalCaseHistoryLogDTO(functionalCase, functionalCaseBlob, caseCustomFieldMap.get(caseId), new ArrayList<>(), new ArrayList<>());
                    FunctionalCaseHistoryLogDTO old = new FunctionalCaseHistoryLogDTO(oldCaseMap.get(caseId), oldBlobMap.get(caseId), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
                    addLog(request, userId, caseId, historyLogDTO, updateLogDTOS, old);
                    FunctionalCaseDTO functionalCaseDTO = getFunctionalCaseDTO(functionalCase, functionalCaseCustomFields, customFieldNameMap);
                    updateNoticeList.add(functionalCaseDTO);

                }
                moduleCaseMap.forEach((k, v) -> {
                    updatePosList.addAll(v);
                });
            }
            //批量排序
            batchSort(updatePosList, caseMapper);

        }

        //处理模块
        if (CollectionUtils.isNotEmpty(request.getUpdateModuleList())) {
            List<FunctionalCaseModule> updatePosList = new ArrayList<>();
            //处理新增
            Map<String, List<FunctionalCaseModuleEditRequest>> resourceMap = request.getUpdateModuleList().stream().collect(Collectors.groupingBy(FunctionalCaseModuleEditRequest::getType));
            List<FunctionalCaseModuleEditRequest> addList = resourceMap.get(OperationLogType.ADD.toString());
            if (CollectionUtils.isNotEmpty(addList)) {
                Map<String, List<FunctionalCaseModule>> parentModuleMap = getParentModuleMap(addList);
                for (FunctionalCaseModuleEditRequest functionalCaseModuleEditRequest : addList) {
                    checkModules(functionalCaseModuleEditRequest, parentModuleMap);
                    FunctionalCaseModule functionalCaseModule = addModule(request, userId, functionalCaseModuleEditRequest, moduleMapper);
                    reSetModuleMap(functionalCaseModuleEditRequest, parentModuleMap, functionalCaseModule);
                }
                parentModuleMap.forEach((k, v) -> {
                    updatePosList.addAll(v);
                });

            }
            //处理更新
            List<FunctionalCaseModuleEditRequest> updateList = resourceMap.get(OperationLogType.UPDATE.toString());
            if (CollectionUtils.isNotEmpty(updateList)) {
                Map<String, List<FunctionalCaseModule>> parentModuleMap = getParentModuleMap(addList);
                for (FunctionalCaseModuleEditRequest functionalCaseModuleEditRequest : updateList) {
                    checkModules(functionalCaseModuleEditRequest, parentModuleMap);
                    FunctionalCaseModule updateModule = updateModule(userId, functionalCaseModuleEditRequest);
                    reSetModuleMap(functionalCaseModuleEditRequest, parentModuleMap, updateModule);
                }
                parentModuleMap.forEach((k, v) -> {
                    updatePosList.addAll(v);
                });
            }
            //批量排序
            batchSortModule(updatePosList, moduleMapper);
        }

        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);

        Project project = projectMapper.selectByPrimaryKey(request.getProjectId());
        for (LogDTO addLogDTO : addLogDTOS) {
            addLogDTO.setOrganizationId(project.getOrganizationId());
        }
        for (LogDTO updateLogDTO : updateLogDTOS) {
            updateLogDTO.setOrganizationId(project.getOrganizationId());
        }
        operationLogService.batchAdd(addLogDTOS);
        operationLogService.batchAdd(updateLogDTOS);
        User user = userMapper.selectByPrimaryKey(userId);
        List<Map> resources = new ArrayList<>();
        resources.addAll(JSON.parseArray(JSON.toJSONString(noticeList), Map.class));
        commonNoticeSendService.sendNotice(NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK, NoticeConstants.Event.CREATE, resources, user, request.getProjectId());
        resources = new ArrayList<>();
        resources.addAll(JSON.parseArray(JSON.toJSONString(updateNoticeList), Map.class));
        commonNoticeSendService.sendNotice(NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK, NoticeConstants.Event.UPDATE, resources, user, request.getProjectId());
    }

    private static void batchSortModule(List<FunctionalCaseModule> updatePosList, FunctionalCaseModuleMapper moduleMapper) {
        for (FunctionalCaseModule functionalCaseModule : updatePosList) {
            FunctionalCaseModule functionalCaseModuleUpdatePos = new FunctionalCaseModule();
            functionalCaseModuleUpdatePos.setId(functionalCaseModule.getId());
            functionalCaseModuleUpdatePos.setPos(functionalCaseModule.getPos());
            moduleMapper.updateByPrimaryKeySelective(functionalCaseModuleUpdatePos);
        }
    }

    private static void batchSort(List<FunctionalCase> updatePosList, FunctionalCaseMapper caseMapper) {
        if (CollectionUtils.isEmpty(updatePosList)) {
            return;
        }
        for (FunctionalCase functionalCase : updatePosList) {
            FunctionalCase functionalCaseUpdatePos = new FunctionalCase();
            functionalCaseUpdatePos.setId(functionalCase.getId());
            functionalCaseUpdatePos.setPos(functionalCase.getPos());
            caseMapper.updateByPrimaryKeySelective(functionalCaseUpdatePos);
        }
    }

    private static void checkModules(FunctionalCaseModuleEditRequest functionalCaseModuleEditRequest, Map<String, List<FunctionalCaseModule>> parentModuleMap) {
        List<FunctionalCaseModule> functionalCaseModules = parentModuleMap.get(functionalCaseModuleEditRequest.getParentId());
        List<FunctionalCaseModule> sameNameList = functionalCaseModules.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getName(), functionalCaseModuleEditRequest.getName())).toList();
        if (CollectionUtils.isNotEmpty(sameNameList)) {
            throw new MSException(Translator.get("node.name.repeat"));
        }
    }

    /**
     * 获取页面改动的自定义字段
     */
    @NotNull
    private Map<String, String> getCustomFieldNameMap(FunctionalCaseMinderEditRequest request) {
        List<CaseCustomFieldDTO> caseCustomFieldDTOS = new ArrayList<>();
        for (FunctionalCaseChangeRequest caseChangeRequest : request.getUpdateCaseList()) {
            if (CollectionUtils.isNotEmpty(caseChangeRequest.getCustomFields())){
                caseCustomFieldDTOS.addAll(caseChangeRequest.getCustomFields());
            }
        }
        if (CollectionUtils.isEmpty(caseCustomFieldDTOS)){
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

    private static void addLog(FunctionalCaseMinderEditRequest request, String userId, String caseId, FunctionalCaseHistoryLogDTO historyLogDTO, List<LogDTO> addLogDTOS, FunctionalCaseHistoryLogDTO old) {
        LogDTO dto = new LogDTO(
                request.getProjectId(),
                null,
                caseId,
                userId,
                OperationLogType.ADD.name(),
                OperationLogModule.FUNCTIONAL_CASE,
                historyLogDTO.getFunctionalCase().getName());
        dto.setHistory(true);
        dto.setPath("/functional/mind/case/edit");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setModifiedValue(JSON.toJSONBytes(historyLogDTO));
        dto.setOriginalValue(JSON.toJSONBytes(old));
        addLogDTOS.add(dto);
    }

    @NotNull
    private FunctionalCaseModule addModule(FunctionalCaseMinderEditRequest request, String userId, FunctionalCaseModuleEditRequest functionalCaseModuleEditRequest, FunctionalCaseModuleMapper moduleMapper) {
        FunctionalCaseModule functionalCaseModule = new FunctionalCaseModule();
        functionalCaseModule.setId(IDGenerator.nextStr());
        functionalCaseModule.setName(functionalCaseModuleEditRequest.getName());
        functionalCaseModule.setParentId(functionalCaseModuleEditRequest.getParentId());
        functionalCaseModule.setProjectId(request.getProjectId());
        functionalCaseModule.setCreateTime(System.currentTimeMillis());
        functionalCaseModule.setUpdateTime(functionalCaseModule.getCreateTime());
        functionalCaseModule.setPos(this.countPos(functionalCaseModule.getParentId()));
        functionalCaseModule.setCreateUser(userId);
        functionalCaseModule.setUpdateUser(userId);
        moduleMapper.insert(functionalCaseModule);
        return functionalCaseModule;
    }

    @NotNull
    private FunctionalCaseModule updateModule(String userId, FunctionalCaseModuleEditRequest functionalCaseModuleEditRequest) {
        FunctionalCaseModule updateModule = new FunctionalCaseModule();
        updateModule.setId(functionalCaseModuleEditRequest.getId());
        updateModule.setName(functionalCaseModuleEditRequest.getName());
        updateModule.setUpdateTime(System.currentTimeMillis());
        updateModule.setUpdateUser(userId);
        updateModule.setCreateUser(null);
        updateModule.setCreateTime(null);
        functionalCaseModuleMapper.updateByPrimaryKeySelective(updateModule);
        return updateModule;
    }

    @NotNull
    private Map<String, List<FunctionalCaseModule>> getParentModuleMap(List<FunctionalCaseModuleEditRequest> addList) {
        List<String> targetIds = addList.stream().map(FunctionalCaseModuleEditRequest::getTargetId).distinct().toList();
        FunctionalCaseModuleExample functionalCaseModuleExample = new FunctionalCaseModuleExample();
        functionalCaseModuleExample.createCriteria().andIdIn(targetIds);
        List<FunctionalCaseModule> functionalCaseModules = functionalCaseModuleMapper.selectByExample(functionalCaseModuleExample);
        List<String> parentIds = functionalCaseModules.stream().map(FunctionalCaseModule::getParentId).distinct().toList();
        functionalCaseModuleExample = new FunctionalCaseModuleExample();
        functionalCaseModuleExample.createCriteria().andParentIdIn(parentIds);
        functionalCaseModules = functionalCaseModuleMapper.selectByExample(functionalCaseModuleExample);
        return functionalCaseModules.stream().collect(Collectors.groupingBy(FunctionalCaseModule::getParentId));
    }

    private Long countPos(String parentId) {
        Long maxPos = extFunctionalCaseModuleMapper.getMaxPosByParentId(parentId);
        if (maxPos == null) {
            return LIMIT_POS;
        } else {
            return maxPos + LIMIT_POS;
        }
    }

    private void reSetModuleMap(FunctionalCaseModuleEditRequest functionalCaseModuleEditRequest, Map<String, List<FunctionalCaseModule>> parentModuleMap, FunctionalCaseModule functionalCaseModule) {
        List<FunctionalCaseModule> functionalCaseModuleInDbList = parentModuleMap.get(functionalCaseModule.getParentId());
        if (CollectionUtils.isEmpty(functionalCaseModuleInDbList)) {
            return;
        }
        List<FunctionalCaseModule> sortList = functionalCaseModuleInDbList.stream().sorted(Comparator.comparing(FunctionalCaseModule::getPos)).toList();
        int j = getModuleIndex(functionalCaseModuleEditRequest, sortList);
        List<FunctionalCaseModule> functionalCaseModules = getFunctionalCaseModules(functionalCaseModuleEditRequest, functionalCaseModule, sortList, j);
        for (int i = 0; i < functionalCaseModules.size(); i++) {
            functionalCaseModules.get(i).setPos(5000L * i);
        }
        parentModuleMap.put(functionalCaseModule.getParentId(), functionalCaseModules);
    }

    @NotNull
    private static List<FunctionalCaseModule> getFunctionalCaseModules(FunctionalCaseModuleEditRequest functionalCaseModuleEditRequest, FunctionalCaseModule functionalCaseModule, List<FunctionalCaseModule> sortList, int j) {
        List<FunctionalCaseModule> finallyModules = new ArrayList<>();
        List<FunctionalCaseModule> beforeModules;
        List<FunctionalCaseModule> afterModules;
        if (StringUtils.equals(functionalCaseModuleEditRequest.getMoveMode(), MoveTypeEnum.AFTER.name())) {
            beforeModules = sortList.subList(0, j + 1);
            afterModules = sortList.subList(j + 1, sortList.size());
        } else {
            beforeModules = sortList.subList(0, j);
            afterModules = sortList.subList(j, sortList.size());
        }
        finallyModules.addAll(beforeModules);
        finallyModules.add(functionalCaseModule);
        finallyModules.addAll(afterModules);
        return finallyModules;
    }

    private static int getModuleIndex(FunctionalCaseModuleEditRequest functionalCaseModuleEditRequest, List<FunctionalCaseModule> sortList) {
        int j = 0;
        for (int i = 0; i < sortList.size(); i++) {
            if (StringUtils.equalsIgnoreCase(sortList.get(i).getId(), functionalCaseModuleEditRequest.getTargetId())) {
                j = i;
                break;
            }
        }
        return j;
    }


    private void reSetMap(FunctionalCaseChangeRequest functionalCaseChangeRequest, Map<String, List<FunctionalCase>> moduleCaseMap, FunctionalCase functionalCase) {
        List<FunctionalCase> functionalCaseInDbList = moduleCaseMap.get(functionalCase.getModuleId());
        if (CollectionUtils.isEmpty(functionalCaseInDbList)) {
            return;
        }
        List<FunctionalCase> sortList = functionalCaseInDbList.stream().sorted(Comparator.comparing(FunctionalCase::getPos)).toList();
        int j = 0;
        j = getIndex(functionalCaseChangeRequest, sortList, j);
        List<FunctionalCase> functionalCases = getFunctionalCases(functionalCaseChangeRequest, sortList, j, functionalCase);
        for (int i = 0; i < functionalCases.size(); i++) {
            functionalCases.get(i).setPos(5000L * i);
        }
        moduleCaseMap.put(functionalCase.getModuleId(), functionalCases);
    }

    @NotNull
    private Map<String, List<FunctionalCase>> getModuleCaseMap(List<FunctionalCaseChangeRequest> addList) {
        List<String> list = addList.stream().map(FunctionalCaseChangeRequest::getTargetId).distinct().toList();
        FunctionalCaseExample functionalCaseExample = new FunctionalCaseExample();
        functionalCaseExample.createCriteria().andIdIn(list);
        List<FunctionalCase> functionalCases = functionalCaseMapper.selectByExample(functionalCaseExample);
        List<String> targetModuleIds = functionalCases.stream().map(FunctionalCase::getModuleId).distinct().toList();
        if (CollectionUtils.isEmpty(targetModuleIds)) {
            return new HashMap<>();
        }
        functionalCaseExample = new FunctionalCaseExample();
        functionalCaseExample.createCriteria().andModuleIdIn(targetModuleIds);
        List<FunctionalCase> functionalCasesByModule = functionalCaseMapper.selectByExample(functionalCaseExample);
        return functionalCasesByModule.stream().collect(Collectors.groupingBy(FunctionalCase::getModuleId));
    }

    private FunctionalCase updateCase(FunctionalCaseMinderEditRequest request, String userId, FunctionalCaseMapper caseMapper) {
        FunctionalCase functionalCase = new FunctionalCase();
        BeanUtils.copyBean(functionalCase, request);
        functionalCase.setUpdateUser(userId);
        functionalCase.setUpdateTime(System.currentTimeMillis());
        //更新用例
        caseMapper.updateByPrimaryKeySelective(functionalCase);
        return functionalCase;
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

    private List<FunctionalCaseCustomField> updateCustomFields(FunctionalCaseChangeRequest functionalCaseChangeRequest, Map<String, List<FunctionalCaseCustomField>> caseCustomFieldMap, String caseId, FunctionalCaseCustomFieldMapper caseCustomFieldMapper) {
        List<FunctionalCaseCustomField> total = new ArrayList<>();
        List<FunctionalCaseCustomField> functionalCaseCustomFields = caseCustomFieldMap.get(caseId);
        List<CaseCustomFieldDTO> customFields = functionalCaseChangeRequest.getCustomFields();
        if (CollectionUtils.isNotEmpty(customFields)) {
            customFields = customFields.stream().distinct().collect(Collectors.toList());
            List<String> fieldIds = customFields.stream().map(CaseCustomFieldDTO::getFieldId).collect(Collectors.toList());
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
                List<FunctionalCaseCustomField> functionalCaseCustomFields1 = saveCustomField(caseId, caseCustomFieldMapper, addFields);
                total.addAll(functionalCaseCustomFields1);
            }
            ;
            if (CollectionUtils.isNotEmpty(updateFields)) {
                List<FunctionalCaseCustomField> functionalCaseCustomFields1 = updateField(updateFields, caseId, caseCustomFieldMapper);
                total.addAll(functionalCaseCustomFields1);
            }
        }
        return total;
    }

    @NotNull
    private static List<FunctionalCase> getFunctionalCases(FunctionalCaseChangeRequest functionalCaseChangeRequest, List<FunctionalCase> sortList, int j, FunctionalCase functionalCase) {
        List<FunctionalCase> finallyCases = new ArrayList<>();
        List<FunctionalCase> beforeCases;
        List<FunctionalCase> afterCases;
        if (StringUtils.equals(functionalCaseChangeRequest.getMoveMode(), MoveTypeEnum.AFTER.name())) {
            beforeCases = sortList.subList(0, j + 1);
            afterCases = sortList.subList(j + 1, sortList.size());
        } else {
            beforeCases = sortList.subList(0, j);
            afterCases = sortList.subList(j, sortList.size());
        }
        finallyCases.addAll(beforeCases);
        finallyCases.add(functionalCase);
        finallyCases.addAll(afterCases);
        return finallyCases;
    }

    private static int getIndex(FunctionalCaseChangeRequest functionalCaseChangeRequest, List<FunctionalCase> sortList, int j) {
        for (int i = 0; i < sortList.size(); i++) {
            if (StringUtils.equalsIgnoreCase(sortList.get(i).getId(), functionalCaseChangeRequest.getTargetId())) {
                j = i;
                break;
            }
        }
        return j;
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

    private List<FunctionalCaseCustomField> addCustomFields(FunctionalCaseChangeRequest functionalCaseChangeRequest, String caseId, FunctionalCaseCustomFieldMapper caseCustomFieldMapper) {
        List<CaseCustomFieldDTO> customFields = functionalCaseChangeRequest.getCustomFields();
        List<FunctionalCaseCustomField> caseCustomFields = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(customFields)) {
            customFields = customFields.stream().distinct().collect(Collectors.toList());
            caseCustomFields = saveCustomField(caseId, caseCustomFieldMapper, customFields);
        }
        return caseCustomFields;
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
    private FunctionalCase addCase(FunctionalCaseMinderEditRequest request, String userId, FunctionalCaseChangeRequest functionalCaseChangeRequest, FunctionalCaseMapper caseMapper) {
        FunctionalCase functionalCase = new FunctionalCase();
        BeanUtils.copyBean(functionalCase, functionalCaseChangeRequest);
        String caseId = IDGenerator.nextStr();
        functionalCase.setId(caseId);
        functionalCase.setProjectId(request.getProjectId());
        functionalCase.setVersionId(request.getVersionId());
        functionalCase.setNum(functionalCaseService.getNextNum(request.getProjectId()));
        functionalCase.setReviewStatus(FunctionalCaseReviewStatus.UN_REVIEWED.name());
        functionalCase.setPos(functionalCaseService.getNextOrder(functionalCase.getProjectId()));
        functionalCase.setRefId(caseId);
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

    private void deleteResource(FunctionalCaseMinderEditRequest request, String userId) {
        if (CollectionUtils.isNotEmpty(request.getDeleteResourceList())) {
            User user = userMapper.selectByPrimaryKey(userId);
            Map<String, List<MinderOptionDTO>> resourceMap = request.getDeleteResourceList().stream().collect(Collectors.groupingBy(MinderOptionDTO::getType));
            List<MinderOptionDTO> caseOptionDTOS = resourceMap.get(Translator.get("minder_extra_node.case"));
            if (CollectionUtils.isNotEmpty(caseOptionDTOS)) {
                List<String> caseIds = caseOptionDTOS.stream().map(MinderOptionDTO::getId).toList();
                checkPermission(caseIds, FUNCTIONAL_CASE, userId);
                functionalCaseService.handDeleteFunctionalCase(caseIds, false, userId, request.getProjectId());
                functionalCaseLogService.batchDeleteFunctionalCaseLogByIds(caseIds, "/functional/mind/case/edit");
                functionalCaseNoticeService.batchSendNotice(request.getProjectId(), caseIds, user, NoticeConstants.Event.DELETE);

            }
            List<MinderOptionDTO> caseModuleOptionDTOS = resourceMap.get(Translator.get("minder_extra_node.module"));
            if (CollectionUtils.isNotEmpty(caseModuleOptionDTOS)) {
                List<String> moduleIds = caseModuleOptionDTOS.stream().map(MinderOptionDTO::getId).toList();
                checkPermission(moduleIds, FUNCTIONAL_CASE_MODULE, userId);
                List<FunctionalCase> functionalCases = functionalCaseModuleService.deleteModuleByIds(moduleIds, new ArrayList<>(), userId);
                functionalCaseModuleService.batchDelLog(functionalCases, request.getProjectId());
                functionalCaseNoticeService.batchSendNotice(request.getProjectId(), functionalCases.stream().map(FunctionalCase::getId).toList(), user, NoticeConstants.Event.DELETE);

            }
        }
    }


    public List<FunctionalMinderTreeDTO> getReviewMindFunctionalCase(FunctionalCaseReviewMindRequest request, boolean deleted, String userId, String viewStatusUserId) {
        List<FunctionalMinderTreeDTO> list = new ArrayList<>();
        //查出当前模块下的所有用例
        List<FunctionalCaseMindDTO> functionalCaseMindDTOList = extFunctionalCaseMapper.getMinderCaseReviewList(request, deleted, userId, viewStatusUserId);
        //构造父子级数据
        buildList(functionalCaseMindDTOList, list);
        return list;
    }


    public List<FunctionalMinderTreeDTO> getPlanMindFunctionalCase(FunctionalCasePlanMindRequest request, boolean deleted) {
        List<FunctionalMinderTreeDTO> list = new ArrayList<>();
        //查出当前模块下的所有用例
        List<FunctionalCaseMindDTO> functionalCaseMindDTOList = extFunctionalCaseMapper.getMinderTestPlanList(request, deleted);
        //构造父子级数据
        buildList(functionalCaseMindDTOList, list);
        return list;
    }


}
