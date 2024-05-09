package io.metersphere.functional.service;

import io.metersphere.functional.constants.FunctionalCaseTypeConstants;
import io.metersphere.functional.constants.MinderLabel;
import io.metersphere.functional.domain.*;
import io.metersphere.functional.dto.*;
import io.metersphere.functional.mapper.*;
import io.metersphere.functional.request.FunctionalCaseBatchMoveRequest;
import io.metersphere.functional.request.FunctionalCaseMindRequest;
import io.metersphere.functional.request.FunctionalCaseMinderEditRequest;
import io.metersphere.functional.request.FunctionalCaseMinderRemoveRequest;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.CustomField;
import io.metersphere.system.domain.CustomFieldExample;
import io.metersphere.system.dto.sdk.enums.MoveTypeEnum;
import io.metersphere.system.mapper.CustomFieldMapper;
import io.metersphere.system.mapper.ExtCheckOwnerMapper;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 功能用例脑图
 *
 * @date : 2023-5-17
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class FunctionalCaseMinderService {


    @Resource
    private FunctionalCaseMapper functionalCaseMapper;

    @Resource
    private ExtFunctionalCaseMapper extFunctionalCaseMapper;

    @Resource
    private FunctionalCaseBlobMapper functionalCaseBlobMapper;

    @Resource
    private ExtFunctionalCaseBlobMapper extFunctionalCaseBlobMapper;

    @Resource
    private FunctionalCaseModuleMapper functionalCaseModuleMapper;

    @Resource
    private ExtFunctionalCaseModuleMapper extFunctionalCaseModuleMapper;

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
    SqlSessionFactory sqlSessionFactory;

    private static final String FUNCTIONAL_CASE = "functional_case";
    private static final String FUNCTIONAL_CASE_MODULE = "functional_case_module";
    private static final String  CHECK_OWNER_CASE = "check_owner_case";
    /**
     * 功能用例-脑图用例列表查询
     *
     * @param deleted 用例是否删除
     * @return FunctionalMinderTreeDTO
     */
    public List<FunctionalMinderTreeDTO> getMindFunctionalCase(FunctionalCaseMindRequest request, boolean deleted) {
        List<FunctionalMinderTreeDTO> list = new ArrayList<>();
        //查出当前模块下的所有用例
        List<FunctionalCaseMindDTO> functionalCaseMindDTOList = extFunctionalCaseMapper.getMinderCaseList(request, deleted);
        //构造父子级数据
        for (FunctionalCaseMindDTO functionalCaseMindDTO : functionalCaseMindDTOList) {
            FunctionalMinderTreeDTO root = new FunctionalMinderTreeDTO();
            FunctionalMinderTreeNodeDTO rootData = new FunctionalMinderTreeNodeDTO();
            rootData.setId(functionalCaseMindDTO.getId());
            rootData.setPos(functionalCaseMindDTO.getPos());
            rootData.setText(functionalCaseMindDTO.getName());
            rootData.setPriority(functionalCaseMindDTO.getPriority());
            rootData.setStatus(functionalCaseMindDTO.getReviewStatus());
            rootData.setResource(List.of(MinderLabel.CASE.toString(), functionalCaseMindDTO.getPriority()));
            List<FunctionalMinderTreeDTO> children = buildChildren(functionalCaseMindDTO);
            root.setChildren(children);
            root.setData(rootData);
            list.add(root);
        }
        return list;
    }

    private List<FunctionalMinderTreeDTO> buildChildren(FunctionalCaseMindDTO functionalCaseMindDTO) {
        List<FunctionalMinderTreeDTO> children = new ArrayList<>();
        if (functionalCaseMindDTO.getPrerequisite() != null) {
            String prerequisiteText = new String(functionalCaseMindDTO.getPrerequisite(), StandardCharsets.UTF_8);
            FunctionalMinderTreeDTO prerequisiteFunctionalMinderTreeDTO = getFunctionalMinderTreeDTO(prerequisiteText, MinderLabel.PREREQUISITE.toString(), 0L);
            children.add(prerequisiteFunctionalMinderTreeDTO);
        }

        if (StringUtils.equalsIgnoreCase(functionalCaseMindDTO.getCaseEditType(), FunctionalCaseTypeConstants.CaseEditType.TEXT.name()) && functionalCaseMindDTO.getTextDescription() != null) {
            String textDescription = new String(functionalCaseMindDTO.getTextDescription(), StandardCharsets.UTF_8);
            FunctionalMinderTreeDTO stepFunctionalMinderTreeDTO = getFunctionalMinderTreeDTO(textDescription, MinderLabel.TEXT_DESCRIPTION.toString(), 1L);
            if (functionalCaseMindDTO.getExpectedResult() != null) {
                String expectedResultText = new String(functionalCaseMindDTO.getExpectedResult(), StandardCharsets.UTF_8);
                FunctionalMinderTreeDTO expectedResultFunctionalMinderTreeDTO = getFunctionalMinderTreeDTO(expectedResultText, MinderLabel.TEXT_EXPECTED_RESULT.toString(), 1L);
                stepFunctionalMinderTreeDTO.getChildren().add(expectedResultFunctionalMinderTreeDTO);
            } else {
                FunctionalMinderTreeDTO expectedResultFunctionalMinderTreeDTO = getFunctionalMinderTreeDTO("", MinderLabel.TEXT_EXPECTED_RESULT.toString(), 1L);
                stepFunctionalMinderTreeDTO.getChildren().add(expectedResultFunctionalMinderTreeDTO);
            }
            children.add(stepFunctionalMinderTreeDTO);
        }

        int i = 1;
        if (StringUtils.equalsIgnoreCase(functionalCaseMindDTO.getCaseEditType(), FunctionalCaseTypeConstants.CaseEditType.STEP.name()) && functionalCaseMindDTO.getSteps() != null) {
            String stepText = new String(functionalCaseMindDTO.getSteps(), StandardCharsets.UTF_8);
            List<FunctionalCaseStepDTO> functionalCaseStepDTOS = JSON.parseArray(stepText, FunctionalCaseStepDTO.class);
            for (FunctionalCaseStepDTO functionalCaseStepDTO : functionalCaseStepDTOS) {
                i = i + 1;
                FunctionalMinderTreeDTO stepFunctionalMinderTreeDTO = getFunctionalMinderTreeDTO(functionalCaseStepDTO.getDesc(), MinderLabel.STEPS.toString(), Long.valueOf(functionalCaseStepDTO.getNum()));
                stepFunctionalMinderTreeDTO.getData().setId(functionalCaseStepDTO.getId());
                FunctionalMinderTreeDTO expectedResultFunctionalMinderTreeDTO;
                if (functionalCaseMindDTO.getExpectedResult() != null) {
                    expectedResultFunctionalMinderTreeDTO = getFunctionalMinderTreeDTO(functionalCaseStepDTO.getResult(), MinderLabel.STEPS_EXPECTED_RESULT.toString(), Long.valueOf(functionalCaseStepDTO.getNum()));
                } else {
                    expectedResultFunctionalMinderTreeDTO = getFunctionalMinderTreeDTO("", MinderLabel.STEPS_EXPECTED_RESULT.toString(), Long.valueOf(functionalCaseStepDTO.getNum()));
                }
                stepFunctionalMinderTreeDTO.getChildren().add(expectedResultFunctionalMinderTreeDTO);
                children.add(stepFunctionalMinderTreeDTO);
            }
        }


        if (functionalCaseMindDTO.getDescription() != null) {
            String descriptionText = new String(functionalCaseMindDTO.getDescription(), StandardCharsets.UTF_8);
            FunctionalMinderTreeDTO descriptionFunctionalMinderTreeDTO = getFunctionalMinderTreeDTO(descriptionText, MinderLabel.DESCRIPTION.toString(), (long) (i + 1));
            children.add(descriptionFunctionalMinderTreeDTO);
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

    public void updateFunctionalCase(FunctionalCaseMinderEditRequest request, String userId) {
        if (StringUtils.isNotBlank(request.getName())) {
            switch (request.getType()) {
                case CASE -> {
                    FunctionalCase functionalCase = new FunctionalCase();
                    functionalCase.setName(request.getName());
                    buildUpdateCaseParam(request.getId(), userId, functionalCase);
                    functionalCaseMapper.updateByPrimaryKeySelective(functionalCase);
                }
                case MODULE -> {
                    FunctionalCaseModule functionalCaseModule = new FunctionalCaseModule();
                    functionalCaseModule.setName(request.getName());
                    buildUpdateCaseModuleParam(request.getId(), userId, functionalCaseModule);
                    functionalCaseModuleMapper.updateByPrimaryKeySelective(functionalCaseModule);
                }
                case PREREQUISITE -> {
                    FunctionalCaseBlob functionalCaseBlob = new FunctionalCaseBlob();
                    functionalCaseBlob.setId(request.getId());
                    functionalCaseBlob.setPrerequisite(StringUtils.defaultIfBlank(request.getName(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
                    functionalCaseBlobMapper.updateByPrimaryKeySelective(functionalCaseBlob);
                }
                case STEPS -> {
                    FunctionalCaseBlob functionalCaseBlob = functionalCaseBlobMapper.selectByPrimaryKey(request.getId());
                    updateSteps(request, functionalCaseBlob);
                }
                case STEPS_EXPECTED_RESULT -> {
                    FunctionalCaseBlob functionalCaseBlob = functionalCaseBlobMapper.selectByPrimaryKey(request.getId());
                    updateStepResult(request, functionalCaseBlob);
                }
                case TEXT_DESCRIPTION -> {
                    FunctionalCaseBlob functionalCaseBlob = new FunctionalCaseBlob();
                    functionalCaseBlob.setId(request.getId());
                    functionalCaseBlob.setTextDescription(StringUtils.defaultIfBlank(request.getName(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
                    functionalCaseBlobMapper.updateByPrimaryKeySelective(functionalCaseBlob);
                }
                case TEXT_EXPECTED_RESULT -> {
                    FunctionalCaseBlob functionalCaseBlob = new FunctionalCaseBlob();
                    functionalCaseBlob.setId(request.getId());
                    functionalCaseBlob.setExpectedResult(StringUtils.defaultIfBlank(request.getName(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
                    functionalCaseBlobMapper.updateByPrimaryKeySelective(functionalCaseBlob);
                }
                case DESCRIPTION -> {
                    FunctionalCaseBlob functionalCaseBlob = new FunctionalCaseBlob();
                    functionalCaseBlob.setId(request.getId());
                    functionalCaseBlob.setDescription(StringUtils.defaultIfBlank(request.getName(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
                    functionalCaseBlobMapper.updateByPrimaryKeySelective(functionalCaseBlob);
                }
                default -> {
                }
            }
        }
        if (StringUtils.isNotBlank(request.getPriority())) {
            CustomFieldExample example = new CustomFieldExample();
            example.createCriteria().andNameEqualTo("functional_priority").andSceneEqualTo("FUNCTIONAL").andScopeIdEqualTo(request.getProjectId());
            List<CustomField> customFields = customFieldMapper.selectByExample(example);
            String field = customFields.get(0).getId();
            FunctionalCaseCustomField customField = new FunctionalCaseCustomField();
            customField.setCaseId(request.getId());
            customField.setFieldId(field);
            customField.setValue(request.getPriority());
            functionalCaseCustomFieldMapper.updateByPrimaryKeySelective(customField);
            FunctionalCase functionalCase = new FunctionalCase();
            buildUpdateCaseParam(request.getId(), userId, functionalCase);
            functionalCaseMapper.updateByPrimaryKeySelective(functionalCase);
        }
    }

    private void updateStepResult(FunctionalCaseMinderEditRequest request, FunctionalCaseBlob functionalCaseBlob) {
        String stepText = new String(functionalCaseBlob.getSteps(), StandardCharsets.UTF_8);
        List<FunctionalCaseStepDTO> functionalCaseStepDTOS = JSON.parseArray(stepText, FunctionalCaseStepDTO.class);
        for (FunctionalCaseStepDTO functionalCaseStepDTO : functionalCaseStepDTOS) {
            if (functionalCaseStepDTO.getNum() == request.getPos().intValue()) {
                functionalCaseStepDTO.setResult(request.getName());
            }
        }
        functionalCaseBlob.setSteps(StringUtils.defaultIfBlank(JSON.toJSONString(functionalCaseStepDTOS), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        functionalCaseBlobMapper.updateByPrimaryKeySelective(functionalCaseBlob);
    }

    private void updateSteps(FunctionalCaseMinderEditRequest request, FunctionalCaseBlob functionalCaseBlob) {
        if (functionalCaseBlob.getSteps() != null) {
            String stepText = new String(functionalCaseBlob.getSteps(), StandardCharsets.UTF_8);
            List<FunctionalCaseStepDTO> functionalCaseStepDTOS = JSON.parseArray(stepText, FunctionalCaseStepDTO.class);
            for (FunctionalCaseStepDTO functionalCaseStepDTO : functionalCaseStepDTOS) {
                if (functionalCaseStepDTO.getNum() == request.getPos().intValue()) {
                    functionalCaseStepDTO.setDesc(request.getName());
                }
            }
            functionalCaseBlob.setSteps(StringUtils.defaultIfBlank(JSON.toJSONString(functionalCaseStepDTOS), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
            functionalCaseBlobMapper.updateByPrimaryKeySelective(functionalCaseBlob);
        }
    }

    private void buildUpdateCaseModuleParam(String id, String userId, FunctionalCaseModule functionalCaseModule) {
        functionalCaseModule.setId(id);
        functionalCaseModule.setUpdateUser(userId);
        functionalCaseModule.setUpdateTime(System.currentTimeMillis());
    }

    private void buildUpdateCaseParam(String id, String userId, FunctionalCase functionalCase) {
        functionalCase.setId(id);
        functionalCase.setUpdateUser(userId);
        functionalCase.setUpdateTime(System.currentTimeMillis());
    }

    public void removeFunctionalCaseBatch(FunctionalCaseMinderRemoveRequest request, String userId) {
        List<MinderOptionDTO> resourceList = request.getResourceList();
        if (CollectionUtils.isEmpty(resourceList)) {
            throw new MSException(Translator.get("node.not_blank"));
        }
        Map<String, List<MinderOptionDTO>> resourceMap = resourceList.stream().collect(Collectors.groupingBy(MinderOptionDTO::getType));
        MinderTargetDTO caseMinderTargetDTO = request.getCaseMinderTargetDTO();
        MinderTargetDTO moduleMinderTargetDTO = request.getModuleMinderTargetDTO();
        List<MinderOptionDTO> caseOptionDTOS = resourceMap.get(MinderLabel.CASE.toString());
        List<String> caseIds = new ArrayList<>();
        caseIds = checkPermission(caseOptionDTOS, caseIds, FUNCTIONAL_CASE, userId);
        List<String> moduleIds = new ArrayList<>();
        List<MinderOptionDTO> moduleOptionDTOS = resourceMap.get(MinderLabel.MODULE.toString());
        moduleIds = checkPermission(moduleOptionDTOS, moduleIds, FUNCTIONAL_CASE_MODULE, userId);
        if (StringUtils.isNotBlank(request.getParentTargetId()) ) {
            //移动到某节点下
            if (!extCheckOwnerMapper.checkoutOwner(FUNCTIONAL_CASE_MODULE, userId, List.of(request.getParentTargetId()))) {
                throw new MSException(Translator.get(CHECK_OWNER_CASE));
            }
            if (CollectionUtils.isNotEmpty(caseIds)) {
                //拖拽到别的节点
                FunctionalCaseBatchMoveRequest functionalCaseBatchMoveRequest = new FunctionalCaseBatchMoveRequest();
                functionalCaseBatchMoveRequest.setModuleId(request.getParentTargetId());
                functionalCaseBatchMoveRequest.setProjectId(request.getProjectId());
                functionalCaseService.batchMoveFunctionalCaseByIds(functionalCaseBatchMoveRequest, userId, caseIds);
                moveSortCase(userId, caseMinderTargetDTO, caseIds);
            }
            if (CollectionUtils.isNotEmpty(moduleIds)) {
                //处理模块拖拽以及移动
                //拖拽到别的节点
                extFunctionalCaseModuleMapper.batchUpdateStringColumn("parent_id", moduleIds, request.getParentTargetId());
                moveSortModule(request.getProjectId(), userId, moduleMinderTargetDTO, moduleIds);
            }
        } else if ( CollectionUtils.isNotEmpty(caseIds)) {
            //直接给用例排序
            moveSortCase(userId, caseMinderTargetDTO, caseIds);

        } else if (CollectionUtils.isNotEmpty(moduleIds)) {
            //直接给模块排序
            moveSortModule(request.getProjectId(), userId, moduleMinderTargetDTO, moduleIds);
        }
        updateSteps(request, resourceList);

    }

    private List<String> checkPermission(List<MinderOptionDTO> caseOptionDTOS, List<String> caseIds, String functionalCase, String userId) {
        if (CollectionUtils.isNotEmpty(caseOptionDTOS)) {
            caseIds = caseOptionDTOS.stream().map(MinderOptionDTO::getId).toList();
            if (!extCheckOwnerMapper.checkoutOwner(functionalCase, userId, caseIds)) {
                throw new MSException(Translator.get(CHECK_OWNER_CASE));
            }
        }
        return caseIds;
    }

    private void moveSortModule(String projectId, String userId, MinderTargetDTO moduleMinderTargetDTO, List<String> moduleIds) {
        if (moduleMinderTargetDTO != null && StringUtils.isNotBlank(moduleMinderTargetDTO.getTargetId())) {
            //排序
            String targetId = moduleMinderTargetDTO.getTargetId();
            FunctionalCaseModule functionalCaseModule = functionalCaseModuleMapper.selectByPrimaryKey(targetId);
            if (functionalCaseModule != null){
                FunctionalCaseModuleExample functionalCaseModuleExample = new FunctionalCaseModuleExample();
                functionalCaseModuleExample.createCriteria().andProjectIdEqualTo(projectId);
                List<FunctionalCaseModule> functionalCaseModules = functionalCaseModuleMapper.selectByExample(functionalCaseModuleExample);
                List<String> ids = new ArrayList<>(functionalCaseModules.stream().map(FunctionalCaseModule::getId).toList());
                List<String> finalIds = getFinalIds(moduleMinderTargetDTO, moduleIds, ids, targetId);
                SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
                FunctionalCaseModuleMapper moduleMapper = sqlSession.getMapper(FunctionalCaseModuleMapper.class);
                for (int i = 0; i < finalIds.size(); i++) {
                    FunctionalCaseModule updateModule = new FunctionalCaseModule();
                    updateModule.setId(finalIds.get(i));
                    updateModule.setPos(5000L * i);
                    updateModule.setUpdateUser(userId);
                    updateModule.setUpdateTime(System.currentTimeMillis());
                    moduleMapper.updateByPrimaryKeySelective(updateModule);
                }
                sqlSession.flushStatements();
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        }
    }

    private void moveSortCase(String userId, MinderTargetDTO caseMinderTargetDTO, List<String> caseIds) {
        if (caseMinderTargetDTO != null && StringUtils.isNotBlank(caseMinderTargetDTO.getTargetId())) {
            String targetId = caseMinderTargetDTO.getTargetId();
            //排序
            FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(targetId);
            if (functionalCase != null && !functionalCase.getDeleted()) {
                FunctionalCaseExample functionalCaseExample = new FunctionalCaseExample();
                functionalCaseExample.createCriteria().andDeletedEqualTo(false).andModuleIdEqualTo(functionalCase.getModuleId()).andProjectIdEqualTo(functionalCase.getProjectId());
                List<FunctionalCase> functionalCases = functionalCaseMapper.selectByExample(functionalCaseExample);
                List<String> ids = new ArrayList<>(functionalCases.stream().map(FunctionalCase::getId).toList());
                List<String> finalIds = getFinalIds(caseMinderTargetDTO, caseIds, ids, targetId);
                SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
                FunctionalCaseMapper caseMapper = sqlSession.getMapper(FunctionalCaseMapper.class);
                for (int i = 0; i < finalIds.size(); i++) {
                    FunctionalCase updateCase = new FunctionalCase();
                    updateCase.setId(finalIds.get(i));
                    updateCase.setPos(5000L * i);
                    updateCase.setUpdateUser(userId);
                    updateCase.setUpdateTime(System.currentTimeMillis());
                    caseMapper.updateByPrimaryKeySelective(updateCase);
                }
                sqlSession.flushStatements();
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        }
    }

    @NotNull
    private static List<String> getFinalIds(MinderTargetDTO minderTargetDTO, List<String> paramIds, List<String> selectIds, String targetId) {
        List<String> finalIds = new ArrayList<>();
        int i1 = selectIds.indexOf(targetId);
        List<String> beforeIds;
        List<String> afterIds;
        if (StringUtils.equals(minderTargetDTO.getMoveMode(), MoveTypeEnum.AFTER.name())) {
            beforeIds = selectIds.subList(0, i1 + 1);
            afterIds = selectIds.subList(i1 + 1, selectIds.size());
        } else {
            beforeIds = selectIds.subList(0, i1);
            afterIds = selectIds.subList(i1, selectIds.size());
        }
        finalIds.addAll(beforeIds);
        finalIds.addAll(paramIds);
        finalIds.addAll(afterIds);
        return finalIds.stream().distinct().toList();
    }

    private void updateSteps(FunctionalCaseMinderRemoveRequest request, List<MinderOptionDTO> resourceList) {
        if (StringUtils.isNotBlank(request.getSteps())) {
            List<FunctionalCaseStepDTO> functionalCaseStepDTOS = JSON.parseArray(request.getSteps(), FunctionalCaseStepDTO.class);
            for (int i = 0; i < functionalCaseStepDTOS.size(); i++) {
                functionalCaseStepDTOS.get(i).setNum(i);
            }
            byte[] bytes = StringUtils.defaultIfBlank(JSON.toJSONString(functionalCaseStepDTOS), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8);
            extFunctionalCaseBlobMapper.batchUpdateColumn("steps", List.of(resourceList.get(0).getId()), bytes);
        }
    }


    public void deleteFunctionalCaseBatch(String projectId, List<MinderOptionDTO> resourceList, String userId) {
        if (CollectionUtils.isEmpty(resourceList)) {
            throw new MSException(Translator.get("node.not_blank"));
        }
        Map<String, List<MinderOptionDTO>> resourceMap = resourceList.stream().collect(Collectors.groupingBy(MinderOptionDTO::getType));

        List<MinderOptionDTO> caseOptionDTOS = resourceMap.get(MinderLabel.CASE.toString());
        if (CollectionUtils.isNotEmpty(caseOptionDTOS)) {
            List<String> caseIds = caseOptionDTOS.stream().map(MinderOptionDTO::getId).toList();
            if (!extCheckOwnerMapper.checkoutOwner(FUNCTIONAL_CASE, userId, caseIds)) {
                throw new MSException(Translator.get(CHECK_OWNER_CASE));
            }
            functionalCaseService.handDeleteFunctionalCase(caseIds, false, userId, projectId);
        }
        List<MinderOptionDTO> caseModuleOptionDTOS = resourceMap.get(MinderLabel.MODULE.toString());
        if (CollectionUtils.isNotEmpty(caseModuleOptionDTOS)) {
            List<String> moduleIds = caseModuleOptionDTOS.stream().map(MinderOptionDTO::getId).toList();
            if (!extCheckOwnerMapper.checkoutOwner(FUNCTIONAL_CASE_MODULE, userId, moduleIds)) {
                throw new MSException(Translator.get(CHECK_OWNER_CASE));
            }
            List<FunctionalCase> functionalCases = functionalCaseModuleService.deleteModuleByIds(moduleIds, new ArrayList<>(), userId);
            functionalCaseModuleService.batchDelLog(functionalCases, projectId);
        }
        List<MinderOptionDTO> prerequisiteOptionDTOS = resourceMap.get(MinderLabel.PREREQUISITE.toString());
        updateBlob(userId, "prerequisite", prerequisiteOptionDTOS);
        List<MinderOptionDTO> descriptionOptionDTOS = resourceMap.get(MinderLabel.DESCRIPTION.toString());
        updateBlob(userId, "description", descriptionOptionDTOS);
        List<MinderOptionDTO> stepOptionDTOS = resourceMap.get(MinderLabel.STEPS.toString());
        if (CollectionUtils.isNotEmpty(stepOptionDTOS)) {
            List<MinderOptionDTO> stepResultOptionDTOS = resourceMap.get(MinderLabel.STEPS_EXPECTED_RESULT.toString());
            stepOptionDTOS.addAll(stepResultOptionDTOS);
            updateBlob(userId, "steps", stepOptionDTOS);
        }
        List<MinderOptionDTO> textOptionDTOS = resourceMap.get(MinderLabel.TEXT_DESCRIPTION.toString());
        updateBlob(userId, "text_description", textOptionDTOS);
        List<MinderOptionDTO> resultOptionDTOS = resourceMap.get(MinderLabel.TEXT_EXPECTED_RESULT.toString());
        updateBlob(userId, "expected_result", resultOptionDTOS);
    }

    private void updateBlob(String userId, String column, List<MinderOptionDTO> preRequisiteOptionDTOS) {
        if (CollectionUtils.isNotEmpty(preRequisiteOptionDTOS)) {
            List<String> caseIds = preRequisiteOptionDTOS.stream().map(MinderOptionDTO::getId).distinct().toList();
            if (!extCheckOwnerMapper.checkoutOwner(FUNCTIONAL_CASE, userId, caseIds)) {
                throw new MSException(Translator.get(CHECK_OWNER_CASE));
            }
            extFunctionalCaseBlobMapper.batchUpdateColumn(column, caseIds, null);
        }
    }

}
