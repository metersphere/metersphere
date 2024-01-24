package io.metersphere.functional.service;

import io.metersphere.bug.domain.BugRelationCaseExample;
import io.metersphere.bug.mapper.BugRelationCaseMapper;
import io.metersphere.functional.constants.CaseEvent;
import io.metersphere.functional.constants.CaseFileSourceType;
import io.metersphere.functional.constants.FunctionalCaseReviewStatus;
import io.metersphere.functional.constants.FunctionalCaseTypeConstants;
import io.metersphere.functional.domain.*;
import io.metersphere.functional.dto.*;
import io.metersphere.functional.excel.domain.FunctionalCaseExcelData;
import io.metersphere.functional.mapper.*;
import io.metersphere.functional.request.*;
import io.metersphere.functional.result.CaseManagementResultCode;
import io.metersphere.plan.domain.TestPlanFunctionalCaseExample;
import io.metersphere.plan.mapper.TestPlanFunctionalCaseMapper;
import io.metersphere.project.domain.FileAssociation;
import io.metersphere.project.domain.ProjectVersion;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.project.mapper.ExtBaseProjectVersionMapper;
import io.metersphere.project.service.ProjectTemplateService;
import io.metersphere.provider.BaseCaseProvider;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.constants.FunctionalCaseExecuteResult;
import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.CustomFieldOption;
import io.metersphere.system.dto.OperationHistoryDTO;
import io.metersphere.system.dto.request.OperationHistoryRequest;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.sdk.TemplateCustomFieldDTO;
import io.metersphere.system.dto.sdk.TemplateDTO;
import io.metersphere.system.dto.sdk.request.PosRequest;
import io.metersphere.system.service.BaseCustomFieldOptionService;
import io.metersphere.system.service.BaseCustomFieldService;
import io.metersphere.system.service.OperationHistoryService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import io.metersphere.system.utils.ServiceUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wx
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class FunctionalCaseService {


    @Resource
    private ExtFunctionalCaseMapper extFunctionalCaseMapper;

    @Resource
    private FunctionalCaseMapper functionalCaseMapper;

    @Resource
    private FunctionalCaseBlobMapper functionalCaseBlobMapper;

    @Resource
    private FunctionalCaseCustomFieldService functionalCaseCustomFieldService;

    @Resource
    private FunctionalCaseAttachmentService functionalCaseAttachmentService;

    @Resource
    private ProjectTemplateService projectTemplateService;

    @Resource
    private FunctionalCaseFollowerMapper functionalCaseFollowerMapper;

    @Resource
    private CaseReviewService caseReviewService;

    @Resource
    SqlSessionFactory sqlSessionFactory;

    @Resource
    private ExtBaseProjectVersionMapper extBaseProjectVersionMapper;

    @Resource
    private FunctionalCaseModuleService functionalCaseModuleService;

    @Resource
    private CaseReviewFunctionalCaseService caseReviewFunctionalCaseService;

    @Resource
    private BaseCustomFieldService baseCustomFieldService;

    @Resource
    private BaseCaseProvider provider;

    @Resource
    private OperationHistoryService operationHistoryService;

    private static final String CASE_MODULE_COUNT_ALL = "all";

    private static final String ADD_FUNCTIONAL_CASE_FILE_LOG_URL = "/functional/case/add";
    private static final String UPDATE_FUNCTIONAL_CASE_FILE_LOG_URL = "/functional/case/update";
    private static final String FUNCTIONAL_CASE_BATCH_COPY_FILE_LOG_URL = "/functional/case/batch/copy";

    @Resource
    private FunctionalCaseDemandMapper functionalCaseDemandMapper;
    @Resource
    private FunctionalCaseTestMapper functionalCaseTestMapper;
    @Resource
    private BugRelationCaseMapper bugRelationCaseMapper;
    @Resource
    private FunctionalCaseRelationshipEdgeMapper functionalCaseRelationshipEdgeMapper;
    @Resource
    private CaseReviewFunctionalCaseMapper caseReviewFunctionalCaseMapper;
    @Resource
    private TestPlanFunctionalCaseMapper testPlanFunctionalCaseMapper;
    @Resource
    private BaseCustomFieldOptionService baseCustomFieldOptionService;


    public FunctionalCase addFunctionalCase(FunctionalCaseAddRequest request, List<MultipartFile> files, String userId) {
        String caseId = IDGenerator.nextStr();
        //添加功能用例
        FunctionalCase functionalCase = addCase(caseId, request, userId);

        //上传文件
        functionalCaseAttachmentService.uploadFile(request.getProjectId(), caseId, files, true, userId);

        //上传副文本里的文件
        functionalCaseAttachmentService.uploadMinioFile(caseId, request.getProjectId(), request.getCaseDetailFileIds(), userId, CaseFileSourceType.CASE_DETAIL.toString());

        //关联附件
        if (CollectionUtils.isNotEmpty(request.getRelateFileMetaIds())) {
            functionalCaseAttachmentService.association(request.getRelateFileMetaIds(), caseId, userId, ADD_FUNCTIONAL_CASE_FILE_LOG_URL, request.getProjectId());
        }

        addCaseReviewCase(request.getReviewId(), caseId, userId);

        return functionalCase;
    }


    /**
     * 添加用例评审和用例关联关系
     *
     * @param reviewId reviewId
     */
    private void addCaseReviewCase(String reviewId, String caseId, String userId) {
        if (StringUtils.isNotBlank(reviewId)) {
            caseReviewService.checkCaseReview(reviewId);
            caseReviewFunctionalCaseService.addCaseReviewFunctionalCase(caseId, userId, reviewId);
        }
    }


    /**
     * 添加功能用例
     *
     * @param request request
     */
    private FunctionalCase addCase(String caseId, FunctionalCaseAddRequest request, String userId) {
        FunctionalCase functionalCase = new FunctionalCase();
        BeanUtils.copyBean(functionalCase, request);
        functionalCase.setId(caseId);
        functionalCase.setNum(getNextNum(request.getProjectId()));
        functionalCase.setReviewStatus(FunctionalCaseReviewStatus.UN_REVIEWED.name());
        functionalCase.setPos(getNextOrder(request.getProjectId()));
        functionalCase.setRefId(caseId);
        functionalCase.setLastExecuteResult(FunctionalCaseExecuteResult.UN_EXECUTED.name());
        functionalCase.setLatest(true);
        functionalCase.setCreateUser(userId);
        functionalCase.setCreateTime(System.currentTimeMillis());
        functionalCase.setUpdateTime(System.currentTimeMillis());
        functionalCase.setVersionId(StringUtils.defaultIfBlank(request.getVersionId(), extBaseProjectVersionMapper.getDefaultVersion(request.getProjectId())));
        functionalCase.setTags(request.getTags());
        functionalCaseMapper.insertSelective(functionalCase);
        //附属表
        FunctionalCaseBlob functionalCaseBlob = new FunctionalCaseBlob();
        functionalCaseBlob.setId(caseId);
        functionalCaseBlob.setSteps(StringUtils.defaultIfBlank(request.getSteps(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        functionalCaseBlob.setTextDescription(StringUtils.defaultIfBlank(request.getTextDescription(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        functionalCaseBlob.setExpectedResult(StringUtils.defaultIfBlank(request.getExpectedResult(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        functionalCaseBlob.setPrerequisite(StringUtils.defaultIfBlank(request.getPrerequisite(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        functionalCaseBlob.setDescription(StringUtils.defaultIfBlank(request.getDescription(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        functionalCaseBlobMapper.insertSelective(functionalCaseBlob);
        //保存自定义字段
        List<CaseCustomFieldDTO> customFields = request.getCustomFields();
        if (CollectionUtils.isNotEmpty(customFields)) {
            customFields = customFields.stream().distinct().collect(Collectors.toList());
            functionalCaseCustomFieldService.saveCustomField(caseId, customFields);
        }
        return functionalCase;
    }


    public Long getNextOrder(String projectId) {
        Long pos = extFunctionalCaseMapper.getPos(projectId);
        return (pos == null ? 0 : pos) + ServiceUtils.POS_STEP;
    }

    public long getNextNum(String projectId) {
        return NumGenerator.nextNum(projectId, ApplicationNumScope.CASE_MANAGEMENT);
    }


    /**
     * 查看用例获取详情
     *
     * @param functionalCaseId functionalCaseId
     * @return FunctionalCaseDetailDTO
     */
    public FunctionalCaseDetailDTO getFunctionalCaseDetail(String functionalCaseId, String userId) {
        FunctionalCase functionalCase = checkFunctionalCase(functionalCaseId);
        FunctionalCaseDetailDTO functionalCaseDetailDTO = new FunctionalCaseDetailDTO();
        BeanUtils.copyBean(functionalCaseDetailDTO, functionalCase);
        FunctionalCaseBlob caseBlob = functionalCaseBlobMapper.selectByPrimaryKey(functionalCaseId);
        functionalCaseDetailDTO.setSteps(new String(caseBlob.getSteps(), StandardCharsets.UTF_8));
        functionalCaseDetailDTO.setTextDescription(new String(caseBlob.getTextDescription(), StandardCharsets.UTF_8));
        functionalCaseDetailDTO.setExpectedResult(new String(caseBlob.getExpectedResult(), StandardCharsets.UTF_8));
        functionalCaseDetailDTO.setPrerequisite(new String(caseBlob.getPrerequisite(), StandardCharsets.UTF_8));
        functionalCaseDetailDTO.setDescription(new String(caseBlob.getDescription(), StandardCharsets.UTF_8));

        //模板校验 获取自定义字段
        checkTemplateCustomField(functionalCaseDetailDTO, functionalCase);

        //是否关注用例
        Boolean isFollow = checkIsFollowCase(functionalCase.getId(), userId);
        functionalCaseDetailDTO.setFollowFlag(isFollow);

        //获取附件信息
        functionalCaseAttachmentService.getAttachmentInfo(functionalCaseDetailDTO);

        List<ProjectVersion> versions = extBaseProjectVersionMapper.getVersionByIds(List.of(functionalCaseDetailDTO.getVersionId()));
        if (CollectionUtils.isNotEmpty(versions)) {
            functionalCaseDetailDTO.setVersionName(versions.get(0).getName());
        }

        //处理已关联需求数量/缺陷数量/用例数量
        handleCount(functionalCaseDetailDTO);

        return functionalCaseDetailDTO;

    }

    private void handleCount(FunctionalCaseDetailDTO functionalCaseDetailDTO) {
        //获取已关联需求数量
        FunctionalCaseDemandExample functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo(functionalCaseDetailDTO.getId());
        functionalCaseDetailDTO.setDemandCount((int) functionalCaseDemandMapper.countByExample(functionalCaseDemandExample));
        //获取已关联用例数量
        FunctionalCaseTestExample caseTestExample = new FunctionalCaseTestExample();
        caseTestExample.createCriteria().andCaseIdEqualTo(functionalCaseDetailDTO.getId());
        functionalCaseDetailDTO.setCaseCount((int) functionalCaseTestMapper.countByExample(caseTestExample));
        //获取已关联缺陷数量
        BugRelationCaseExample bugRelationCaseExample = new BugRelationCaseExample();
        bugRelationCaseExample.createCriteria().andCaseIdEqualTo(functionalCaseDetailDTO.getId());
        functionalCaseDetailDTO.setBugCount((int) bugRelationCaseMapper.countByExample(bugRelationCaseExample));
        //获取已关联依赖关系数量
        FunctionalCaseRelationshipEdgeExample relationshipEdgeExample = new FunctionalCaseRelationshipEdgeExample();
        relationshipEdgeExample.createCriteria()
                .andSourceIdEqualTo(functionalCaseDetailDTO.getId());
        relationshipEdgeExample.or(
                relationshipEdgeExample.createCriteria()
                        .andTargetIdEqualTo(functionalCaseDetailDTO.getId())
        );
        functionalCaseDetailDTO.setRelateEdgeCount((int) functionalCaseRelationshipEdgeMapper.countByExample(relationshipEdgeExample));
        //获取已关联用例评审数量
        CaseReviewFunctionalCaseExample caseReviewFunctionalCaseExample = new CaseReviewFunctionalCaseExample();
        caseReviewFunctionalCaseExample.createCriteria().andCaseIdEqualTo(functionalCaseDetailDTO.getId());
        functionalCaseDetailDTO.setCaseReviewCount((int) caseReviewFunctionalCaseMapper.countByExample(caseReviewFunctionalCaseExample));
        //获取已关联测试计划数量
        TestPlanFunctionalCaseExample testPlanFunctionalCaseExample = new TestPlanFunctionalCaseExample();
        testPlanFunctionalCaseExample.createCriteria().andFunctionalCaseIdEqualTo(functionalCaseDetailDTO.getId());
        functionalCaseDetailDTO.setTestPlanCount((int) testPlanFunctionalCaseMapper.countByExample(testPlanFunctionalCaseExample));

    }


    private Boolean checkIsFollowCase(String caseId, String userId) {
        FunctionalCaseFollowerExample example = new FunctionalCaseFollowerExample();
        example.createCriteria().andCaseIdEqualTo(caseId).andUserIdEqualTo(userId);
        return functionalCaseFollowerMapper.countByExample(example) > 0;
    }


    /**
     * 校验用例是否存在
     *
     * @param functionalCaseId functionalCaseId
     * @return FunctionalCase
     */
    private FunctionalCase checkFunctionalCase(String functionalCaseId) {
        FunctionalCaseExample functionalCaseExample = new FunctionalCaseExample();
        functionalCaseExample.createCriteria().andIdEqualTo(functionalCaseId).andDeletedEqualTo(false);
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(functionalCaseId);
        if (functionalCase == null) {
            throw new MSException(CaseManagementResultCode.FUNCTIONAL_CASE_NOT_FOUND);
        }
        return functionalCase;
    }


    /**
     * 获取模板自定义字段
     *
     * @param functionalCase functionalCase
     */
    private void checkTemplateCustomField(FunctionalCaseDetailDTO functionalCaseDetailDTO, FunctionalCase functionalCase) {
        TemplateDTO templateDTO = projectTemplateService.getTemplateDTOById(functionalCase.getTemplateId(), functionalCase.getProjectId(), TemplateScene.FUNCTIONAL.name());
        if (CollectionUtils.isNotEmpty(templateDTO.getCustomFields())) {
            List<TemplateCustomFieldDTO> customFields = templateDTO.getCustomFields();
            customFields.forEach(item -> {
                FunctionalCaseCustomField caseCustomField = functionalCaseCustomFieldService.getCustomField(item.getFieldId(), functionalCase.getId());
                Optional.ofNullable(caseCustomField).ifPresentOrElse(customField -> {
                    item.setDefaultValue(customField.getValue());
                    if (Translator.get("custom_field.functional_priority").equals(item.getFieldName())) {
                        functionalCaseDetailDTO.setFunctionalPriority(customField.getValue());
                    }
                }, () -> {
                });

            });
            functionalCaseDetailDTO.setCustomFields(customFields);
        }
    }


    /**
     * 更新用例 基本信息
     *
     * @param request request
     * @param files   files
     * @param userId  userId
     * @return FunctionalCase
     */
    public FunctionalCase updateFunctionalCase(FunctionalCaseEditRequest request, List<MultipartFile> files, String userId) {
        FunctionalCase checked = checkFunctionalCase(request.getId());
        FunctionalCaseBlob functionalCaseBlob = functionalCaseBlobMapper.selectByPrimaryKey(request.getId());

        //对于用例模块的变更，同一用例的其他版本用例也需要变更
        if (!StringUtils.equals(checked.getModuleId(), request.getModuleId())) {
            updateFunctionalCaseModule(checked.getRefId(), request.getModuleId());
        }

        //基本信息
        FunctionalCase functionalCase = new FunctionalCase();
        BeanUtils.copyBean(functionalCase, request);
        updateCase(request, userId, functionalCase);

        //处理删除文件id
        if (CollectionUtils.isNotEmpty(request.getDeleteFileMetaIds())) {
            functionalCaseAttachmentService.deleteCaseAttachment(request.getDeleteFileMetaIds(), request.getId(), request.getProjectId());
        }

        //处理取消关联文件id
        if (CollectionUtils.isNotEmpty(request.getUnLinkFilesIds())) {
            functionalCaseAttachmentService.unAssociation(request.getId(), request.getUnLinkFilesIds(), UPDATE_FUNCTIONAL_CASE_FILE_LOG_URL, userId, request.getProjectId());
        }

        //上传新文件
        functionalCaseAttachmentService.uploadFile(request.getProjectId(), request.getId(), files, true, userId);

        //上传副文本文件
        functionalCaseAttachmentService.uploadMinioFile(request.getId(), request.getProjectId(), request.getCaseDetailFileIds(), userId, CaseFileSourceType.CASE_DETAIL.toString());

        //关联新附件
        if (CollectionUtils.isNotEmpty(request.getRelateFileMetaIds())) {
            functionalCaseAttachmentService.association(request.getRelateFileMetaIds(), request.getId(), userId, UPDATE_FUNCTIONAL_CASE_FILE_LOG_URL, request.getProjectId());
        }

        //处理评审状态
        handleReviewStatus(request, functionalCaseBlob, checked.getName());

        return functionalCase;

    }

    private void handleReviewStatus(FunctionalCaseEditRequest request, FunctionalCaseBlob blob, String name) {
        caseReviewFunctionalCaseService.reReviewedCase(request, blob, name);
    }


    /**
     * 多版本所属模块更新处理
     *
     * @param refId    refId
     * @param moduleId moduleId
     */
    private void updateFunctionalCaseModule(String refId, String moduleId) {
        extFunctionalCaseMapper.updateFunctionalCaseModule(refId, moduleId);
    }


    private void updateCase(FunctionalCaseEditRequest request, String userId, FunctionalCase functionalCase) {
        functionalCase.setUpdateUser(userId);
        functionalCase.setUpdateTime(System.currentTimeMillis());
        //更新用例
        functionalCaseMapper.updateByPrimaryKeySelective(functionalCase);
        //更新附属表信息
        FunctionalCaseBlob functionalCaseBlob = new FunctionalCaseBlob();
        functionalCaseBlob.setId(request.getId());
        functionalCaseBlob.setSteps(StringUtils.defaultIfBlank(request.getSteps(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        functionalCaseBlob.setTextDescription(StringUtils.defaultIfBlank(request.getTextDescription(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        functionalCaseBlob.setExpectedResult(StringUtils.defaultIfBlank(request.getExpectedResult(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        functionalCaseBlob.setPrerequisite(StringUtils.defaultIfBlank(request.getPrerequisite(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        functionalCaseBlob.setDescription(StringUtils.defaultIfBlank(request.getDescription(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        functionalCaseBlobMapper.updateByPrimaryKeyWithBLOBs(functionalCaseBlob);

        //更新自定义字段
        List<CaseCustomFieldDTO> customFields = request.getCustomFields();
        if (CollectionUtils.isNotEmpty(customFields)) {
            customFields = customFields.stream().distinct().collect(Collectors.toList());
            functionalCaseCustomFieldService.updateCustomField(request.getId(), customFields);
        }
    }


    /**
     * 关注/取消关注用例
     *
     * @param functionalCaseId functionalCaseId
     * @param userId           userId
     */
    public void editFollower(String functionalCaseId, String userId) {
        checkFunctionalCase(functionalCaseId);
        FunctionalCaseFollowerExample example = new FunctionalCaseFollowerExample();
        example.createCriteria().andCaseIdEqualTo(functionalCaseId).andUserIdEqualTo(userId);
        if (functionalCaseFollowerMapper.countByExample(example) > 0) {
            functionalCaseFollowerMapper.deleteByPrimaryKey(functionalCaseId, userId);
        } else {
            FunctionalCaseFollower functionalCaseFollower = new FunctionalCaseFollower();
            functionalCaseFollower.setCaseId(functionalCaseId);
            functionalCaseFollower.setUserId(userId);
            functionalCaseFollowerMapper.insert(functionalCaseFollower);
        }
    }


    /**
     * 删除用例
     *
     * @param request request
     * @param userId  userId
     */
    public void deleteFunctionalCase(FunctionalCaseDeleteRequest request, String userId) {
        handDeleteFunctionalCase(Collections.singletonList(request.getId()), request.getDeleteAll(), userId);
    }

    private void handDeleteFunctionalCase(List<String> ids, Boolean deleteAll, String userId) {
        Map<String, Object> param = new HashMap<>();
        if (deleteAll) {
            //全部删除  进入回收站
            List<String> refId = extFunctionalCaseMapper.getRefIds(ids, false);
            FunctionalCaseExample functionalCaseExample = new FunctionalCaseExample();
            functionalCaseExample.createCriteria().andRefIdIn(refId);
            List<FunctionalCase> functionalCases = functionalCaseMapper.selectByExample(functionalCaseExample);
            List<String> caseIds = functionalCases.stream().map(FunctionalCase::getId).toList();
            param.put(CaseEvent.Param.CASE_IDS, CollectionUtils.isNotEmpty(caseIds) ? caseIds : new ArrayList<>());
            extFunctionalCaseMapper.batchDelete(refId, userId);
        } else {
            param.put(CaseEvent.Param.CASE_IDS, CollectionUtils.isNotEmpty(ids) ? ids : new ArrayList<>());
            doDelete(ids, userId);
        }
        param.put(CaseEvent.Param.USER_ID, userId);
        param.put(CaseEvent.Param.EVENT_NAME, CaseEvent.Event.DELETE_FUNCTIONAL_CASE);
        provider.updateCaseReview(param);
    }


    private void doDelete(List<String> ids, String userId) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        FunctionalCaseMapper caseMapper = sqlSession.getMapper(FunctionalCaseMapper.class);
        ids.forEach(id -> {
            FunctionalCase functionalCase = new FunctionalCase();
            functionalCase.setDeleted(true);
            functionalCase.setId(id);
            functionalCase.setDeleteUser(userId);
            functionalCase.setDeleteTime(System.currentTimeMillis());
            caseMapper.updateByPrimaryKeySelective(functionalCase);
        });
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
    }


    /**
     * 根据用例id 获取用例是否存在多个版本
     *
     * @param functionalCaseId functionalCaseId
     * @return List<FunctionalCaseVersionDTO>
     */
    public List<FunctionalCaseVersionDTO> getFunctionalCaseVersion(String functionalCaseId) {
        FunctionalCase functionalCase = checkFunctionalCase(functionalCaseId);
        return extFunctionalCaseMapper.getFunctionalCaseByRefId(functionalCase.getRefId());
    }

    /**
     * 列表查询
     *
     * @param request request
     * @return List<FunctionalCasePageDTO>
     */
    public List<FunctionalCasePageDTO> getFunctionalCasePage(FunctionalCasePageRequest request, Boolean deleted) {
        List<FunctionalCasePageDTO> functionalCaseLists = extFunctionalCaseMapper.list(request, deleted);
        if (CollectionUtils.isEmpty(functionalCaseLists)) {
            return new ArrayList<>();
        }
        //处理自定义字段值
        return handleCustomFields(functionalCaseLists);
    }

    private List<FunctionalCasePageDTO> handleCustomFields(List<FunctionalCasePageDTO> functionalCaseLists) {
        List<String> ids = functionalCaseLists.stream().map(FunctionalCasePageDTO::getId).collect(Collectors.toList());
        Map<String, List<FunctionalCaseCustomFieldDTO>> collect = getCaseCustomFiledMap(ids);
        functionalCaseLists.forEach(functionalCasePageDTO -> {
            functionalCasePageDTO.setCustomFields(collect.get(functionalCasePageDTO.getId()));
        });
        return functionalCaseLists;

    }

    public Map<String, List<FunctionalCaseCustomFieldDTO>> getCaseCustomFiledMap(List<String> ids) {
        List<FunctionalCaseCustomFieldDTO> customFields = functionalCaseCustomFieldService.getCustomFieldsByCaseIds(ids);
        customFields.forEach(customField -> {
            if (customField.getInternal()) {
                customField.setFieldName(baseCustomFieldService.translateInternalField(customField.getFieldName()));
            }
        });
        List<String> fieldIds = customFields.stream().map(FunctionalCaseCustomFieldDTO::getFieldId).toList();
        List<CustomFieldOption> fieldOptions = baseCustomFieldOptionService.getByFieldIds(fieldIds);
        Map<String, List<CustomFieldOption>> customOptions = fieldOptions.stream().collect(Collectors.groupingBy(CustomFieldOption::getFieldId));
        customFields.forEach(customField -> {
            customField.setOptions(customOptions.get(customField.getFieldId()));
        });
        return customFields.stream().collect(Collectors.groupingBy(FunctionalCaseCustomFieldDTO::getCaseId));
    }

    public void batchDeleteFunctionalCaseToGc(FunctionalCaseBatchRequest request, String userId) {
        List<String> ids = doSelectIds(request, request.getProjectId());
        if (CollectionUtils.isNotEmpty(ids)) {
            handDeleteFunctionalCase(ids, request.getDeleteAll(), userId);
        }
    }


    public <T> List<String> doSelectIds(T dto, String projectId) {
        BaseFunctionalCaseBatchDTO request = (BaseFunctionalCaseBatchDTO) dto;
        if (request.isSelectAll()) {
            List<String> ids = extFunctionalCaseMapper.getIds(request, projectId, false);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            return ids;
        } else {
            return request.getSelectIds();
        }
    }


    /**
     * 批量移动用例
     *
     * @param request request
     * @param userId  userId
     */
    public void batchMoveFunctionalCase(FunctionalCaseBatchMoveRequest request, String userId) {
        List<String> ids = doSelectIds(request, request.getProjectId());
        if (CollectionUtils.isNotEmpty(ids)) {
            List<String> refId = extFunctionalCaseMapper.getRefIds(ids, false);
            extFunctionalCaseMapper.batchMoveModule(request, refId, userId);
        }
    }

    /**
     * 批量复制用例
     *
     * @param request request
     * @param userId  userId
     */
    @Async
    public void batchCopyFunctionalCase(FunctionalCaseBatchMoveRequest request, String userId) {
        List<String> ids = doSelectIds(request, request.getProjectId());
        if (CollectionUtils.isNotEmpty(ids)) {
            //基本信息
            Map<String, FunctionalCase> functionalCaseMap = copyBaseInfo(request.getProjectId(), ids);
            //大字段
            Map<String, FunctionalCaseBlob> functionalCaseBlobMap = copyBlobInfo(ids);
            //附件 本地附件
            Map<String, List<FunctionalCaseAttachment>> attachmentMap = functionalCaseAttachmentService.getAttachmentByCaseIds(ids);
            //文件库附件
            Map<String, List<FileAssociation>> fileAssociationMap = functionalCaseAttachmentService.getFileAssociationByCaseIds(ids);
            //自定义字段
            Map<String, List<FunctionalCaseCustomField>> customFieldMap = functionalCaseCustomFieldService.getCustomFieldMapByCaseIds(ids);

            Long nextOrder = getNextOrder(request.getProjectId());

            for (String s : ids) {
                String id = IDGenerator.nextStr();
                FunctionalCase functionalCase = functionalCaseMap.get(s);
                FunctionalCaseBlob functionalCaseBlob = functionalCaseBlobMap.get(s);
                List<FunctionalCaseAttachment> caseAttachments = attachmentMap.get(s);
                List<FileAssociation> fileAssociationList = fileAssociationMap.get(s);
                List<FunctionalCaseCustomField> customFields = customFieldMap.get(s);

                Optional.ofNullable(functionalCase).ifPresent(functional -> {
                    functional.setId(id);
                    functional.setRefId(id);
                    functional.setModuleId(request.getModuleId());
                    functional.setNum(getNextNum(request.getProjectId()));
                    functional.setName(getCopyName(functionalCase.getName()));
                    functional.setReviewStatus(FunctionalCaseReviewStatus.UN_REVIEWED.name());
                    functional.setPos(nextOrder + ServiceUtils.POS_STEP);
                    functional.setLastExecuteResult(FunctionalCaseExecuteResult.UN_EXECUTED.name());
                    functional.setCreateUser(userId);
                    functional.setCreateTime(System.currentTimeMillis());
                    functional.setUpdateTime(System.currentTimeMillis());
                    functionalCaseMapper.insert(functional);

                    functionalCaseBlob.setId(id);
                    functionalCaseBlobMapper.insert(functionalCaseBlob);
                });

                if (CollectionUtils.isNotEmpty(caseAttachments)) {
                    caseAttachments.forEach(attachment -> {
                        attachment.setId(IDGenerator.nextStr());
                        attachment.setCaseId(id);
                        attachment.setCreateUser(userId);
                        attachment.setCreateTime(System.currentTimeMillis());
                    });
                    functionalCaseAttachmentService.batchSaveAttachment(caseAttachments);
                }

                if (CollectionUtils.isNotEmpty(customFields)) {
                    customFields.forEach(customField -> {
                        customField.setCaseId(id);
                    });
                    functionalCaseCustomFieldService.batchSaveCustomField(customFields);
                }

                if (CollectionUtils.isNotEmpty(fileAssociationList)) {
                    List<String> fileIds = fileAssociationList.stream().map(FileAssociation::getFileId).collect(Collectors.toList());
                    functionalCaseAttachmentService.association(fileIds, id, userId, FUNCTIONAL_CASE_BATCH_COPY_FILE_LOG_URL, request.getProjectId());
                }

            }
        }
    }

    private String getCopyName(String name) {
        String copyName = "copy_" + name + "_" + UUID.randomUUID().toString().substring(0, 4);
        if (copyName.length() > 255) {
            copyName = copyName.substring(0, 250) + copyName.substring(copyName.length() - 5);
        }
        return copyName;
    }


    public Map<String, FunctionalCaseBlob> copyBlobInfo(List<String> ids) {
        FunctionalCaseBlobExample blobExample = new FunctionalCaseBlobExample();
        blobExample.createCriteria().andIdIn(ids);
        List<FunctionalCaseBlob> functionalCaseBlobs = functionalCaseBlobMapper.selectByExampleWithBLOBs(blobExample);
        return functionalCaseBlobs.stream().collect(Collectors.toMap(FunctionalCaseBlob::getId, functionalCaseBlob -> functionalCaseBlob));
    }

    public Map<String, FunctionalCase> copyBaseInfo(String projectId, List<String> ids) {
        FunctionalCaseExample example = new FunctionalCaseExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andDeletedEqualTo(false).andIdIn(ids);
        List<FunctionalCase> functionalCaseLists = functionalCaseMapper.selectByExample(example);
        return functionalCaseLists.stream().collect(Collectors.toMap(FunctionalCase::getId, functionalCase -> functionalCase));
    }


    /**
     * 批量编辑
     *
     * @param request request
     * @param userId  userId
     */
    public void batchEditFunctionalCase(FunctionalCaseBatchEditRequest request, String userId) {
        List<String> ids = doSelectIds(request, request.getProjectId());
        if (CollectionUtils.isNotEmpty(ids)) {
            //标签处理
            handleTags(request, userId, ids);
            //自定义字段处理
            handleCustomFields(request, userId, ids);
        }

    }

    private void handleCustomFields(FunctionalCaseBatchEditRequest request, String userId, List<String> ids) {
        Optional.ofNullable(request.getCustomField()).ifPresent(customField -> {
            functionalCaseCustomFieldService.batchUpdate(customField, ids);

            FunctionalCase functionalCase = new FunctionalCase();
            functionalCase.setProjectId(request.getProjectId());
            functionalCase.setUpdateTime(System.currentTimeMillis());
            functionalCase.setUpdateUser(userId);
            extFunctionalCaseMapper.batchUpdate(functionalCase, ids);
        });
    }

    private void handleTags(FunctionalCaseBatchEditRequest request, String userId, List<String> ids) {
        if (CollectionUtils.isNotEmpty(request.getTags())) {
            if (request.isAppend()) {
                //追加标签
                List<FunctionalCase> caseList = extFunctionalCaseMapper.getTagsByIds(ids);
                Map<String, FunctionalCase> collect = caseList.stream().collect(Collectors.toMap(FunctionalCase::getId, v -> v));

                SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
                FunctionalCaseMapper caseMapper = sqlSession.getMapper(FunctionalCaseMapper.class);
                ids.forEach(id -> {
                    FunctionalCase functionalCase = new FunctionalCase();
                    if (CollectionUtils.isNotEmpty(collect.get(id).getTags())) {
                        List<String> tags = collect.get(id).getTags();
                        tags.addAll(request.getTags());
                        List<String> newTags = tags.stream().distinct().collect(Collectors.toList());
                        functionalCase.setTags(newTags);
                    } else {
                        functionalCase.setTags(request.getTags());
                    }
                    functionalCase.setId(id);
                    functionalCase.setUpdateTime(System.currentTimeMillis());
                    functionalCase.setUpdateUser(userId);
                    caseMapper.updateByPrimaryKeySelective(functionalCase);
                });
                sqlSession.flushStatements();
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            } else {
                //替换标签
                FunctionalCase functionalCase = new FunctionalCase();
                functionalCase.setTags(request.getTags());
                functionalCase.setProjectId(request.getProjectId());
                functionalCase.setUpdateTime(System.currentTimeMillis());
                functionalCase.setUpdateUser(userId);
                extFunctionalCaseMapper.batchUpdate(functionalCase, ids);
            }
        }

    }

    public Map<String, Long> moduleCount(FunctionalCasePageRequest request, boolean delete) {
        //查出每个模块节点下的资源数量。 不需要按照模块进行筛选
        request.setModuleIds(null);
        List<ModuleCountDTO> moduleCountDTOList = extFunctionalCaseMapper.countModuleIdByRequest(request, delete);
        Map<String, Long> moduleCountMap = functionalCaseModuleService.getModuleCountMap(request.getProjectId(), moduleCountDTOList);
        //查出全部用例数量
        long allCount = extFunctionalCaseMapper.caseCount(request, delete);
        moduleCountMap.put(CASE_MODULE_COUNT_ALL, allCount);
        return moduleCountMap;

    }

    public void editPos(PosRequest request) {
        ServiceUtils.updatePosField(request,
                FunctionalCase.class,
                functionalCaseMapper::selectByPrimaryKey,
                extFunctionalCaseMapper::getPrePos,
                extFunctionalCaseMapper::getLastPos,
                functionalCaseMapper::updateByPrimaryKeySelective);
    }

    public String checkNumExist(String num, String projectId) {
        FunctionalCaseExample example = new FunctionalCaseExample();
        example.createCriteria().andNumEqualTo(Long.valueOf(num)).andProjectIdEqualTo(projectId).andDeletedEqualTo(false);
        List<FunctionalCase> functionalCases = functionalCaseMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(functionalCases)) {
            return functionalCases.get(0).getId();
        }
        return null;
    }


    /**
     * 导入新建数据
     *
     * @param list            导入数据集合
     * @param request         request
     * @param moduleTree      模块树
     * @param userId          用户id
     * @param customFieldsMap 当前默认模板的自定义字段
     */
    public void saveImportData(List<FunctionalCaseExcelData> list, FunctionalCaseImportRequest request, List<BaseTreeNode> moduleTree, String userId, Map<String, TemplateCustomFieldDTO> customFieldsMap, Map<String, String> pathMap) {
        //默认模板
        TemplateDTO defaultTemplateDTO = projectTemplateService.getDefaultTemplateDTO(request.getProjectId(), TemplateScene.FUNCTIONAL.name());
        //模块路径
        List<String> modulePath = list.stream().map(FunctionalCaseExcelData::getModule).toList();
        //构建模块树
        Map<String, String> caseModulePathMap = functionalCaseModuleService.createCaseModule(modulePath, request.getProjectId(), moduleTree, userId, pathMap);

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        FunctionalCaseMapper caseMapper = sqlSession.getMapper(FunctionalCaseMapper.class);
        FunctionalCaseBlobMapper caseBlobMapper = sqlSession.getMapper(FunctionalCaseBlobMapper.class);
        FunctionalCaseCustomFieldMapper customFieldMapper = sqlSession.getMapper(FunctionalCaseCustomFieldMapper.class);
        Long nextOrder = getNextOrder(request.getProjectId());
        for (int i = 0; i < list.size(); i++) {
            parseInsertDataToModule(list.get(i), request, userId, caseModulePathMap, defaultTemplateDTO, nextOrder, caseMapper, caseBlobMapper, customFieldMapper, customFieldsMap);
        }
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
    }

    private void parseInsertDataToModule(FunctionalCaseExcelData functionalCaseExcelData, FunctionalCaseImportRequest request, String userId, Map<String, String> caseModulePathMap, TemplateDTO defaultTemplateDTO, Long nextOrder,
                                         FunctionalCaseMapper caseMapper, FunctionalCaseBlobMapper caseBlobMapper, FunctionalCaseCustomFieldMapper customFieldMapper, Map<String, TemplateCustomFieldDTO> customFieldsMap) {
        //构建用例
        FunctionalCase functionalCase = new FunctionalCase();
        String caseId = IDGenerator.nextStr();
        functionalCase.setId(caseId);
        functionalCase.setNum(getNextNum(request.getProjectId()));
        functionalCase.setModuleId(caseModulePathMap.get(functionalCaseExcelData.getModule()));
        functionalCase.setProjectId(request.getProjectId());
        functionalCase.setTemplateId(defaultTemplateDTO.getId());
        functionalCase.setName(functionalCaseExcelData.getName());
        functionalCase.setReviewStatus(FunctionalCaseReviewStatus.UN_REVIEWED.name());
        functionalCase.setTags(handleImportTags(functionalCaseExcelData.getTags()));
        functionalCase.setCaseEditType(StringUtils.defaultIfBlank(functionalCaseExcelData.getCaseEditType(), FunctionalCaseTypeConstants.CaseEditType.TEXT.name()));
        functionalCase.setPos(nextOrder + ServiceUtils.POS_STEP);
        functionalCase.setVersionId(request.getVersionId());
        functionalCase.setRefId(caseId);
        functionalCase.setLastExecuteResult(FunctionalCaseExecuteResult.UN_EXECUTED.name());
        functionalCase.setLatest(true);
        functionalCase.setCreateUser(userId);
        functionalCase.setCreateTime(System.currentTimeMillis());
        functionalCase.setUpdateTime(System.currentTimeMillis());
        caseMapper.insertSelective(functionalCase);

        //用例附属表
        FunctionalCaseBlob caseBlob = new FunctionalCaseBlob();
        caseBlob.setId(caseId);
        caseBlob.setSteps(StringUtils.defaultIfBlank(functionalCaseExcelData.getSteps(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        caseBlob.setTextDescription(StringUtils.defaultIfBlank(functionalCaseExcelData.getTextDescription(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        caseBlob.setExpectedResult(StringUtils.defaultIfBlank(functionalCaseExcelData.getExpectedResult(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        caseBlob.setPrerequisite(StringUtils.defaultIfBlank(functionalCaseExcelData.getPrerequisite(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        caseBlob.setDescription(StringUtils.defaultIfBlank(functionalCaseExcelData.getDescription(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        caseBlobMapper.insertSelective(caseBlob);

        //自定义字段
        handleImportCustomField(functionalCaseExcelData, caseId, customFieldMapper, customFieldsMap);
    }


    /**
     * 处理导入标签
     *
     * @param tags 标签
     * @return
     */
    private List<String> handleImportTags(String tags) {
        List<String> split = List.of(tags.split("[,;]"));
        return split.stream().map(String::trim).filter(StringUtils::isNotEmpty).collect(Collectors.toList());
    }


    /**
     * 处理导入自定义字段
     *
     * @param functionalCaseExcelData 导入数据
     * @param caseId                  用例id
     * @param customFieldMapper       自定义字段mapper
     * @param customFieldsMap         当前默认模板的自定义字段
     */
    private void handleImportCustomField(FunctionalCaseExcelData functionalCaseExcelData, String caseId, FunctionalCaseCustomFieldMapper customFieldMapper, Map<String, TemplateCustomFieldDTO> customFieldsMap) {
        //需要保存的自定义字段
        Map<String, Object> customData = functionalCaseExcelData.getCustomData();
        customData.forEach((k, v) -> {
            if (customFieldsMap.containsKey(k)) {
                TemplateCustomFieldDTO templateCustomFieldDTO = customFieldsMap.get(k);
                FunctionalCaseCustomField caseCustomField = new FunctionalCaseCustomField();
                caseCustomField.setCaseId(caseId);
                caseCustomField.setFieldId(templateCustomFieldDTO.getFieldId());
                caseCustomField.setValue(v.toString());
                customFieldMapper.insertSelective(caseCustomField);
            }
        });
    }


    /**
     * 导入更新数据
     *
     * @param updateList      更新数据集合
     * @param request         request
     * @param moduleTree      模块树
     * @param userId          用户id
     * @param customFieldsMap 当前默认模板的自定义字段
     */
    public void updateImportData(List<FunctionalCaseExcelData> updateList, FunctionalCaseImportRequest request, List<BaseTreeNode> moduleTree, String userId, Map<String, TemplateCustomFieldDTO> customFieldsMap, Map<String, String> pathMap) {
        //默认模板
        TemplateDTO defaultTemplateDTO = projectTemplateService.getDefaultTemplateDTO(request.getProjectId(), TemplateScene.FUNCTIONAL.name());
        //模块路径
        List<String> modulePath = updateList.stream().map(FunctionalCaseExcelData::getModule).toList();
        //构建模块树
        Map<String, String> caseModulePathMap = functionalCaseModuleService.createCaseModule(modulePath, request.getProjectId(), moduleTree, userId, pathMap);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        FunctionalCaseMapper caseMapper = sqlSession.getMapper(FunctionalCaseMapper.class);
        FunctionalCaseBlobMapper caseBlobMapper = sqlSession.getMapper(FunctionalCaseBlobMapper.class);
        FunctionalCaseCustomFieldMapper customFieldMapper = sqlSession.getMapper(FunctionalCaseCustomFieldMapper.class);
        for (int i = 0; i < updateList.size(); i++) {
            parseUpdateDataToModule(updateList.get(i), request, userId, caseModulePathMap, defaultTemplateDTO, caseMapper, caseBlobMapper, customFieldMapper, customFieldsMap);
        }

        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
    }

    private void parseUpdateDataToModule(FunctionalCaseExcelData functionalCaseExcelData, FunctionalCaseImportRequest request, String userId, Map<String, String> caseModulePathMap, TemplateDTO defaultTemplateDTO, FunctionalCaseMapper caseMapper, FunctionalCaseBlobMapper caseBlobMapper, FunctionalCaseCustomFieldMapper customFieldMapper, Map<String, TemplateCustomFieldDTO> customFieldsMap) {
        //用例表
        FunctionalCase functionalCase = caseMapper.selectByPrimaryKey(functionalCaseExcelData.getNum());
        functionalCase.setName(functionalCaseExcelData.getName());
        functionalCase.setModuleId(caseModulePathMap.get(functionalCaseExcelData.getModule()));
        functionalCase.setTags(handleImportTags(functionalCaseExcelData.getTags()));
        functionalCase.setCaseEditType(StringUtils.defaultIfBlank(functionalCaseExcelData.getCaseEditType(), FunctionalCaseTypeConstants.CaseEditType.TEXT.name()));
        //模板
        functionalCase.setTemplateId(defaultTemplateDTO.getId());
        functionalCase.setVersionId(request.getVersionId());
        functionalCase.setUpdateUser(userId);
        functionalCase.setUpdateTime(System.currentTimeMillis());
        caseMapper.updateByPrimaryKeySelective(functionalCase);

        //用例附属表
        FunctionalCaseBlob caseBlob = new FunctionalCaseBlob();
        caseBlob.setId(functionalCase.getId());
        caseBlob.setSteps(StringUtils.defaultIfBlank(functionalCaseExcelData.getSteps(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        caseBlob.setTextDescription(StringUtils.defaultIfBlank(functionalCaseExcelData.getTextDescription(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        caseBlob.setExpectedResult(StringUtils.defaultIfBlank(functionalCaseExcelData.getExpectedResult(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        caseBlob.setPrerequisite(StringUtils.defaultIfBlank(functionalCaseExcelData.getPrerequisite(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        caseBlob.setDescription(StringUtils.defaultIfBlank(functionalCaseExcelData.getDescription(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        caseBlobMapper.updateByPrimaryKeyWithBLOBs(caseBlob);

        //自定义字段
        handleUpdateCustomField(functionalCaseExcelData, functionalCase.getId(), customFieldMapper, customFieldsMap);

    }

    private void handleUpdateCustomField(FunctionalCaseExcelData functionalCaseExcelData, String caseId, FunctionalCaseCustomFieldMapper customFieldMapper, Map<String, TemplateCustomFieldDTO> customFieldsMap) {
        FunctionalCaseCustomFieldExample fieldExample = new FunctionalCaseCustomFieldExample();
        fieldExample.createCriteria().andCaseIdEqualTo(caseId);
        customFieldMapper.deleteByExample(fieldExample);
        handleImportCustomField(functionalCaseExcelData, caseId, customFieldMapper, customFieldsMap);
    }

    /**
     * 功能用例变更历史
     * @param request request
     * @return List<OperationHistoryDTO>
     */
    public List<OperationHistoryDTO> getOperationHistoryList(OperationHistoryRequest request) {
        List<OperationHistoryDTO> operationHistoryList = operationHistoryService.list(request);
        if (CollectionUtils.isNotEmpty(operationHistoryList)) {
            List<String> caseIds = operationHistoryList.stream()
                    .map(OperationHistoryDTO::getSourceId).toList();

            Map<String, String> caseMap = extFunctionalCaseMapper.selectOptionByIds(caseIds).stream()
                    .collect(Collectors.toMap(OptionDTO::getId, OptionDTO::getName));

            operationHistoryList.forEach(item -> item.setVersionName(caseMap.getOrDefault(item.getSourceId(), StringUtils.EMPTY)));
        }

        return operationHistoryList;
    }
}
