package io.metersphere.functional.service;

import io.metersphere.functional.domain.*;
import io.metersphere.functional.dto.CaseCustomsFieldDTO;
import io.metersphere.functional.dto.FunctionalCaseDetailDTO;
import io.metersphere.functional.dto.FunctionalCaseVersionDTO;
import io.metersphere.functional.mapper.ExtFunctionalCaseMapper;
import io.metersphere.functional.mapper.FunctionalCaseBlobMapper;
import io.metersphere.functional.mapper.FunctionalCaseFollowerMapper;
import io.metersphere.functional.mapper.FunctionalCaseMapper;
import io.metersphere.functional.request.FunctionalCaseAddRequest;
import io.metersphere.functional.request.FunctionalCaseDeleteRequest;
import io.metersphere.functional.request.FunctionalCaseEditRequest;
import io.metersphere.functional.result.FunctionalCaseResultCode;
import io.metersphere.project.service.ProjectTemplateService;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.constants.FunctionalCaseExecuteResult;
import io.metersphere.sdk.constants.FunctionalCaseReviewStatus;
import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.dto.sdk.TemplateCustomFieldDTO;
import io.metersphere.system.dto.sdk.TemplateDTO;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
        //TODO v1.0不能固定 后续换成版本接口提供默认版本id
        functionalCase.setVersionId(StringUtils.defaultIfBlank(request.getVersionId(), "v1.0.0"));
        functionalCaseMapper.insertSelective(functionalCase);
        //附属表
        FunctionalCaseBlob functionalCaseBlob = new FunctionalCaseBlob();
        functionalCaseBlob.setId(caseId);
        BeanUtils.copyBean(functionalCaseBlob, request);
        functionalCaseBlobMapper.insertSelective(functionalCaseBlob);
        //保存自定义字段
        List<CaseCustomsFieldDTO> customsFields = request.getCustomsFields();
        if (CollectionUtils.isNotEmpty(customsFields)) {
            customsFields = customsFields.stream().distinct().collect(Collectors.toList());
            functionalCaseCustomFieldService.saveCustomField(caseId, customsFields);
        }
        return functionalCase;
    }

    public Long getNextOrder(String projectId) {
        Long pos = extFunctionalCaseMapper.getPos(projectId);
        return (pos == null ? 0 : pos) + ORDER_STEP;
    }

    public int getNextNum(String projectId) {
        long nextNum = NumGenerator.nextNum(projectId, ApplicationNumScope.CASE_MANAGEMENT);
        BigDecimal bigDecimal = new BigDecimal(nextNum);
        return bigDecimal.intValue();
    }


    /**
     * 查看用例获取详情
     *
     * @param functionalCaseId
     * @return
     */
    public FunctionalCaseDetailDTO getFunctionalCaseDetail(String functionalCaseId) {
        FunctionalCase functionalCase = checkFunctionalCase(functionalCaseId);
        FunctionalCaseDetailDTO functionalCaseDetailDTO = new FunctionalCaseDetailDTO();
        BeanUtils.copyBean(functionalCaseDetailDTO, functionalCase);
        FunctionalCaseBlob caseBlob = functionalCaseBlobMapper.selectByPrimaryKey(functionalCaseId);
        BeanUtils.copyBean(functionalCaseDetailDTO, caseBlob);

        //模板校验 获取自定义字段
        functionalCaseDetailDTO = checkTemplateCustomField(functionalCaseDetailDTO, functionalCase);

        //获取附件信息
        functionalCaseAttachmentService.getAttachmentInfo(functionalCaseDetailDTO);

        return functionalCaseDetailDTO;

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
        functionalCaseCustomFieldService.updateCustomField(request.getId(), request.getCustomsFields());
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
     * 获取用例关注人
     *
     * @param functionalCaseId
     * @return
     */
    public List<String> getFollower(String functionalCaseId) {
        checkFunctionalCase(functionalCaseId);
        FunctionalCaseFollowerExample example = new FunctionalCaseFollowerExample();
        example.createCriteria().andCaseIdEqualTo(functionalCaseId);
        List<FunctionalCaseFollower> caseFollowers = functionalCaseFollowerMapper.selectByExample(example);
        List<String> followers = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(caseFollowers)) {
            followers = caseFollowers.stream().map(FunctionalCaseFollower::getUserId).distinct().collect(Collectors.toList());
        }
        return followers;
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
}
