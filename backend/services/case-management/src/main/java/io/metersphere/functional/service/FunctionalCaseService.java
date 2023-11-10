package io.metersphere.functional.service;

import io.metersphere.functional.domain.*;
import io.metersphere.functional.dto.CaseCustomFieldDTO;
import io.metersphere.functional.dto.FunctionalCaseDetailDTO;
import io.metersphere.functional.dto.FunctionalCasePageDTO;
import io.metersphere.functional.dto.FunctionalCaseVersionDTO;
import io.metersphere.functional.mapper.ExtFunctionalCaseMapper;
import io.metersphere.functional.mapper.FunctionalCaseBlobMapper;
import io.metersphere.functional.mapper.FunctionalCaseFollowerMapper;
import io.metersphere.functional.mapper.FunctionalCaseMapper;
import io.metersphere.functional.request.*;
import io.metersphere.functional.result.FunctionalCaseResultCode;
import io.metersphere.project.mapper.ExtProjectVersionMapper;
import io.metersphere.project.service.ProjectTemplateService;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.constants.FunctionalCaseExecuteResult;
import io.metersphere.sdk.constants.FunctionalCaseReviewStatus;
import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.dto.sdk.TemplateCustomFieldDTO;
import io.metersphere.system.dto.sdk.TemplateDTO;
import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wx
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class FunctionalCaseService {

    public static final int ORDER_STEP = 5000;

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
    private DeleteFunctionalCaseService deleteFunctionalCaseService;

    @Resource
    SqlSessionFactory sqlSessionFactory;

    @Resource
    private ExtProjectVersionMapper extProjectVersionMapper;

    public FunctionalCase addFunctionalCase(FunctionalCaseAddRequest request, List<MultipartFile> files, String userId) {
        String caseId = IDGenerator.nextStr();
        //添加功能用例
        FunctionalCase functionalCase = addCase(caseId, request, userId);

        //上传文件
        functionalCaseAttachmentService.uploadFile(request, caseId, files, true, userId);

        //关联附件
        functionalCaseAttachmentService.relateFileMeta(request.getRelateFileMetaIds(), caseId, userId);

        //TODO 记录变更历史

        return functionalCase;
    }

    /**
     * 添加功能用例
     *
     * @param request
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
        functionalCase.setVersionId(StringUtils.defaultIfBlank(request.getVersionId(), extProjectVersionMapper.getDefaultVersion(request.getProjectId())));
        functionalCaseMapper.insertSelective(functionalCase);
        //附属表
        FunctionalCaseBlob functionalCaseBlob = new FunctionalCaseBlob();
        functionalCaseBlob.setId(caseId);
        BeanUtils.copyBean(functionalCaseBlob, request);
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
        return (pos == null ? 0 : pos) + ORDER_STEP;
    }

    public long getNextNum(String projectId) {
        return NumGenerator.nextNum(projectId, ApplicationNumScope.CASE_MANAGEMENT);
    }


    /**
     * 查看用例获取详情
     *
     * @param functionalCaseId
     * @return
     */
    public FunctionalCaseDetailDTO getFunctionalCaseDetail(String functionalCaseId, String userId) {
        FunctionalCase functionalCase = checkFunctionalCase(functionalCaseId);
        FunctionalCaseDetailDTO functionalCaseDetailDTO = new FunctionalCaseDetailDTO();
        BeanUtils.copyBean(functionalCaseDetailDTO, functionalCase);
        FunctionalCaseBlob caseBlob = functionalCaseBlobMapper.selectByPrimaryKey(functionalCaseId);
        BeanUtils.copyBean(functionalCaseDetailDTO, caseBlob);

        //模板校验 获取自定义字段
        functionalCaseDetailDTO = checkTemplateCustomField(functionalCaseDetailDTO, functionalCase);

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
     * @param functionalCaseId
     * @return
     */
    private FunctionalCase checkFunctionalCase(String functionalCaseId) {
        FunctionalCaseExample functionalCaseExample = new FunctionalCaseExample();
        functionalCaseExample.createCriteria().andIdEqualTo(functionalCaseId).andDeletedEqualTo(false);
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(functionalCaseId);
        if (functionalCase == null) {
            throw new MSException(FunctionalCaseResultCode.FUNCTIONAL_CASE_NOT_FOUND);
        }
        return functionalCase;
    }


    /**
     * 获取模板自定义字段
     *
     * @param functionalCase
     */
    private FunctionalCaseDetailDTO checkTemplateCustomField(FunctionalCaseDetailDTO functionalCaseDetailDTO, FunctionalCase functionalCase) {
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
        return functionalCaseDetailDTO;
    }


    /**
     * 更新用例 基本信息
     *
     * @param request
     * @param files
     * @param userId
     * @return
     */
    public FunctionalCase updateFunctionalCase(FunctionalCaseEditRequest request, List<MultipartFile> files, String userId) {
        FunctionalCase checked = checkFunctionalCase(request.getId());

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

        //上传新文件
        functionalCaseAttachmentService.uploadFile(request, request.getId(), files, true, userId);

        //关联新附件
        functionalCaseAttachmentService.relateFileMeta(request.getRelateFileMetaIds(), request.getId(), userId);

        //TODO 记录变更历史 addFunctionalCaseHistory

        return functionalCase;

    }


    /**
     * 多版本所属模块更新处理
     *
     * @param refId
     * @param moduleId
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
        BeanUtils.copyBean(functionalCaseBlob, request);
        functionalCaseBlobMapper.updateByPrimaryKeySelective(functionalCaseBlob);

        //更新自定义字段
        functionalCaseCustomFieldService.updateCustomField(request.getId(), request.getCustomFields());
    }


    /**
     * 关注/取消关注用例
     *
     * @param functionalCaseId
     * @param userId
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
     * @param request
     * @param userId
     */
    public void deleteFunctionalCase(FunctionalCaseDeleteRequest request, String userId) {
        List<FunctionalCaseVersionDTO> versionDTOList = getFunctionalCaseVersion(request.getId());
        if (versionDTOList.size() > 1) {
            //存在多个版本
            List<String> ids = versionDTOList.stream().map(FunctionalCaseVersionDTO::getId).collect(Collectors.toList());
            String projectId = versionDTOList.get(0).getProjectId();
            handleFunctionalCaseByVersions(request, projectId, ids, userId);
        } else {
            //只有一个版本 直接放入回收站
            doDelete(request.getId(), userId);
        }

    }


    /**
     * 用例存在多个版本情况下 删除用例的处理
     *
     * @param request
     * @param ids
     * @param userId
     */
    private void handleFunctionalCaseByVersions(FunctionalCaseDeleteRequest request, String projectId, List<String> ids, String userId) {
        if (request.getDeleteAll()) {
            //删除所有版本
            ids.forEach(id -> {
                doDelete(id, userId);
            });
        } else {
            //删除指定版本(用例多版本情况下 删除单个版本用例)
            deleteFunctionalCaseService.deleteFunctionalCaseResource(Arrays.asList(request.getId()), projectId);
        }
    }


    private void doDelete(String id, String userId) {
        FunctionalCase functionalCase = new FunctionalCase();
        functionalCase.setDeleted(true);
        functionalCase.setId(id);
        functionalCase.setDeleteUser(userId);
        functionalCase.setDeleteTime(System.currentTimeMillis());
        functionalCaseMapper.updateByPrimaryKeySelective(functionalCase);
    }


    /**
     * 根据用例id 获取用例是否存在多个版本
     *
     * @param functionalCaseId
     * @return
     */
    public List<FunctionalCaseVersionDTO> getFunctionalCaseVersion(String functionalCaseId) {
        FunctionalCase functionalCase = checkFunctionalCase(functionalCaseId);
        List<FunctionalCaseVersionDTO> list = extFunctionalCaseMapper.getFunctionalCaseByRefId(functionalCase.getRefId());
        return list;
    }

    /**
     * 列表查询
     *
     * @param request
     * @return
     */
    public List<FunctionalCasePageDTO> getFunctionalCasePage(FunctionalCasePageRequest request, Boolean deleted) {
        List<FunctionalCasePageDTO> functionalCaseLists = extFunctionalCaseMapper.list(request, deleted);
        if (CollectionUtils.isEmpty(functionalCaseLists)) {
            return new ArrayList<>();
        }
        //处理自定义字段值
        return handleCustomFieldsAnd(functionalCaseLists);
    }

    private List<FunctionalCasePageDTO> handleCustomFieldsAnd(List<FunctionalCasePageDTO> functionalCaseLists) {
        List<String> ids = functionalCaseLists.stream().map(FunctionalCasePageDTO::getId).collect(Collectors.toList());
        List<FunctionalCaseCustomField> customFields = functionalCaseCustomFieldService.getCustomFieldByCaseIds(ids);
        Map<String, List<FunctionalCaseCustomField>> collect = customFields.stream().collect(Collectors.groupingBy(FunctionalCaseCustomField::getCaseId));
        functionalCaseLists.forEach(functionalCasePageDTO -> {
            functionalCasePageDTO.setCustomFields(collect.get(functionalCasePageDTO.getId()));
        });
        return functionalCaseLists;

    }

    public void batchDeleteFunctionalCaseToGc(FunctionalCaseBatchRequest request, String userId) {
        List<String> ids = doSelectIds(request, request.getProjectId());
        if (CollectionUtils.isNotEmpty(ids)) {
            List<String> refId = extFunctionalCaseMapper.getRefIds(ids);
            extFunctionalCaseMapper.batchDelete(refId, userId);
        }
    }


    public <T> List<String> doSelectIds(T dto, String projectId) {
        TableBatchProcessDTO request = (TableBatchProcessDTO) dto;
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
     * @param request
     * @param userId
     */
    public void batchMoveFunctionalCase(FunctionalCaseBatchMoveRequest request, String userId) {
        List<String> ids = doSelectIds(request, request.getProjectId());
        if (CollectionUtils.isNotEmpty(ids)) {
            List<String> refId = extFunctionalCaseMapper.getRefIds(ids);
            extFunctionalCaseMapper.batchMoveModule(request, refId, userId);
        }
    }

    /**
     * 批量复制用例
     *
     * @param request
     * @param userId
     */
    public void batchCopyFunctionalCase(FunctionalCaseBatchMoveRequest request, String userId) {
        List<String> ids = doSelectIds(request, request.getProjectId());
        if (CollectionUtils.isNotEmpty(ids)) {
            //基本信息
            Map<String, FunctionalCase> functionalCaseMap = copyBaseInfo(request.getProjectId(), ids);
            //大字段
            Map<String, FunctionalCaseBlob> functionalCaseBlobMap = copyBlobInfo(ids);
            //附件 本地附件
            Map<String, List<FunctionalCaseAttachment>> attachmentMap = functionalCaseAttachmentService.getAttachmentByCaseIds(ids);
            //TODO 文件库附件
            //自定义字段
            Map<String, List<FunctionalCaseCustomField>> customFieldMap = functionalCaseCustomFieldService.getCustomFieldMapByCaseIds(ids);

            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            FunctionalCaseMapper mapper = sqlSession.getMapper(FunctionalCaseMapper.class);
            Long nextOrder = getNextOrder(request.getProjectId());

            try {
                for (int i = 0; i < ids.size(); i++) {
                    String id = IDGenerator.nextStr();
                    FunctionalCase functionalCase = functionalCaseMap.get(ids.get(i));
                    FunctionalCaseBlob functionalCaseBlob = functionalCaseBlobMap.get(ids.get(i));
                    List<FunctionalCaseAttachment> caseAttachments = attachmentMap.get(ids.get(i));
                    List<FunctionalCaseCustomField> customFields = customFieldMap.get(ids.get(i));

                    Optional.ofNullable(functionalCase).ifPresent(functional -> {
                        functional.setId(id);
                        functional.setRefId(id);
                        functional.setModuleId(request.getModuleId());
                        functional.setNum(getNextNum(request.getProjectId()));
                        functional.setName(getCopyName(functionalCase.getName()));
                        functional.setReviewStatus(FunctionalCaseReviewStatus.UN_REVIEWED.name());
                        functional.setPos(nextOrder + ORDER_STEP);
                        functional.setLastExecuteResult(FunctionalCaseExecuteResult.UN_EXECUTED.name());
                        functional.setCreateUser(userId);
                        functional.setCreateTime(System.currentTimeMillis());
                        functional.setUpdateTime(System.currentTimeMillis());
                        mapper.insert(functional);

                        functionalCaseBlob.setId(id);
                        functionalCaseBlobMapper.insert(functionalCaseBlob);
                    });

                    if (CollectionUtils.isNotEmpty(caseAttachments)) {
                        caseAttachments.stream().forEach(attachment -> {
                            attachment.setId(IDGenerator.nextStr());
                            attachment.setCaseId(id);
                            attachment.setCreateUser(userId);
                            attachment.setCreateTime(System.currentTimeMillis());
                        });
                        functionalCaseAttachmentService.batchSaveAttachment(caseAttachments);
                    }

                    if (CollectionUtils.isNotEmpty(customFields)) {
                        customFields.stream().forEach(customField -> {
                            customField.setCaseId(id);
                        });
                        functionalCaseCustomFieldService.batchSaveCustomField(customFields);
                    }

                    if (i % 50 == 0) {
                        sqlSession.flushStatements();
                    }
                }
                sqlSession.flushStatements();
            } finally {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
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


    private Map<String, FunctionalCaseBlob> copyBlobInfo(List<String> ids) {
        FunctionalCaseBlobExample blobExample = new FunctionalCaseBlobExample();
        blobExample.createCriteria().andIdIn(ids);
        List<FunctionalCaseBlob> functionalCaseBlobs = functionalCaseBlobMapper.selectByExampleWithBLOBs(blobExample);
        Map<String, FunctionalCaseBlob> functionalCaseBlobMap = functionalCaseBlobs.stream().collect(Collectors.toMap(FunctionalCaseBlob::getId, functionalCaseBlob -> functionalCaseBlob));
        return functionalCaseBlobMap;
    }

    private Map<String, FunctionalCase> copyBaseInfo(String projectId, List<String> ids) {
        FunctionalCaseExample example = new FunctionalCaseExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andDeletedEqualTo(false).andIdIn(ids);
        List<FunctionalCase> functionalCaseLists = functionalCaseMapper.selectByExample(example);
        Map<String, FunctionalCase> functionalMap = functionalCaseLists.stream().collect(Collectors.toMap(FunctionalCase::getId, functionalCase -> functionalCase));
        return functionalMap;
    }
}
