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
import io.metersphere.plan.domain.TestPlanCaseExecuteHistoryExample;
import io.metersphere.plan.domain.TestPlanConfig;
import io.metersphere.plan.domain.TestPlanFunctionalCase;
import io.metersphere.plan.domain.TestPlanFunctionalCaseExample;
import io.metersphere.plan.mapper.TestPlanCaseExecuteHistoryMapper;
import io.metersphere.plan.mapper.TestPlanConfigMapper;
import io.metersphere.plan.mapper.TestPlanFunctionalCaseMapper;
import io.metersphere.project.domain.*;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.project.mapper.ExtBaseProjectVersionMapper;
import io.metersphere.project.mapper.FileAssociationMapper;
import io.metersphere.project.mapper.ProjectApplicationMapper;
import io.metersphere.project.service.ProjectService;
import io.metersphere.project.service.ProjectTemplateService;
import io.metersphere.provider.BaseCaseProvider;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.file.FileCenter;
import io.metersphere.sdk.file.FileCopyRequest;
import io.metersphere.sdk.file.FileRepository;
import io.metersphere.sdk.util.*;
import io.metersphere.system.domain.CustomFieldOption;
import io.metersphere.system.domain.OperationHistoryExample;
import io.metersphere.system.domain.User;
import io.metersphere.system.dto.OperationHistoryDTO;
import io.metersphere.system.dto.request.OperationHistoryRequest;
import io.metersphere.system.dto.sdk.*;
import io.metersphere.system.dto.sdk.request.PosRequest;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import io.metersphere.system.mapper.OperationHistoryMapper;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.service.*;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import io.metersphere.system.utils.ServiceUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private UserLoginService userLoginService;

    @Resource
    private TestPlanConfigMapper testPlanConfigMapper;

    private static final String CASE_MODULE_COUNT_ALL = "all";

    private static final String ADD_FUNCTIONAL_CASE_FILE_LOG_URL = "/functional/case/add";
    private static final String UPDATE_FUNCTIONAL_CASE_FILE_LOG_URL = "/functional/case/update";
    private static final String FUNCTIONAL_CASE_BATCH_COPY_FILE_LOG_URL = "/functional/case/batch/copy";

    private static final String CASE_TABLE = "functional_case";

    @Resource
    private FunctionalCaseDemandMapper functionalCaseDemandMapper;
    @Resource
    private ExtFunctionalCaseTestMapper extFunctionalCaseTestMapper;
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
    @Resource
    private OperationLogService operationLogService;
    @Resource
    private FunctionalCaseCustomFieldMapper functionalCaseCustomFieldMapper;
    @Resource
    private FunctionalCaseAttachmentMapper functionalCaseAttachmentMapper;
    @Resource
    private FileAssociationMapper fileAssociationMapper;
    @Resource
    private CommonNoticeSendService commonNoticeSendService;
    @Resource
    private OperationHistoryService operationHistoryService;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ProjectApplicationMapper projectApplicationMapper;
    @Resource
    private OperationHistoryMapper operationHistoryMapper;

    @Resource
    private CaseReviewHistoryMapper caseReviewHistoryMapper;
    @Resource
    private TestPlanCaseExecuteHistoryMapper testPlanCaseExecuteHistoryMapper;
    @Resource
    private FunctionalCaseCommentMapper functionalCaseCommentMapper;
    @Resource
    private ProjectService projectService;
    @Resource
    private FunctionalCaseNoticeService functionalCaseNoticeService;
    @Resource
    private CaseReviewLogService caseReviewLogService;;

    public FunctionalCase addFunctionalCase(FunctionalCaseAddRequest request, List<MultipartFile> files, String userId, String organizationId) {
        String caseId = IDGenerator.nextStr();
        //添加功能用例
        FunctionalCase functionalCase = addCase(caseId, request, userId);

        //上传文件
        List<String> uploadFileIds = functionalCaseAttachmentService.uploadFile(request.getProjectId(), caseId, files, true, userId);

        //上传富文本里的文件
        filterCaseDetailTmpFile(request);
        functionalCaseAttachmentService.uploadMinioFile(caseId, request.getProjectId(), request.getCaseDetailFileIds(), userId, CaseFileSourceType.CASE_DETAIL.toString());

        //关联附件
        if (CollectionUtils.isNotEmpty(request.getRelateFileMetaIds())) {
            functionalCaseAttachmentService.association(request.getRelateFileMetaIds(), caseId, userId, ADD_FUNCTIONAL_CASE_FILE_LOG_URL, request.getProjectId());
        }

        //处理复制时的已存在的文件
        if (CollectionUtils.isNotEmpty(request.getAttachments())) {
            copyAttachment(request, userId, uploadFileIds, caseId);
        }
        LogUtils.info("保存用例的文件操作完成");
        addCaseReviewCase(request.getReviewId(), functionalCase, userId);

        //记录日志
        FunctionalCaseHistoryLogDTO historyLogDTO = getAddLogModule(functionalCase);
        saveAddDataLog(functionalCase, new FunctionalCaseHistoryLogDTO(), historyLogDTO, userId, organizationId, OperationLogType.ADD.name(), OperationLogModule.CASE_MANAGEMENT_CASE_CASE);

        return functionalCase;
    }

    private void filterCaseDetailTmpFile(FunctionalCaseAddRequest request) {
        // 非用例上传的图片文件不处理
        if (CollectionUtils.isNotEmpty(request.getCaseDetailFileIds())) {
            request.getCaseDetailFileIds().removeIf(tmpFileId -> StringUtils.isNotBlank(request.getDescription()) && !request.getDescription().contains("/attachment/download/file/" + request.getProjectId() + "/" + tmpFileId));
            request.getCaseDetailFileIds().removeIf(tmpFileId -> StringUtils.isNotBlank(request.getTextDescription()) && !request.getTextDescription().contains("/attachment/download/file/" + request.getProjectId() + "/" + tmpFileId));
            request.getCaseDetailFileIds().removeIf(tmpFileId -> StringUtils.isNotBlank(request.getExpectedResult()) && !request.getExpectedResult().contains("/attachment/download/file/" + request.getProjectId() + "/" + tmpFileId));
            request.getCaseDetailFileIds().removeIf(tmpFileId -> StringUtils.isNotBlank(request.getPrerequisite()) && !request.getPrerequisite().contains("/attachment/download/file/" + request.getProjectId() + "/" + tmpFileId));
        }
    }

    private void copyAttachment(FunctionalCaseAddRequest request, String userId, List<String> uploadFileIds, String caseId) {
        //获取用例已经上传的文件ID
        Map<String, FunctionalCaseAttachmentDTO> attachmentDTOMap = request.getAttachments().stream().collect(Collectors.toMap(FunctionalCaseAttachmentDTO::getId, t -> t));
        List<String> attachmentFileIds = request.getAttachments().stream().filter(t -> !t.isDeleted()).map(FunctionalCaseAttachmentDTO::getId).filter(t -> !uploadFileIds.contains(t)).toList();
        if (CollectionUtils.isEmpty(attachmentFileIds)) {
            return;
        }
        FunctionalCaseAttachmentExample functionalCaseAttachmentExample = new FunctionalCaseAttachmentExample();
        functionalCaseAttachmentExample.createCriteria().andCaseIdEqualTo(caseId).andFileIdIn(attachmentFileIds);
        List<FunctionalCaseAttachment> functionalCaseAttachments = functionalCaseAttachmentMapper.selectByExample(functionalCaseAttachmentExample);

        functionalCaseAttachmentExample = new FunctionalCaseAttachmentExample();
        functionalCaseAttachmentExample.createCriteria().andCaseIdNotEqualTo(caseId).andFileIdIn(attachmentFileIds);
        List<FunctionalCaseAttachment> oldFiles = functionalCaseAttachmentMapper.selectByExample(functionalCaseAttachmentExample);
        Map<String, List<FunctionalCaseAttachment>> oldFileMap = oldFiles.stream().collect(Collectors.groupingBy(FunctionalCaseAttachment::getFileId));

        List<String> attachmentFileIdInDBs = functionalCaseAttachments.stream().map(FunctionalCaseAttachment::getFileId).toList();
        List<String> saveAttachmentFileIds = attachmentFileIds.stream().filter(t -> !attachmentFileIdInDBs.contains(t)).toList();
        FileRepository defaultRepository = FileCenter.getDefaultRepository();
        for (String saveAttachmentFileId : saveAttachmentFileIds) {
            FunctionalCaseAttachmentDTO functionalCaseAttachmentDTO = attachmentDTOMap.get(saveAttachmentFileId);
            FunctionalCaseAttachment caseAttachment = functionalCaseAttachmentService.creatAttachment(saveAttachmentFileId, functionalCaseAttachmentDTO.getFileName(), functionalCaseAttachmentDTO.getSize(), caseId, functionalCaseAttachmentDTO.getLocal(), userId);
            if (functionalCaseAttachmentDTO.getLocal()) {
                caseAttachment.setFileSource(CaseFileSourceType.ATTACHMENT.toString());
                LogUtils.info("开始复制文件");
                copyFile(request, caseId, saveAttachmentFileId, oldFileMap, functionalCaseAttachmentDTO, defaultRepository);
            }
            functionalCaseAttachmentMapper.insertSelective(caseAttachment);
        }
    }

    private static void copyFile(FunctionalCaseAddRequest request, String caseId, String saveAttachmentFileId, Map<String, List<FunctionalCaseAttachment>> oldFileMap, FunctionalCaseAttachmentDTO functionalCaseAttachmentDTO, FileRepository defaultRepository) {
        List<FunctionalCaseAttachment> oldFunctionalCaseAttachments = oldFileMap.get(saveAttachmentFileId);
        if (CollectionUtils.isNotEmpty(oldFunctionalCaseAttachments)) {
            FunctionalCaseAttachment functionalCaseAttachment = oldFunctionalCaseAttachments.getFirst();
            // 复制文件
            FileCopyRequest fileCopyRequest = new FileCopyRequest();
            fileCopyRequest.setCopyFolder(DefaultRepositoryDir.getFunctionalCaseDir(request.getProjectId(), functionalCaseAttachment.getCaseId()) + "/" + saveAttachmentFileId);
            fileCopyRequest.setCopyfileName(functionalCaseAttachmentDTO.getFileName());
            fileCopyRequest.setFileName(functionalCaseAttachmentDTO.getFileName());
            fileCopyRequest.setFolder(DefaultRepositoryDir.getFunctionalCaseDir(request.getProjectId(), caseId) + "/" + saveAttachmentFileId);
            // 将文件从上一个用例复制到当前用例资源目录
            try {
                defaultRepository.copyFile(fileCopyRequest);
            } catch (Exception e) {
                LogUtils.error("复制文件失败：{}", e);
            }
        }
    }


    /**
     * 添加用例评审和用例关联关系
     *
     * @param reviewId reviewId
     */
    private void addCaseReviewCase(String reviewId, FunctionalCase functionalCase, String userId) {
        if (StringUtils.isNotBlank(reviewId)) {
            CaseReview caseReview = caseReviewService.checkCaseReview(reviewId);
            caseReviewFunctionalCaseService.addCaseReviewFunctionalCase(functionalCase.getId(), userId, reviewId);
            caseReviewLogService.createCaseAndAssociateLog(caseReview, functionalCase, userId);
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
        functionalCase.setLastExecuteResult(ExecStatus.PENDING.name());
        functionalCase.setLatest(true);
        functionalCase.setCreateUser(userId);
        functionalCase.setUpdateUser(userId);
        functionalCase.setCreateTime(System.currentTimeMillis());
        functionalCase.setUpdateTime(System.currentTimeMillis());
        functionalCase.setVersionId(StringUtils.defaultIfBlank(request.getVersionId(), extBaseProjectVersionMapper.getDefaultVersion(request.getProjectId())));
        functionalCase.setTags(request.getTags());
        functionalCase.setAiCreate(request.getAiCreate());
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
    public FunctionalCaseDetailDTO getFunctionalCaseDetail(String functionalCaseId, String userId, boolean checkDetailCount) {
        FunctionalCase functionalCase = checkFunctionalCase(functionalCaseId);
        FunctionalCaseDetailDTO functionalCaseDetailDTO = new FunctionalCaseDetailDTO();
        BeanUtils.copyBean(functionalCaseDetailDTO, functionalCase);
        FunctionalCaseBlob caseBlob = functionalCaseBlobMapper.selectByPrimaryKey(functionalCaseId);
        functionalCaseDetailDTO.setSteps(new String(caseBlob.getSteps() == null ? new byte[0] : caseBlob.getSteps(), StandardCharsets.UTF_8));
        functionalCaseDetailDTO.setTextDescription(new String(caseBlob.getTextDescription() == null ? new byte[0] : caseBlob.getTextDescription(), StandardCharsets.UTF_8));
        functionalCaseDetailDTO.setExpectedResult(new String(caseBlob.getExpectedResult() == null ? new byte[0] : caseBlob.getExpectedResult(), StandardCharsets.UTF_8));
        functionalCaseDetailDTO.setPrerequisite(new String(caseBlob.getPrerequisite() == null ? new byte[0] : caseBlob.getPrerequisite(), StandardCharsets.UTF_8));
        functionalCaseDetailDTO.setDescription(new String(caseBlob.getDescription() == null ? new byte[0] : caseBlob.getDescription(), StandardCharsets.UTF_8));

        //模板校验 获取自定义字段
        checkTemplateCustomField(functionalCaseDetailDTO, functionalCase);

        //是否关注用例
        Boolean isFollow = checkIsFollowCase(functionalCase.getId(), userId);
        functionalCaseDetailDTO.setFollowFlag(isFollow);

        //获取附件信息
        functionalCaseAttachmentService.getAttachmentInfo(functionalCaseDetailDTO);

        List<ProjectVersion> versions = extBaseProjectVersionMapper.getVersionByIds(List.of(functionalCaseDetailDTO.getVersionId()));
        if (CollectionUtils.isNotEmpty(versions)) {
            functionalCaseDetailDTO.setVersionName(versions.getFirst().getName());
        }


        //模块名称
        handDTO(functionalCaseDetailDTO);

        if (checkDetailCount) {
            //处理已关联需求数量/缺陷数量/用例数量
            handleCount(functionalCaseDetailDTO);
        }

        return functionalCaseDetailDTO;

    }

    /**
     * 处理返回对象的相关属性
     *
     * @param functionalCaseDetailDTO functionalCaseDetailDTO
     */
    private void handDTO(FunctionalCaseDetailDTO functionalCaseDetailDTO) {
        String name = functionalCaseModuleService.getModuleName(functionalCaseDetailDTO.getModuleId());
        functionalCaseDetailDTO.setModuleName(name);

        User user = userMapper.selectByPrimaryKey(functionalCaseDetailDTO.getCreateUser());
        functionalCaseDetailDTO.setCreateUserName(user.getName());
    }

    private void handleCount(FunctionalCaseDetailDTO functionalCaseDetailDTO) {
        //获取已关联需求数量
        FunctionalCaseDemandExample functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo(functionalCaseDetailDTO.getId());
        functionalCaseDetailDTO.setDemandCount((int) functionalCaseDemandMapper.countByExample(functionalCaseDemandExample));
        //获取已关联用例数量
        Integer otherCaseCount = extFunctionalCaseTestMapper.getOtherCaseCount(functionalCaseDetailDTO.getId());
        functionalCaseDetailDTO.setCaseCount(otherCaseCount);
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
        List<TestPlanFunctionalCase> testPlanFunctionalCases = testPlanFunctionalCaseMapper.selectByExample(testPlanFunctionalCaseExample);
        if (CollectionUtils.isNotEmpty(testPlanFunctionalCases)) {
            Map<String, List<TestPlanFunctionalCase>> planMap = testPlanFunctionalCases.stream().collect(Collectors.groupingBy(TestPlanFunctionalCase::getTestPlanId));
            functionalCaseDetailDTO.setTestPlanCount(planMap.size());
        } else {
            functionalCaseDetailDTO.setTestPlanCount(0);
        }

        //获取评论总数量数量
        List<OptionDTO> commentList = new ArrayList<>();
        FunctionalCaseCommentExample functionalCaseCommentExample = new FunctionalCaseCommentExample();
        functionalCaseCommentExample.createCriteria().andCaseIdEqualTo(functionalCaseDetailDTO.getId());
        long caseComment = functionalCaseCommentMapper.countByExample(functionalCaseCommentExample);
        OptionDTO caseOption = new OptionDTO();
        caseOption.setId("caseComment");
        caseOption.setName(String.valueOf(caseComment));
        commentList.add(0, caseOption);
        CaseReviewHistoryExample caseReviewHistoryExample = new CaseReviewHistoryExample();
        caseReviewHistoryExample.createCriteria().andCaseIdEqualTo(functionalCaseDetailDTO.getId());
        long reviewComment = caseReviewHistoryMapper.countByExample(caseReviewHistoryExample);
        OptionDTO reviewOption = new OptionDTO();
        reviewOption.setId("reviewComment");
        reviewOption.setName(String.valueOf(reviewComment));
        commentList.add(1, reviewOption);
        //获取关联测试计划的执行评论数量
        TestPlanCaseExecuteHistoryExample testPlanCaseExecuteHistoryExample = new TestPlanCaseExecuteHistoryExample();
        testPlanCaseExecuteHistoryExample.createCriteria().andCaseIdEqualTo(functionalCaseDetailDTO.getId());
        long testPlanExecuteComment = testPlanCaseExecuteHistoryMapper.countByExample(testPlanCaseExecuteHistoryExample);
        OptionDTO executeOption = new OptionDTO();
        executeOption.setId("executiveComment");
        executeOption.setName(String.valueOf(testPlanExecuteComment));
        commentList.add(2, executeOption);
        functionalCaseDetailDTO.setCommentList(commentList);
        long commentCount = caseComment + reviewComment + testPlanExecuteComment;
        functionalCaseDetailDTO.setCommentCount((int) commentCount);
        //获取变更历史数量数量
        OperationHistoryExample operationHistoryExample = new OperationHistoryExample();
        List<String> types = List.of(OperationLogType.ADD.name(), OperationLogType.IMPORT.name(), OperationLogType.UPDATE.name());
        operationHistoryExample.createCriteria().andSourceIdEqualTo(functionalCaseDetailDTO.getId()).andModuleEqualTo(OperationLogModule.CASE_MANAGEMENT_CASE_CASE).andTypeIn(types);
        functionalCaseDetailDTO.setHistoryCount((int) operationHistoryMapper.countByExample(operationHistoryExample));

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
        List<FunctionalCase> functionalCases = functionalCaseMapper.selectByExample(functionalCaseExample);
        if (CollectionUtils.isEmpty(functionalCases)) {
            throw new MSException(CaseManagementResultCode.FUNCTIONAL_CASE_NOT_FOUND);
        }
        return functionalCases.getFirst();
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
            List<String> fieldIds = customFields.stream().map(TemplateCustomFieldDTO::getFieldId).distinct().toList();
            FunctionalCaseCustomFieldExample example = new FunctionalCaseCustomFieldExample();
            example.createCriteria().andCaseIdEqualTo(functionalCase.getId()).andFieldIdIn(fieldIds);
            List<FunctionalCaseCustomField> functionalCaseCustomFields = functionalCaseCustomFieldMapper.selectByExample(example);
            Map<String, FunctionalCaseCustomField> customFieldMap = functionalCaseCustomFields.stream().collect(Collectors.toMap(FunctionalCaseCustomField::getFieldId, t -> t));
            List<CustomFieldOption> memberCustomOption = projectTemplateService.getMemberOptions(functionalCase.getProjectId());
            customFields.forEach(item -> {
                if (StringUtils.equalsAnyIgnoreCase(item.getType(), CustomFieldType.MEMBER.name(), CustomFieldType.MULTIPLE_MEMBER.name())) {
                    item.setOptions(memberCustomOption);
                }
                FunctionalCaseCustomField caseCustomField = customFieldMap.get(item.getFieldId());
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

        //上传富文本文件
        functionalCaseAttachmentService.uploadMinioFile(request.getId(), request.getProjectId(), request.getCaseDetailFileIds(), userId, CaseFileSourceType.CASE_DETAIL.toString());

        //关联新附件
        if (CollectionUtils.isNotEmpty(request.getRelateFileMetaIds())) {
            functionalCaseAttachmentService.association(request.getRelateFileMetaIds(), request.getId(), userId, UPDATE_FUNCTIONAL_CASE_FILE_LOG_URL, request.getProjectId());
        }

        //处理评审状态
        handleReviewStatus(request, functionalCaseBlob, checked.getName(), userId);

        return functionalCase;

    }

    private void handleReviewStatus(FunctionalCaseEditRequest request, FunctionalCaseBlob blob, String name, String userId) {
        caseReviewFunctionalCaseService.reReviewedCase(request, blob, name, userId);
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
        boolean hasUpdate = false;
        if (request.getSteps() != null) {
            hasUpdate = true;
            functionalCaseBlob.setSteps(StringUtils.defaultIfEmpty(request.getSteps(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        }
        if (request.getTextDescription() != null) {
            hasUpdate = true;
            functionalCaseBlob.setTextDescription(StringUtils.defaultIfEmpty(request.getTextDescription(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        }
        if (request.getExpectedResult() != null) {
            hasUpdate = true;
            functionalCaseBlob.setExpectedResult(StringUtils.defaultIfEmpty(request.getExpectedResult(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        }
        if (request.getPrerequisite() != null) {
            hasUpdate = true;
            functionalCaseBlob.setPrerequisite(StringUtils.defaultIfEmpty(request.getPrerequisite(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        }
        if (request.getDescription() != null) {
            hasUpdate = true;
            functionalCaseBlob.setDescription(StringUtils.defaultIfEmpty(request.getDescription(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        }
        if (hasUpdate) {
            functionalCaseBlobMapper.updateByPrimaryKeyWithBLOBs(functionalCaseBlob);
        }
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
        handDeleteFunctionalCase(Collections.singletonList(request.getId()), request.getDeleteAll(), userId, request.getProjectId());
    }

    public void handDeleteFunctionalCase(List<String> ids, Boolean deleteAll, String userId, String projectId) {
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
        User user = userMapper.selectByPrimaryKey(userId);
        functionalCaseNoticeService.batchSendNotice(projectId, ids, user, NoticeConstants.Event.DELETE);
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
    public List<FunctionalCasePageDTO> getFunctionalCasePage(FunctionalCasePageRequest request, Boolean deleted, Boolean isRepeat) {
        List<FunctionalCasePageDTO> functionalCaseLists = extFunctionalCaseMapper.list(request, deleted, isRepeat);
        if (CollectionUtils.isEmpty(functionalCaseLists)) {
            return new ArrayList<>();
        }
        //处理自定义字段值
        return handleCustomFields(functionalCaseLists, request.getProjectId());
    }

    private List<FunctionalCasePageDTO> handleCustomFields(List<FunctionalCasePageDTO> functionalCaseLists, String projectId) {
        List<String> ids = functionalCaseLists.stream().map(FunctionalCasePageDTO::getId).collect(Collectors.toList());
        Map<String, List<FunctionalCaseCustomFieldDTO>> collect = getCaseCustomFiledMap(ids, projectId);
        Set<String> userIds = extractUserIds(functionalCaseLists);
        Map<String, String> userMap = userLoginService.getUserNameMap(new ArrayList<>(userIds));
        functionalCaseLists.forEach(functionalCasePageDTO -> {
            functionalCasePageDTO.setCustomFields(collect.get(functionalCasePageDTO.getId()));
            functionalCasePageDTO.setCreateUserName(userMap.get(functionalCasePageDTO.getCreateUser()));
            functionalCasePageDTO.setUpdateUserName(userMap.get(functionalCasePageDTO.getUpdateUser()));
            functionalCasePageDTO.setDeleteUserName(userMap.get(functionalCasePageDTO.getDeleteUser()));
        });
        return functionalCaseLists;

    }

    private Set<String> extractUserIds(List<FunctionalCasePageDTO> list) {
        return list.stream()
                .flatMap(functionalCasePageDTO -> Stream.of(functionalCasePageDTO.getUpdateUser(), functionalCasePageDTO.getDeleteUser(), functionalCasePageDTO.getCreateUser()))
                .collect(Collectors.toSet());
    }

    public Map<String, List<FunctionalCaseCustomFieldDTO>> getCaseCustomFiledMap(List<String> ids, String projectId) {
        List<CustomFieldOption> memberCustomOption = projectTemplateService.getMemberOptions(projectId);
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
            if (StringUtils.equalsAnyIgnoreCase(customField.getType(), CustomFieldType.MEMBER.name(), CustomFieldType.MULTIPLE_MEMBER.name())) {
                customField.setOptions(memberCustomOption);
            }
        });
        return customFields.stream().collect(Collectors.groupingBy(FunctionalCaseCustomFieldDTO::getCaseId));
    }

    public void batchDeleteFunctionalCaseToGc(FunctionalCaseBatchRequest request, String userId) {
        List<String> ids = doSelectIds(request, request.getProjectId());
        if (CollectionUtils.isNotEmpty(ids)) {
            handDeleteFunctionalCase(ids, request.getDeleteAll(), userId, request.getProjectId());
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
        batchMoveFunctionalCaseByIds(request, userId, ids);
    }

    public void batchMoveFunctionalCaseByIds(FunctionalCaseBatchMoveRequest request, String userId, List<String> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            User user = userMapper.selectByPrimaryKey(userId);
            List<String> refId = extFunctionalCaseMapper.getRefIds(ids, false);
            extFunctionalCaseMapper.batchMoveModule(request, refId, userId);
            functionalCaseNoticeService.batchSendNotice(request.getProjectId(), ids, user, NoticeConstants.Event.UPDATE);
        }
    }

    /**
     * 批量复制用例
     *
     * @param request request
     * @param userId  userId
     */
    public void batchCopyFunctionalCase(FunctionalCaseBatchMoveRequest request, String userId, String organizationId) {
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

            AtomicReference<Long> nextOrder = new AtomicReference<>(getNextOrder(request.getProjectId()));

            List<FunctionalCase> addList = new ArrayList<>();
            List<FunctionalCaseBlob> addBlobList = new ArrayList<>();
            List<FunctionalCaseAttachment> addAttachMentList = new ArrayList<>();
            List<FunctionalCaseCustomField> addCustomFieldList = new ArrayList<>();
            Map<String, List<String>> addFileAssociationMap = new HashMap<>();
            Map<FunctionalCase, FunctionalCaseHistoryLogDTO> addLogMap = new HashMap<>();

            for (String s : ids) {
                String id = IDGenerator.nextStr();
                FunctionalCase functionalCase = functionalCaseMap.get(s);
                FunctionalCaseBlob functionalCaseBlob = functionalCaseBlobMap.get(s);
                List<FunctionalCaseAttachment> caseAttachments = attachmentMap.get(s);
                List<FileAssociation> fileAssociationList = fileAssociationMap.get(s);
                List<FunctionalCaseCustomField> customFields = customFieldMap.get(s);
                Long num = functionalCase.getNum();

                Optional.ofNullable(functionalCase).ifPresent(functional -> {
                    functional.setId(id);
                    functional.setRefId(id);
                    functional.setModuleId(request.getModuleId());
                    functional.setNum(getNextNum(request.getProjectId()));
                    functional.setName(getCopyName(functionalCase.getName(), num, functional.getNum()));
                    functional.setReviewStatus(FunctionalCaseReviewStatus.UN_REVIEWED.name());
                    functional.setPos(nextOrder.get());
                    functional.setLastExecuteResult(ExecStatus.PENDING.name());
                    functional.setCreateUser(userId);
                    functional.setUpdateUser(userId);
                    functional.setCreateTime(System.currentTimeMillis());
                    functional.setUpdateTime(System.currentTimeMillis());
                    functional.setAiCreate(false);
                    addList.add(functional);

                    functionalCaseBlob.setId(id);
                    addBlobList.add(functionalCaseBlob);
                    nextOrder.updateAndGet(v -> v + ServiceUtils.POS_STEP);
                });

                if (CollectionUtils.isNotEmpty(caseAttachments)) {
                    caseAttachments.forEach(attachment -> {
                        attachment.setId(IDGenerator.nextStr());
                        attachment.setCaseId(id);
                        attachment.setCreateUser(userId);
                        attachment.setCreateTime(System.currentTimeMillis());
                        addAttachMentList.add(attachment);
                    });
                }

                if (CollectionUtils.isNotEmpty(customFields)) {
                    customFields.forEach(customField -> {
                        customField.setCaseId(id);
                        addCustomFieldList.add(customField);
                    });
                }

                if (CollectionUtils.isNotEmpty(fileAssociationList)) {
                    List<String> fileIds = fileAssociationList.stream().map(FileAssociation::getFileId).collect(Collectors.toList());
                    addFileAssociationMap.put(id, fileIds);
                }

                //日志
                FunctionalCaseHistoryLogDTO historyLogDTO = new FunctionalCaseHistoryLogDTO();
                historyLogDTO.setFunctionalCase(functionalCase);
                historyLogDTO.setFunctionalCaseBlob(functionalCaseBlob);
                historyLogDTO.setCustomFields(customFields);
                historyLogDTO.setCaseAttachments(caseAttachments);
                historyLogDTO.setFileAssociationList(fileAssociationList);
                addLogMap.put(functionalCase, historyLogDTO);
            }
            SubListUtils.dealForSubList(addList, 500, subList -> {
                functionalCaseMapper.batchInsert(subList);
            });
            SubListUtils.dealForSubList(addBlobList, 500, subList -> {
                functionalCaseBlobMapper.batchInsert(subList);
            });
            SubListUtils.dealForSubList(addAttachMentList, 500, subList -> {
                functionalCaseAttachmentService.batchSaveAttachment(subList);
            });
            SubListUtils.dealForSubList(addCustomFieldList, 500, subList -> {
                functionalCaseCustomFieldService.batchSaveCustomField(subList);
            });
            addFileAssociationMap.entrySet().forEach(entry -> {
                functionalCaseAttachmentService.association(entry.getValue(), entry.getKey(), userId, FUNCTIONAL_CASE_BATCH_COPY_FILE_LOG_URL, request.getProjectId());
            });
            addLogMap.entrySet().forEach(entry -> {
                saveAddDataLog(entry.getKey(), new FunctionalCaseHistoryLogDTO(), entry.getValue(), userId, organizationId, OperationLogType.ADD.name(), OperationLogModule.CASE_MANAGEMENT_CASE_CASE);
            });

            User user = userMapper.selectByPrimaryKey(userId);
            functionalCaseNoticeService.batchSendNotice(request.getProjectId(), ids, user, NoticeConstants.Event.CREATE);


        }
    }

    private String getCopyName(String name, long oldNum, long newNum) {
        if (!StringUtils.startsWith(name, "copy_")) {
            name = "copy_" + name;
        }
        if (name.length() > 250) {
            name = name.substring(0, 200) + "...";
        }
        if (StringUtils.endsWith(name, "_" + oldNum)) {
            name = StringUtils.substringBeforeLast(name, "_" + oldNum);
        }
        name = name + "_" + newNum;
        return name;
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
            User user = userMapper.selectByPrimaryKey(userId);
            //标签处理
            handleTags(request, userId, ids);
            //自定义字段处理
            handleCustomFields(request, userId, ids);
            //基本信息
            FunctionalCase functionalCase = new FunctionalCase();
            functionalCase.setProjectId(request.getProjectId());
            functionalCase.setUpdateTime(System.currentTimeMillis());
            functionalCase.setUpdateUser(userId);
            extFunctionalCaseMapper.batchUpdate(functionalCase, ids);
            functionalCaseNoticeService.batchSendNotice(request.getProjectId(), ids, user, NoticeConstants.Event.UPDATE);
        }

    }

    private void handleCustomFields(FunctionalCaseBatchEditRequest request, String userId, List<String> ids) {
        boolean customField = Optional.ofNullable(request.getCustomField()).map(o -> o.getFieldId()).isPresent();
        if (customField) {
            functionalCaseCustomFieldService.batchUpdate(request.getCustomField(), ids);
        }
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
                        functionalCase.setTags(ServiceUtils.parseTags(tags));
                    } else {
                        functionalCase.setTags(ServiceUtils.parseTags(request.getTags()));
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
                functionalCase.setTags(request.isClear() ? new ArrayList<>() : ServiceUtils.parseTags(request.getTags()));
                functionalCase.setProjectId(request.getProjectId());
                functionalCase.setUpdateTime(System.currentTimeMillis());
                functionalCase.setUpdateUser(userId);
                extFunctionalCaseMapper.batchUpdate(functionalCase, ids);
            }
        } else if (request.isClear()){
            FunctionalCase functionalCase = new FunctionalCase();
            functionalCase.setTags(new ArrayList<>());
            functionalCase.setProjectId(request.getProjectId());
            functionalCase.setUpdateTime(System.currentTimeMillis());
            functionalCase.setUpdateUser(userId);
            extFunctionalCaseMapper.batchUpdate(functionalCase, ids);
        }


    }

    public Map<String, Long> moduleCount(FunctionalCasePageRequest request, boolean delete) {
        if (StringUtils.isNotEmpty(request.getTestPlanId())) {
            this.checkTestPlanRepeatCase(request);
        }
        //查出每个模块节点下的资源数量。 不需要按照模块进行筛选
        request.setModuleIds(null);
        List<ModuleCountDTO> moduleCountDTOList = extFunctionalCaseMapper.countModuleIdByRequest(request, delete);
        Map<String, Long> moduleCountMap = functionalCaseModuleService.getModuleCountMap(request.getProjectId(), moduleCountDTOList);
        //查出全部用例数量
        AtomicLong allCount = new AtomicLong(0);
        moduleCountDTOList.forEach(item -> allCount.addAndGet(item.getDataCount()));
        moduleCountMap.put(CASE_MODULE_COUNT_ALL, allCount.get());
        return moduleCountMap;

    }

    private void checkTestPlanRepeatCase(FunctionalCasePageRequest request) {
        TestPlanConfig testPlanConfig = testPlanConfigMapper.selectByPrimaryKey(request.getTestPlanId());
        if (testPlanConfig != null && BooleanUtils.isTrue(testPlanConfig.getRepeatCase())) {
            //测试计划允许重复用例，意思就是统计不受测试计划影响。去掉这个条件，
            request.setTestPlanId(null);
        }
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
            return functionalCases.getFirst().getId();
        }
        return null;
    }


    /**
     * 导入新建数据
     *
     * @param list            导入数据集合
     * @param request         request
     * @param moduleTree      模块树
     * @param customFieldsMap 当前默认模板的自定义字段
     * @param pathMap         模块路径
     * @param user            user
     */
    public void saveImportData(List<FunctionalCaseExcelData> list, FunctionalCaseImportRequest request, List<BaseTreeNode> moduleTree, Map<String, TemplateCustomFieldDTO> customFieldsMap, Map<String, String> pathMap, SessionUser user, AtomicLong lastPos) {
        //默认模板
        TemplateDTO defaultTemplateDTO = projectTemplateService.getDefaultTemplateDTO(request.getProjectId(), TemplateScene.FUNCTIONAL.name());
        //模块路径
        List<String> modulePath = list.stream().map(FunctionalCaseExcelData::getModule).toList();
        //构建模块树
        Map<String, String> caseModulePathMap = functionalCaseModuleService.createCaseModule(modulePath, request.getProjectId(), moduleTree, user.getId(), pathMap);

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        FunctionalCaseMapper caseMapper = sqlSession.getMapper(FunctionalCaseMapper.class);
        FunctionalCaseBlobMapper caseBlobMapper = sqlSession.getMapper(FunctionalCaseBlobMapper.class);
        FunctionalCaseCustomFieldMapper customFieldMapper = sqlSession.getMapper(FunctionalCaseCustomFieldMapper.class);
        List<FunctionalCaseDTO> noticeList = new ArrayList<>();
        List<FunctionalCaseHistoryLogDTO> historyLogDTOS = new ArrayList<>();
        List<LogDTO> logDTOS = new ArrayList<>();
        List<String> caseIds = new ArrayList<>();
        Long pos = lastPos.get();
        for (int i = 0; i < list.size(); i++) {
            parseInsertDataToModule(list.get(i), request, user.getId(), caseModulePathMap, defaultTemplateDTO, pos, caseMapper, caseBlobMapper, customFieldMapper, customFieldsMap, caseIds, historyLogDTOS);
            pos -= ServiceUtils.POS_STEP;
            //通知
            noticeModule(noticeList, list.get(i), request, user.getId(), customFieldsMap);
        }
        lastPos.set(pos);
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);

        //新增用例记录日志
        historyLogDTOS.forEach(historyLogDTO -> {
            batchSaveImportData(historyLogDTO, new FunctionalCaseHistoryLogDTO(), user, OperationLogType.IMPORT.name(), OperationLogModule.CASE_MANAGEMENT_CASE_CASE, logDTOS);
        });
        operationLogService.batchAdd(logDTOS);

        List<Map> resources = new ArrayList<>();
        resources.addAll(JSON.parseArray(JSON.toJSONString(noticeList), Map.class));
        commonNoticeSendService.sendNotice(NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK, NoticeConstants.Event.CREATE, resources, user, request.getProjectId());
    }

    /**
     * 导入数据日志 批量
     *
     * @param historyLogDTO
     * @param functionalCaseHistoryLogDTO
     * @param user
     * @param type
     * @param module
     * @param logDTOS
     */
    private void batchSaveImportData(FunctionalCaseHistoryLogDTO historyLogDTO, FunctionalCaseHistoryLogDTO functionalCaseHistoryLogDTO, SessionUser user, String type, String module, List<LogDTO> logDTOS) {
        LogDTO dto = new LogDTO(
                historyLogDTO.getFunctionalCase().getProjectId(),
                user.getLastOrganizationId(),
                historyLogDTO.getFunctionalCase().getId(),
                user.getId(),
                type,
                module,
                historyLogDTO.getFunctionalCase().getName());
        dto.setHistory(true);
        dto.setPath("/functional/case/import/excel");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setModifiedValue(JSON.toJSONBytes(historyLogDTO));
        dto.setOriginalValue(JSON.toJSONBytes(functionalCaseHistoryLogDTO));
        logDTOS.add(dto);
    }

    /**
     * 组装通知数据
     *
     * @param noticeList              xiao
     * @param functionalCaseExcelData
     * @param request
     * @param userId
     */
    private void noticeModule(List<FunctionalCaseDTO> noticeList, FunctionalCaseExcelData functionalCaseExcelData, FunctionalCaseImportRequest request, String userId, Map<String, TemplateCustomFieldDTO> customFieldsMap) {
        FunctionalCaseDTO functionalCaseDTO = new FunctionalCaseDTO();
        functionalCaseDTO.setTriggerMode(Translator.get("log.test_plan.functional_case"));
        functionalCaseDTO.setName(functionalCaseExcelData.getName().toString());
        functionalCaseDTO.setProjectId(request.getProjectId());
        functionalCaseDTO.setCaseEditType(functionalCaseExcelData.getCaseEditType());
        functionalCaseDTO.setCreateUser(userId);
        List<OptionDTO> fields = new ArrayList<>();
        Map<String, Object> customData = functionalCaseExcelData.getCustomData();
        customData.forEach((k, v) -> {
            if (customFieldsMap.containsKey(k)) {
                TemplateCustomFieldDTO templateCustomFieldDTO = customFieldsMap.get(k);
                OptionDTO optionDTO = new OptionDTO();
                optionDTO.setId(templateCustomFieldDTO.getFieldId());
                optionDTO.setName(templateCustomFieldDTO.getFieldName());
                fields.add(optionDTO);
            }
        });
        functionalCaseDTO.setFields(fields);
        noticeList.add(functionalCaseDTO);
    }

    private void parseInsertDataToModule(FunctionalCaseExcelData functionalCaseExcelData, FunctionalCaseImportRequest request, String userId, Map<String, String> caseModulePathMap, TemplateDTO defaultTemplateDTO, Long nextOrder,
                                         FunctionalCaseMapper caseMapper, FunctionalCaseBlobMapper caseBlobMapper, FunctionalCaseCustomFieldMapper customFieldMapper, Map<String, TemplateCustomFieldDTO> customFieldsMap, List<String> caseIds, List<FunctionalCaseHistoryLogDTO> historyLogDTOS) {
        //构建用例
        FunctionalCase functionalCase = new FunctionalCase();
        String caseId = IDGenerator.nextStr();
        functionalCase.setId(caseId);
        functionalCase.setNum(getNextNum(request.getProjectId()));
        functionalCase.setModuleId(caseModulePathMap.get(functionalCaseExcelData.getModule()));
        functionalCase.setProjectId(request.getProjectId());
        functionalCase.setTemplateId(defaultTemplateDTO.getId());
        functionalCase.setName(functionalCaseExcelData.getName().toString());
        functionalCase.setReviewStatus(FunctionalCaseReviewStatus.UN_REVIEWED.name());
        functionalCase.setTags(handleImportTags(functionalCaseExcelData.getTags()));
        functionalCase.setCaseEditType(StringUtils.defaultIfBlank(functionalCaseExcelData.getCaseEditType(), FunctionalCaseTypeConstants.CaseEditType.TEXT.name()));
        functionalCase.setPos(nextOrder);
        functionalCase.setVersionId(request.getVersionId());
        functionalCase.setRefId(caseId);
        functionalCase.setLastExecuteResult(ExecStatus.PENDING.name());
        functionalCase.setLatest(true);
        functionalCase.setCreateUser(userId);
        functionalCase.setUpdateUser(userId);
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
        List<FunctionalCaseCustomField> customFields = handleImportCustomField(functionalCaseExcelData, caseId, customFieldMapper, customFieldsMap, userId);

        caseIds.add(functionalCase.getId());
        historyLogDTOS.add(new FunctionalCaseHistoryLogDTO(functionalCase, caseBlob, customFields, new ArrayList<>(), new ArrayList<>()));

    }


    /**
     * 处理导入标签
     *
     * @param tags 标签
     * @return
     */
    public List<String> handleImportTags(String tags) {
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
    private List<FunctionalCaseCustomField> handleImportCustomField(FunctionalCaseExcelData functionalCaseExcelData, String caseId, FunctionalCaseCustomFieldMapper customFieldMapper, Map<String, TemplateCustomFieldDTO> customFieldsMap, String userId) {
        List<FunctionalCaseCustomField> customFields = new ArrayList<>();
        //需要保存的自定义字段
        Map<String, Object> customData = functionalCaseExcelData.getCustomData();
        customFieldsMap.forEach((k, v) -> {
            //用例等级如果没有默认值，则为P0
            if (StringUtils.equalsIgnoreCase(v.getInternalFieldKey(), "functional_priority") && (v.getDefaultValue() == null || StringUtils.isBlank(v.getDefaultValue().toString()))) {
                v.setDefaultValue("P0");
            }
            Object value = customData.get(k);
            FunctionalCaseCustomField caseCustomField = new FunctionalCaseCustomField();
            caseCustomField.setCaseId(caseId);
            caseCustomField.setFieldId(v.getFieldId());
            Optional.ofNullable(value).ifPresentOrElse(v1 -> {
                if ((v1.toString().length() == 2 && StringUtils.equals(v1.toString(), "[]")) || !StringUtils.isNotBlank(v1.toString())) {
                    setCustomFieldValue(v.getDefaultValue(), caseCustomField);
                } else {
                    setCustomFieldValue(v1, caseCustomField);
                }
            }, () -> {
                setCustomFieldValue(v.getDefaultValue(), caseCustomField);
            });

            if (StringUtils.equalsIgnoreCase(v.getType(), CustomFieldType.MEMBER.name()) && caseCustomField.getValue().contains("CREATE_USER")) {
                caseCustomField.setValue(userId);
            }
            if (StringUtils.equalsIgnoreCase(v.getType(), CustomFieldType.MULTIPLE_MEMBER.name()) && caseCustomField.getValue().contains("CREATE_USER")) {
                caseCustomField.setValue(caseCustomField.getValue().replace("CREATE_USER", userId));
            }
            customFields.add(caseCustomField);
            customFieldMapper.insertSelective(caseCustomField);
        });
        return customFields;
    }

    private void setCustomFieldValue(Object value, FunctionalCaseCustomField caseCustomField) {
        if (value != null && (StringUtils.equalsIgnoreCase(value.toString(), "[]") || value instanceof List)) {
            //数组类型
            caseCustomField.setValue(JSON.toJSONString(value));
        } else {
            caseCustomField.setValue(value == null ? StringUtils.EMPTY : value.toString());
        }
    }


    /**
     * 导入更新数据
     *
     * @param updateList      更新数据集合
     * @param request         request
     * @param moduleTree      模块树
     * @param customFieldsMap 当前默认模板的自定义字段
     * @param pathMap         路径map
     * @param user            用户
     */
    public void updateImportData(List<FunctionalCaseExcelData> updateList, FunctionalCaseImportRequest request, List<BaseTreeNode> moduleTree, Map<String, TemplateCustomFieldDTO> customFieldsMap, Map<String, String> pathMap, SessionUser user) {
        //默认模板
        TemplateDTO defaultTemplateDTO = projectTemplateService.getDefaultTemplateDTO(request.getProjectId(), TemplateScene.FUNCTIONAL.name());
        //模块路径
        List<String> modulePath = updateList.stream().map(FunctionalCaseExcelData::getModule).toList();
        //构建模块树
        Map<String, String> caseModulePathMap = functionalCaseModuleService.createCaseModule(modulePath, request.getProjectId(), moduleTree, user.getId(), pathMap);

        //获取重新提审标识
        ProjectApplicationExample example = new ProjectApplicationExample();
        example.createCriteria().andProjectIdEqualTo(request.getProjectId()).andTypeEqualTo(ProjectApplicationType.CASE.CASE_RE_REVIEW.name());
        List<ProjectApplication> projectApplications = projectApplicationMapper.selectByExample(example);

        List<String> ids = updateList.stream().map(FunctionalCaseExcelData::getNum).toList();
        List<FunctionalCase> caseList = getCaseDataByIds(ids);
        Map<String, List<FunctionalCase>> collect = caseList.stream().collect(Collectors.groupingBy(FunctionalCase::getId));
        List<FunctionalCaseBlob> blobs = getBlobDataByIds(ids);
        Map<String, List<FunctionalCaseBlob>> blobsCollect = blobs.stream().collect(Collectors.groupingBy(FunctionalCaseBlob::getId));
        Map<String, List<FunctionalCaseCustomField>> customFieldMap = functionalCaseCustomFieldService.getCustomFieldMapByCaseIds(ids);

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        FunctionalCaseMapper caseMapper = sqlSession.getMapper(FunctionalCaseMapper.class);
        FunctionalCaseBlobMapper caseBlobMapper = sqlSession.getMapper(FunctionalCaseBlobMapper.class);
        FunctionalCaseCustomFieldMapper customFieldMapper = sqlSession.getMapper(FunctionalCaseCustomFieldMapper.class);
        List<FunctionalCaseDTO> noticeList = new ArrayList<>();
        List<String> caseIds = new ArrayList<>();
        List<FunctionalCaseHistoryLogDTO> historyLogDTOS = new ArrayList<>();
        List<LogDTO> logDTOS = new ArrayList<>();

        for (int i = 0; i < updateList.size(); i++) {
            parseUpdateDataToModule(updateList.get(i), request, user.getId(), caseModulePathMap, defaultTemplateDTO, caseMapper, caseBlobMapper, customFieldMapper, customFieldsMap, collect, historyLogDTOS);
            // 重新提审的数据
            addStatusIds(projectApplications, caseIds, collect, blobsCollect, updateList.get(i));
            //通知
            noticeModule(noticeList, updateList.get(i), request, user.getId(), customFieldsMap);
        }
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);

        //更新用例评审状态
        if (CollectionUtils.isNotEmpty(caseIds)) {
            caseReviewFunctionalCaseService.batchHandleStatusAndHistory(caseIds, user.getId());
        }

        // 日志
        historyLogDTOS.forEach(historyLogDTO -> {
            batchHandImportUpdateLog(historyLogDTO, collect, blobsCollect, customFieldMap, logDTOS, user);
        });
        operationLogService.batchAdd(logDTOS);

        List<Map> resources = new ArrayList<>();
        resources.addAll(JSON.parseArray(JSON.toJSONString(noticeList), Map.class));
        commonNoticeSendService.sendNotice(NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK, NoticeConstants.Event.UPDATE, resources, user, request.getProjectId());
    }

    private void batchHandImportUpdateLog(FunctionalCaseHistoryLogDTO newData, Map<String, List<FunctionalCase>> collect, Map<String, List<FunctionalCaseBlob>> blobsCollect, Map<String, List<FunctionalCaseCustomField>> customFieldMap, List<LogDTO> logDTOS, SessionUser user) {
        FunctionalCase oldCase = collect.get(newData.getFunctionalCase().getId()).getFirst();
        FunctionalCaseBlob oldBlod = blobsCollect.get(newData.getFunctionalCase().getId()).getFirst();
        List<FunctionalCaseCustomField> oldCustomFields = customFieldMap.get(newData.getFunctionalCase().getId());
        batchSaveImportData(newData, new FunctionalCaseHistoryLogDTO(oldCase, oldBlod, oldCustomFields, new ArrayList<>(), new ArrayList<>()), user, OperationLogType.IMPORT.name(), OperationLogModule.CASE_MANAGEMENT_CASE_CASE, logDTOS);
    }

    /**
     * 获取更新的用例大字段数据
     *
     * @param ids
     * @return
     */
    private List<FunctionalCaseBlob> getBlobDataByIds(List<String> ids) {
        FunctionalCaseBlobExample blobExample = new FunctionalCaseBlobExample();
        blobExample.createCriteria().andIdIn(ids);
        return functionalCaseBlobMapper.selectByExampleWithBLOBs(blobExample);
    }

    /**
     * 获取更新的用例数据
     *
     * @param ids
     * @return
     */
    public List<FunctionalCase> getCaseDataByIds(List<String> ids) {
        FunctionalCaseExample example = new FunctionalCaseExample();
        example.createCriteria().andIdIn(ids);
        return functionalCaseMapper.selectByExample(example);
    }


    private void parseUpdateDataToModule(FunctionalCaseExcelData functionalCaseExcelData, FunctionalCaseImportRequest request, String userId, Map<String, String> caseModulePathMap, TemplateDTO defaultTemplateDTO, FunctionalCaseMapper caseMapper, FunctionalCaseBlobMapper caseBlobMapper,
                                         FunctionalCaseCustomFieldMapper customFieldMapper, Map<String, TemplateCustomFieldDTO> customFieldsMap, Map<String, List<FunctionalCase>> collect, List<FunctionalCaseHistoryLogDTO> historyLogDTOS) {
        //用例表
        FunctionalCase functionalCase = collect.get(functionalCaseExcelData.getNum()).getFirst();

        functionalCase.setName(functionalCaseExcelData.getName().toString());
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
        List<FunctionalCaseCustomField> customFields = handleUpdateCustomField(functionalCaseExcelData, functionalCase.getId(), customFieldMapper, customFieldsMap, userId);

        // 记录新值
        historyLogDTOS.add(new FunctionalCaseHistoryLogDTO(functionalCase, caseBlob, customFields, new ArrayList<>(), new ArrayList<>()));

    }

    private void addStatusIds(List<ProjectApplication> projectApplications, List<String> caseIds, Map<String, List<FunctionalCase>> collect, Map<String, List<FunctionalCaseBlob>> blobsCollect, FunctionalCaseExcelData functionalCaseExcelData) {
        FunctionalCase functionalCase = collect.get(functionalCaseExcelData.getNum()).getFirst();
        if (CollectionUtils.isNotEmpty(projectApplications) && Boolean.valueOf(projectApplications.getFirst().getTypeValue())) {
            FunctionalCaseBlob blob = blobsCollect.get(functionalCaseExcelData.getNum()).getFirst();
            if (!StringUtils.equals(functionalCase.getName(), functionalCaseExcelData.getName().toString())
                    || !StringUtils.equals(new String(blob.getSteps(), StandardCharsets.UTF_8), StringUtils.defaultIfBlank(functionalCaseExcelData.getSteps(), StringUtils.EMPTY))
                    || !StringUtils.equals(new String(blob.getTextDescription(), StandardCharsets.UTF_8), StringUtils.defaultIfBlank(functionalCaseExcelData.getTextDescription(), StringUtils.EMPTY))
                    || !StringUtils.equals(new String(blob.getExpectedResult(), StandardCharsets.UTF_8), StringUtils.defaultIfBlank(functionalCaseExcelData.getExpectedResult(), StringUtils.EMPTY))) {
                caseIds.add(functionalCase.getId());
            }
        }
    }

    private List<FunctionalCaseCustomField> handleUpdateCustomField(FunctionalCaseExcelData functionalCaseExcelData, String caseId, FunctionalCaseCustomFieldMapper customFieldMapper, Map<String, TemplateCustomFieldDTO> customFieldsMap, String userId) {
        FunctionalCaseCustomFieldExample fieldExample = new FunctionalCaseCustomFieldExample();
        fieldExample.createCriteria().andCaseIdEqualTo(caseId);
        customFieldMapper.deleteByExample(fieldExample);
        List<FunctionalCaseCustomField> customFields = handleImportCustomField(functionalCaseExcelData, caseId, customFieldMapper, customFieldsMap, userId);
        return customFields;
    }


    /**
     * 保存日志
     *
     * @param functionalCase 用例
     * @param originalValue  原值
     * @param modifiedLogDTO 新值
     */
    private void saveAddDataLog(FunctionalCase functionalCase, FunctionalCaseHistoryLogDTO originalValue, FunctionalCaseHistoryLogDTO modifiedLogDTO, String userId, String organizationId, String type, String module) {
        LogDTO dto = new LogDTO(
                functionalCase.getProjectId(),
                organizationId,
                functionalCase.getId(),
                userId,
                type,
                module,
                functionalCase.getName());
        dto.setHistory(true);
        dto.setPath("/functional/case/add");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setModifiedValue(JSON.toJSONBytes(modifiedLogDTO));
        dto.setOriginalValue(JSON.toJSONBytes(originalValue));
        operationLogService.add(dto);
    }

    /**
     * 日志原值
     *
     * @param functionalCase
     * @return
     */
    private FunctionalCaseHistoryLogDTO getAddLogModule(FunctionalCase functionalCase) {
        FunctionalCaseBlob functionalCaseBlob = functionalCaseBlobMapper.selectByPrimaryKey(functionalCase.getId());
        //自定义字段
        FunctionalCaseCustomFieldExample fieldExample = new FunctionalCaseCustomFieldExample();
        fieldExample.createCriteria().andCaseIdEqualTo(functionalCase.getId());
        List<FunctionalCaseCustomField> customFields = functionalCaseCustomFieldMapper.selectByExample(fieldExample);

        //附件  本地 + 文件库
        FunctionalCaseAttachmentExample attachmentExample = new FunctionalCaseAttachmentExample();
        attachmentExample.createCriteria().andCaseIdEqualTo(functionalCase.getId());
        List<FunctionalCaseAttachment> caseAttachments = functionalCaseAttachmentMapper.selectByExample(attachmentExample);

        FileAssociationExample example = new FileAssociationExample();
        example.createCriteria().andSourceIdEqualTo(functionalCase.getId());
        List<FileAssociation> fileAssociationList = fileAssociationMapper.selectByExample(example);


        FunctionalCaseHistoryLogDTO historyLogDTO = new FunctionalCaseHistoryLogDTO(functionalCase, functionalCaseBlob, customFields, caseAttachments, fileAssociationList);
        return historyLogDTO;
    }

    public List<OperationHistoryDTO> operationHistoryList(OperationHistoryRequest request) {
        return operationHistoryService.listWidthTable(request, CASE_TABLE);
    }
}
