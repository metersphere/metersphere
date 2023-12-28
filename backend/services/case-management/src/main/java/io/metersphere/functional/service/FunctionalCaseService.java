package io.metersphere.functional.service;

import io.metersphere.functional.constants.CaseEvent;
import io.metersphere.functional.constants.FunctionalCaseReviewStatus;
import io.metersphere.functional.domain.*;
import io.metersphere.functional.dto.*;
import io.metersphere.functional.mapper.ExtFunctionalCaseMapper;
import io.metersphere.functional.mapper.FunctionalCaseBlobMapper;
import io.metersphere.functional.mapper.FunctionalCaseFollowerMapper;
import io.metersphere.functional.mapper.FunctionalCaseMapper;
import io.metersphere.functional.request.*;
import io.metersphere.functional.result.CaseManagementResultCode;
import io.metersphere.project.domain.FileAssociation;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.project.mapper.ExtBaseProjectVersionMapper;
import io.metersphere.project.service.ProjectTemplateService;
import io.metersphere.provider.BaseCaseProvider;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.constants.FunctionalCaseExecuteResult;
import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.dto.sdk.TemplateCustomFieldDTO;
import io.metersphere.system.dto.sdk.TemplateDTO;
import io.metersphere.system.dto.sdk.request.PosRequest;
import io.metersphere.system.service.BaseCustomFieldService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import io.metersphere.system.utils.ServiceUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
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

    private static final String CASE_MODULE_COUNT_ALL = "all";

    private static final String ADD_FUNCTIONAL_CASE_FILE_LOG_URL = "/functional/case/add";
    private static final String UPDATE_FUNCTIONAL_CASE_FILE_LOG_URL = "/functional/case/update";
    private static final String FUNCTIONAL_CASE_BATCH_COPY_FILE_LOG_URL = "/functional/case/batch/copy";

    public FunctionalCase addFunctionalCase(FunctionalCaseAddRequest request, List<MultipartFile> files, String userId) {
        String caseId = IDGenerator.nextStr();
        //添加功能用例
        FunctionalCase functionalCase = addCase(caseId, request, userId);

        //上传文件
        functionalCaseAttachmentService.uploadFile(request.getProjectId(), caseId, files, true, userId);

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
        functionalCase.setTags(JSON.toJSONString(request.getTags()));
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
        Map<String, Object> customFields = request.getCustomFields();
        if (MapUtils.isNotEmpty(customFields)) {
            List<CaseCustomFieldDTO> list = getCustomFields(customFields);
            functionalCaseCustomFieldService.saveCustomField(caseId, list);
        }
        return functionalCase;
    }

    private List<CaseCustomFieldDTO> getCustomFields(Map<String, Object> customFields) {
        List<CaseCustomFieldDTO> list = new ArrayList<>();
        customFields.keySet().forEach(key -> {
            CaseCustomFieldDTO caseCustomFieldDTO = new CaseCustomFieldDTO();
            caseCustomFieldDTO.setFieldId(key);
            caseCustomFieldDTO.setValue(JSON.toJSONString(customFields.get(key)));
            list.add(caseCustomFieldDTO);
        });
        return list;
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

        return functionalCaseDetailDTO;

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
        Map<String, Object> customFields = request.getCustomFields();
        if (MapUtils.isNotEmpty(customFields)) {
            List<CaseCustomFieldDTO> list = getCustomFields(customFields);
            functionalCaseCustomFieldService.updateCustomField(request.getId(), list);
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
        param.put(CaseEvent.Param.EVENT_NAME,CaseEvent.Event.DELETE_FUNCTIONAL_CASE);
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
        List<FunctionalCaseCustomFieldDTO> customFields = functionalCaseCustomFieldService.getCustomFieldsByCaseIds(ids);
        customFields.forEach(customField -> {
            if (customField.getInternal()) {
                customField.setName(baseCustomFieldService.translateInternalField(customField.getName()));
            }
        });
        Map<String, List<FunctionalCaseCustomFieldDTO>> collect = customFields.stream().collect(Collectors.groupingBy(FunctionalCaseCustomFieldDTO::getCaseId));
        functionalCaseLists.forEach(functionalCasePageDTO -> {
            functionalCasePageDTO.setCustomFields(collect.get(functionalCasePageDTO.getId()));
        });
        return functionalCaseLists;

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
                    if (StringUtils.isNotBlank(collect.get(id).getTags())) {
                        List<String> tags = JSON.parseArray(collect.get(id).getTags(), String.class);
                        tags.addAll(request.getTags());
                        List<String> newTags = tags.stream().distinct().collect(Collectors.toList());
                        functionalCase.setTags(JSON.toJSONString(newTags));
                    } else {
                        functionalCase.setTags(JSON.toJSONString(request.getTags()));
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
                functionalCase.setTags(JSON.toJSONString(request.getTags()));
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
        List<ModuleCountDTO> moduleCountDTOList = extFunctionalCaseMapper.countModuleIdByKeywordAndFileType(request, delete);
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
}
