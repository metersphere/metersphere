package io.metersphere.functional.service;

import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.domain.FunctionalCaseBlob;
import io.metersphere.functional.dto.CaseCustomsFieldDTO;
import io.metersphere.functional.mapper.ExtFunctionalCaseMapper;
import io.metersphere.functional.mapper.FunctionalCaseBlobMapper;
import io.metersphere.functional.mapper.FunctionalCaseMapper;
import io.metersphere.functional.request.FunctionalCaseAddRequest;
import io.metersphere.sdk.constants.FunctionalCaseExecuteResult;
import io.metersphere.sdk.constants.FunctionalCaseReviewStatus;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.StorageType;
import io.metersphere.sdk.dto.LogDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.file.MinioRepository;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.MsFileUtils;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
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


    public FunctionalCase addFunctionalCase(FunctionalCaseAddRequest request, List<MultipartFile> files, String userId) {
        String caseId = IDGenerator.nextStr();
        //添加功能用例
        FunctionalCase functionalCase = addTestCase(caseId, request, userId);

        //上传文件
        if (CollectionUtils.isNotEmpty(files)) {
            uploadFile(request, caseId, files, true, userId);
        }

        //关联附件
        if (CollectionUtils.isNotEmpty(request.getRelateFileMetaIds())) {
            functionalCaseAttachmentService.relateFileMeta(request.getRelateFileMetaIds(), caseId, userId);
        }
        return functionalCase;
    }

    /**
     * 添加功能用例
     *
     * @param request
     */
    private FunctionalCase addTestCase(String caseId, FunctionalCaseAddRequest request, String userId) {
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
        files.forEach(file -> {
            String fileId = IDGenerator.nextStr();
            FileRequest fileRequest = new FileRequest();
            fileRequest.setFileName(file.getName());
            fileRequest.setProjectId(request.getProjectId());
            fileRequest.setResourceId(MsFileUtils.FUNCTIONAL_CASE_ATTACHMENT_DIR + fileId);
            fileRequest.setStorage(StorageType.MINIO.name());
            try {
                minioRepository.saveFile(file, fileRequest);
            } catch (Exception e) {
                throw new MSException("save file error");
            }
            functionalCaseAttachmentService.saveCaseAttachment(fileId, file, caseId, isLocal, userId);
        });

    }


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
}
