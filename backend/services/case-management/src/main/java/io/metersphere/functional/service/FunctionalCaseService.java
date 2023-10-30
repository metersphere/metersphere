package io.metersphere.functional.service;

import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.domain.FunctionalCaseAttachment;
import io.metersphere.functional.domain.FunctionalCaseBlob;
import io.metersphere.functional.domain.FunctionalCaseCustomField;
import io.metersphere.functional.dto.CaseCustomsFieldDTO;
import io.metersphere.functional.dto.FunctionalCaseDetailDTO;
import io.metersphere.functional.mapper.ExtFunctionalCaseMapper;
import io.metersphere.functional.mapper.FunctionalCaseBlobMapper;
import io.metersphere.functional.mapper.FunctionalCaseMapper;
import io.metersphere.functional.request.FunctionalCaseAddRequest;
import io.metersphere.functional.request.FunctionalCaseEditRequest;
import io.metersphere.functional.result.FunctionalCaseResultCode;
import io.metersphere.project.service.ProjectTemplateService;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.MsFileUtils;
import io.metersphere.system.dto.sdk.TemplateCustomFieldDTO;
import io.metersphere.system.dto.sdk.TemplateDTO;
import io.metersphere.system.file.FileRequest;
import io.metersphere.system.file.MinioRepository;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * @author wx
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class FunctionalCaseService {

    public static final int ORDER_STEP = 5000;

    @Resource
    private MinioRepository minioRepository;

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


    public FunctionalCase addFunctionalCase(FunctionalCaseAddRequest request, List<MultipartFile> files, String userId) {
        String caseId = IDGenerator.nextStr();
        //添加功能用例
        FunctionalCase functionalCase = addCase(caseId, request, userId);

        //上传文件
        uploadFile(request, caseId, files, true, userId);

        //关联附件
        functionalCaseAttachmentService.relateFileMeta(request.getRelateFileMetaIds(), caseId, userId);
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
            functionalCaseCustomFieldService.saveCustomField(caseId, customsFields);
        }
        return functionalCase;
    }

    public Long getNextOrder(String projectId) {
        Long pos = extFunctionalCaseMapper.getPos(projectId);
        return (pos == null ? 0 : pos) + ORDER_STEP;
    }

    public int getNextNum(String projectId) {
        //TODO 获取下一个num方法(暂时直接查询数据库)
        FunctionalCase testCase = extFunctionalCaseMapper.getMaxNumByProjectId(projectId);
        if (testCase == null || testCase.getNum() == null) {
            return 100001;
        } else {
            return Optional.ofNullable(testCase.getNum() + 1).orElse(100001);
        }
    }

    /**
     * 功能用例上传附件
     *
     * @param request
     * @param files
     */
    public void uploadFile(FunctionalCaseAddRequest request, String caseId, List<MultipartFile> files, Boolean isLocal, String userId) {
        if (CollectionUtils.isNotEmpty(files)) {
            files.forEach(file -> {
                String fileId = IDGenerator.nextStr();
                FileRequest fileRequest = new FileRequest();
                fileRequest.setFileName(file.getName());
                fileRequest.setProjectId(request.getProjectId());
                fileRequest.setResourceId(MsFileUtils.FUNCTIONAL_CASE_ATTACHMENT_DIR + "/" + fileId);
                fileRequest.setStorage(StorageType.MINIO.name());
                try {
                    minioRepository.saveFile(file, fileRequest);
                } catch (Exception e) {
                    throw new MSException("save file error");
                }
                functionalCaseAttachmentService.saveCaseAttachment(fileId, file, caseId, isLocal, userId);
            });
        }
    }


    /**
     * 查看用例获取详情
     *
     * @param functionalCaseId
     * @return
     */
    public FunctionalCaseDetailDTO getFunctionalCaseDetail(String functionalCaseId) {
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(functionalCaseId);
        if (functionalCase == null) {
            throw new MSException(FunctionalCaseResultCode.FUNCTIONAL_CASE_NOT_FOUND);
        }
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
        //基本信息
        FunctionalCase functionalCase = new FunctionalCase();
        BeanUtils.copyBean(functionalCase, request);
        updateCase(request, userId, functionalCase);

        //处理删除文件id
        if (CollectionUtils.isNotEmpty(request.getDeleteFileMetaIds())) {
            this.deleteFile(request.getDeleteFileMetaIds(), request);
        }

        //上传新文件
        uploadFile(request, request.getId(), files, true, userId);

        //关联新附件
        functionalCaseAttachmentService.relateFileMeta(request.getRelateFileMetaIds(), request.getId(), userId);
        return functionalCase;

    }

    private void deleteFile(List<String> deleteFileMetaIds, FunctionalCaseEditRequest request) {
        List<FunctionalCaseAttachment> caseAttachments = functionalCaseAttachmentService.deleteCaseAttachment(deleteFileMetaIds, request.getId());
        if (CollectionUtils.isNotEmpty(caseAttachments)) {
            //删除本地上传的minio文件
            deleteMinioFile(caseAttachments, request.getProjectId());
        }
    }

    private void deleteMinioFile(List<FunctionalCaseAttachment> files, String projectId) {
        files.forEach(file -> {
            FileRequest fileRequest = new FileRequest();
            fileRequest.setFileName(file.getFileName());
            fileRequest.setProjectId(projectId);
            fileRequest.setResourceId(MsFileUtils.FUNCTIONAL_CASE_ATTACHMENT_DIR + "/" + file.getFileId());
            fileRequest.setStorage(StorageType.MINIO.name());
            try {
                minioRepository.delete(fileRequest);
            } catch (Exception e) {
                throw new MSException("delete file error");
            }
        });
    }

    private void updateCase(FunctionalCaseEditRequest request, String userId, FunctionalCase functionalCase) {
        //更新用例
        functionalCaseMapper.updateByPrimaryKeySelective(functionalCase);
        //更新附属表信息
        FunctionalCaseBlob functionalCaseBlob = new FunctionalCaseBlob();
        BeanUtils.copyBean(functionalCaseBlob, request);
        functionalCaseBlobMapper.updateByPrimaryKeySelective(functionalCaseBlob);

        //更新自定义字段
        functionalCaseCustomFieldService.updateCustomField(request.getId(), request.getCustomsFields());
    }



    //TODO 日志
    /**
     * 新增用例 日志
     *
     * @param requests
     * @param files
     * @return
     */
    public LogDTO addFunctionalCaseLog(FunctionalCaseAddRequest requests, List<MultipartFile> files) {
        LogDTO dto = new LogDTO(
                requests.getProjectId(),
                null,
                null,
                null,
                OperationLogType.ADD.name(),
                OperationLogModule.FUNCTIONAL_CASE,
                requests.getName());

        dto.setPath("/functional/case/add");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(requests));
        return dto;
    }


    /**
     * 更新用例 日志
     *
     * @param requests
     * @param files
     * @return
     */
    public LogDTO updateFunctionalCaseLog(FunctionalCaseAddRequest requests, List<MultipartFile> files) {
        //TODO 获取原值
        LogDTO dto = new LogDTO(
                requests.getProjectId(),
                null,
                null,
                null,
                OperationLogType.UPDATE.name(),
                OperationLogModule.FUNCTIONAL_CASE,
                requests.getName());

        dto.setPath("/functional/case/update");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setModifiedValue(JSON.toJSONBytes(requests));
        return dto;
    }
}
