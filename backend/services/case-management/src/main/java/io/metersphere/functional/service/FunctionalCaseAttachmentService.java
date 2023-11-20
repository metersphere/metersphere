package io.metersphere.functional.service;


import com.google.common.collect.Lists;
import io.metersphere.functional.domain.FunctionalCaseAttachment;
import io.metersphere.functional.domain.FunctionalCaseAttachmentExample;
import io.metersphere.functional.dto.FunctionalCaseAttachmentDTO;
import io.metersphere.functional.dto.FunctionalCaseDetailDTO;
import io.metersphere.functional.mapper.FunctionalCaseAttachmentMapper;
import io.metersphere.functional.request.FunctionalCaseAddRequest;
import io.metersphere.project.dto.filemanagement.FileInfo;
import io.metersphere.project.dto.filemanagement.FileLogRecord;
import io.metersphere.project.service.FileAssociationService;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.StorageType;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.FileAssociationSourceUtil;
import io.metersphere.system.file.FileRequest;
import io.metersphere.system.file.MinioRepository;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
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
public class FunctionalCaseAttachmentService {


    @Resource
    private FunctionalCaseAttachmentMapper functionalCaseAttachmentMapper;

    @Resource
    private MinioRepository minioRepository;

    @Resource
    private FileAssociationService fileAssociationService;

    /**
     * 保存本地上传文件和用例关联关系
     *
     * @param fileId
     * @param file
     * @param caseId
     * @param isLocal
     * @param userId
     */
    public void saveCaseAttachment(String fileId, MultipartFile file, String caseId, Boolean isLocal, String userId) {
        FunctionalCaseAttachment caseAttachment = creatModule(fileId, file.getName(), file.getSize(), caseId, isLocal, userId);
        functionalCaseAttachmentMapper.insertSelective(caseAttachment);
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
                fileRequest.setFolder(DefaultRepositoryDir.getFunctionalCaseDir(request.getProjectId(), caseId) + "/" + fileId);
                fileRequest.setStorage(StorageType.MINIO.name());
                try {
                    minioRepository.saveFile(file, fileRequest);
                } catch (Exception e) {
                    throw new MSException("save file error");
                }
                saveCaseAttachment(fileId, file, caseId, isLocal, userId);
            });
        }
    }

    private FunctionalCaseAttachment creatModule(String fileId, String fileName, long fileSize, String caseId, Boolean isLocal, String userId) {
        FunctionalCaseAttachment caseAttachment = new FunctionalCaseAttachment();
        caseAttachment.setId(IDGenerator.nextStr());
        caseAttachment.setCaseId(caseId);
        caseAttachment.setFileId(fileId);
        caseAttachment.setFileName(fileName);
        caseAttachment.setSize(fileSize);
        caseAttachment.setLocal(isLocal);
        caseAttachment.setCreateUser(userId);
        caseAttachment.setCreateTime(System.currentTimeMillis());
        return caseAttachment;
    }


    /**
     * 获取附件信息
     *
     * @param functionalCaseDetailDTO
     */
    public void getAttachmentInfo(FunctionalCaseDetailDTO functionalCaseDetailDTO) {
        FunctionalCaseAttachmentExample example = new FunctionalCaseAttachmentExample();
        example.createCriteria().andCaseIdEqualTo(functionalCaseDetailDTO.getId());
        List<FunctionalCaseAttachment> caseAttachments = functionalCaseAttachmentMapper.selectByExample(example);
        List<FunctionalCaseAttachmentDTO> attachmentDTOs = new ArrayList<>(Lists.transform(caseAttachments, (functionalCaseAttachment) -> {
            FunctionalCaseAttachmentDTO attachmentDTO = new FunctionalCaseAttachmentDTO();
            BeanUtils.copyBean(attachmentDTO, functionalCaseAttachment);
            return attachmentDTO;
        }));

        //获取关联的附件信息
        List<FileInfo> files = fileAssociationService.getFiles(functionalCaseDetailDTO.getId(), FileAssociationSourceUtil.SOURCE_TYPE_FUNCTIONAL_CASE);
        List<FunctionalCaseAttachmentDTO> filesDTOs = new ArrayList<>(Lists.transform(files, (fileInfo) -> {
            FunctionalCaseAttachmentDTO attachmentDTO = new FunctionalCaseAttachmentDTO();
            BeanUtils.copyBean(attachmentDTO, fileInfo);
            return attachmentDTO;
        }));
        attachmentDTOs.addAll(filesDTOs);
        Collections.sort(attachmentDTOs, Comparator.comparing(FunctionalCaseAttachmentDTO::getCreateTime));
        functionalCaseDetailDTO.setAttachments(attachmentDTOs);
    }


    /**
     * 更新用例时删除文件 取消关联关系
     *
     * @param deleteFileMetaIds
     */
    public void deleteCaseAttachment(List<String> deleteFileMetaIds, String caseId, String projectId) {
        FunctionalCaseAttachmentExample example = new FunctionalCaseAttachmentExample();
        example.createCriteria().andFileIdIn(deleteFileMetaIds).andCaseIdEqualTo(caseId).andLocalEqualTo(true);
        List<FunctionalCaseAttachment> delAttachment = functionalCaseAttachmentMapper.selectByExample(example);
        example.clear();
        example.createCriteria().andFileIdIn(deleteFileMetaIds).andCaseIdEqualTo(caseId);
        functionalCaseAttachmentMapper.deleteByExample(example);
        this.deleteMinioFile(delAttachment, projectId, caseId);
    }


    private void deleteMinioFile(List<FunctionalCaseAttachment> files, String caseId, String projectId) {
        if (CollectionUtils.isNotEmpty(files)) {
            files.forEach(file -> {
                FileRequest fileRequest = new FileRequest();
                fileRequest.setFileName(file.getFileName());
                fileRequest.setFolder(DefaultRepositoryDir.getFunctionalCaseDir(projectId, caseId) + "/" + file.getFileId());
                fileRequest.setStorage(StorageType.MINIO.name());
                try {
                    minioRepository.delete(fileRequest);
                } catch (Exception e) {
                    throw new MSException("delete file error");
                }
            });
        }
    }

    /**
     * 通过caseId获取附件信息
     *
     * @param ids
     * @return
     */
    public Map<String, List<FunctionalCaseAttachment>> getAttachmentByCaseIds(List<String> ids) {
        FunctionalCaseAttachmentExample example = new FunctionalCaseAttachmentExample();
        example.createCriteria().andCaseIdIn(ids);
        List<FunctionalCaseAttachment> caseAttachments = functionalCaseAttachmentMapper.selectByExample(example);
        Map<String, List<FunctionalCaseAttachment>> attachmentMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(caseAttachments)) {
            attachmentMap = caseAttachments.stream().collect(Collectors.groupingBy(FunctionalCaseAttachment::getCaseId));
        }
        return attachmentMap;
    }

    public void batchSaveAttachment(List<FunctionalCaseAttachment> attachments) {
        functionalCaseAttachmentMapper.batchInsert(attachments);
    }


    /**
     * 保存文件库文件与用例关联关系
     *
     * @param relateFileMetaIds
     * @param caseId
     * @param userId
     * @param logUrl
     * @param projectId
     */
    public void association(List<String> relateFileMetaIds, String caseId, String userId, String logUrl, String projectId) {
        fileAssociationService.association(caseId, FileAssociationSourceUtil.SOURCE_TYPE_FUNCTIONAL_CASE, relateFileMetaIds, false, createFileLogRecord(logUrl, userId, projectId));
    }

    private FileLogRecord createFileLogRecord(String logUrl, String operator, String projectId) {
        return FileLogRecord.builder()
                .logModule(OperationLogModule.FUNCTIONAL_CASE)
                .requestMethod(HttpMethodConstants.POST.name())
                .requestUrl(logUrl)
                .operator(operator)
                .projectId(projectId)
                .build();
    }


    /**
     * 取消关联 删除文件库文件和用例关联关系
     *
     * @param unLinkFilesIds
     * @param logUrl
     * @param userId
     * @param projectId
     */
    public void unAssociation(List<String> unLinkFilesIds, String logUrl, String userId, String projectId) {
        fileAssociationService.deleteBySourceId(unLinkFilesIds, createFileLogRecord(logUrl, userId, projectId));
    }
}