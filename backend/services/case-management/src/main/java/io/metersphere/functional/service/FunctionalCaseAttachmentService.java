package io.metersphere.functional.service;


import com.google.common.collect.Lists;
import io.metersphere.functional.constants.CaseFileSourceType;
import io.metersphere.functional.domain.FunctionalCaseAttachment;
import io.metersphere.functional.domain.FunctionalCaseAttachmentExample;
import io.metersphere.functional.dto.FunctionalCaseAttachmentDTO;
import io.metersphere.functional.dto.FunctionalCaseDetailDTO;
import io.metersphere.functional.mapper.FunctionalCaseAttachmentMapper;
import io.metersphere.functional.request.FunctionalCaseAssociationFileRequest;
import io.metersphere.functional.request.FunctionalCaseDeleteFileRequest;
import io.metersphere.functional.request.FunctionalCaseFileRequest;
import io.metersphere.project.domain.FileAssociation;
import io.metersphere.project.dto.filemanagement.FileInfo;
import io.metersphere.project.dto.filemanagement.FileLogRecord;
import io.metersphere.project.service.FileAssociationService;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.constants.StorageType;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.file.FileCenter;
import io.metersphere.sdk.file.FileCopyRequest;
import io.metersphere.sdk.file.FileRepository;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.util.*;
import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserExample;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.service.CommonFileService;
import io.metersphere.system.service.FileService;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;
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
    private FileService fileService;

    @Resource
    private FileAssociationService fileAssociationService;

    @Resource
    private CommonFileService commonFileService;

    private static final String UPLOAD_FILE = "/attachment/upload/file";
    private static final String DELETED_FILE = "/attachment/delete/file";

    @Resource
    private UserMapper userMapper;


    @Value("50MB")
    private DataSize maxFileSize;

    /**
     * 保存本地上传文件和用例关联关系
     *
     * @param fileId  fileId
     * @param file    file
     * @param caseId  caseId
     * @param isLocal isLocal
     * @param userId  userId
     */
    public void saveCaseAttachment(String fileId, MultipartFile file, String caseId, Boolean isLocal, String userId) {
        FunctionalCaseAttachment caseAttachment = creatAttachment(fileId, file.getOriginalFilename(), file.getSize(), caseId, isLocal, userId);
        functionalCaseAttachmentMapper.insertSelective(caseAttachment);
    }


    /**
     * 功能用例上传附件
     *
     * @param projectId projectId
     * @param files     files
     */
    public List<String> uploadFile(String projectId, String caseId, List<MultipartFile> files, Boolean isLocal, String userId) {
        LogUtils.info("开始上传附件");
        List<String> fileIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(files)) {
            files.forEach(file -> {
                String fileId = IDGenerator.nextStr();
                FileRequest fileRequest = new FileRequest();
                fileRequest.setFileName(file.getOriginalFilename());
                fileRequest.setFolder(DefaultRepositoryDir.getFunctionalCaseDir(projectId, caseId) + "/" + fileId);
                fileRequest.setStorage(StorageType.MINIO.name());
                try {
                    fileService.upload(file, fileRequest);
                } catch (Exception e) {
                    throw new MSException("save file error");
                }
                saveCaseAttachment(fileId, file, caseId, isLocal, userId);
                fileIds.add(fileId);
            });
        }
        LogUtils.info("附件上传结束");
        return fileIds;
    }

    public FunctionalCaseAttachment creatAttachment(String fileId, String fileName, long fileSize, String caseId, Boolean isLocal, String userId) {
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
     * @param functionalCaseDetailDTO functionalCaseDetailDTO
     */
    public void getAttachmentInfo(FunctionalCaseDetailDTO functionalCaseDetailDTO) {
        FunctionalCaseAttachmentExample example = new FunctionalCaseAttachmentExample();
        example.createCriteria().andCaseIdEqualTo(functionalCaseDetailDTO.getId()).andFileSourceEqualTo(CaseFileSourceType.ATTACHMENT.toString());
        List<FunctionalCaseAttachment> caseAttachments = functionalCaseAttachmentMapper.selectByExample(example);
        List<FunctionalCaseAttachmentDTO> attachmentDTOs = new ArrayList<>(Lists.transform(caseAttachments, (functionalCaseAttachment) -> {
            FunctionalCaseAttachmentDTO attachmentDTO = new FunctionalCaseAttachmentDTO();
            BeanUtils.copyBean(attachmentDTO, functionalCaseAttachment);
            attachmentDTO.setId(functionalCaseAttachment.getFileId());
            attachmentDTO.setAssociationId(functionalCaseAttachment.getId());
            return attachmentDTO;
        }));

        //获取关联的附件信息
        List<FileInfo> files = fileAssociationService.getFiles(functionalCaseDetailDTO.getId());
        List<FunctionalCaseAttachmentDTO> filesDTOs = new ArrayList<>(Lists.transform(files, (fileInfo) -> {
            FunctionalCaseAttachmentDTO attachmentDTO = new FunctionalCaseAttachmentDTO();
            BeanUtils.copyBean(attachmentDTO, fileInfo);
            attachmentDTO.setId(fileInfo.getFileId());
            attachmentDTO.setAssociationId(fileInfo.getId());
            if (StringUtils.isNotBlank(fileInfo.getDeletedFileName())) {
                attachmentDTO.setFileName(fileInfo.getDeletedFileName());
            }
            return attachmentDTO;
        }));
        attachmentDTOs.addAll(filesDTOs);
        attachmentDTOs.sort(Comparator.comparing(FunctionalCaseAttachmentDTO::getCreateTime).reversed());
        List<FunctionalCaseAttachmentDTO> returnAttachmentDTO = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(attachmentDTOs)) {
            List<String> userList = attachmentDTOs.stream().map(FunctionalCaseAttachmentDTO::getCreateUser).toList();
            UserExample userExample = new UserExample();
            userExample.createCriteria().andIdIn(userList);
            List<User> users = userMapper.selectByExample(userExample);
            Map<String, String> collect = users.stream().collect(Collectors.toMap(User::getId, User::getName));
            attachmentDTOs.forEach(item -> {
                if (!item.isDeleted()) {
                    String userName = collect.get(item.getCreateUser());
                    item.setCreateUserName(userName);
                    returnAttachmentDTO.add(item);
                }
            });
        }
        functionalCaseDetailDTO.setAttachments(returnAttachmentDTO);
    }


    /**
     * 更新用例时删除文件 取消关联关系
     *
     * @param deleteFileMetaIds deleteFileMetaIds
     */
    public void deleteCaseAttachment(List<String> deleteFileMetaIds, String caseId, String projectId) {
        FunctionalCaseAttachmentExample example = new FunctionalCaseAttachmentExample();
        example.createCriteria().andFileIdIn(deleteFileMetaIds).andCaseIdEqualTo(caseId).andLocalEqualTo(true);
        List<FunctionalCaseAttachment> delAttachment = functionalCaseAttachmentMapper.selectByExample(example);
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
                    fileService.deleteFile(fileRequest);
                    String fileType = StringUtils.substring(file.getFileName(), file.getFileName().lastIndexOf(".") + 1);
                    if (TempFileUtils.isImage(fileType)) {
                        //删除预览图
                        fileRequest = new FileRequest();
                        fileRequest.setFileName(file.getFileName());
                        fileRequest.setFolder(DefaultRepositoryDir.getFunctionalCasePreviewDir(projectId, caseId) + "/" + file.getFileId());
                        fileRequest.setStorage(StorageType.MINIO.name());
                        fileService.deleteFile(fileRequest);
                    }
                } catch (Exception e) {
                    throw new MSException("delete file error");
                }
            });
        }
    }

    /**
     * 通过caseId获取附件信息
     *
     * @param ids ids
     * @return
     */
    public Map<String, List<FunctionalCaseAttachment>> getAttachmentByCaseIds(List<String> ids) {
        List<String> sources = List.of(CaseFileSourceType.ATTACHMENT.toString(), CaseFileSourceType.CASE_DETAIL.toString());
        FunctionalCaseAttachmentExample example = new FunctionalCaseAttachmentExample();
        example.createCriteria().andCaseIdIn(ids).andFileSourceIn(sources);
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
     * @param relateFileMetaIds relateFileMetaIds
     * @param caseId            caseId
     * @param userId            userId
     * @param logUrl            logUrl
     * @param projectId         projectId
     */
    public void association(List<String> relateFileMetaIds, String caseId, String userId, String logUrl, String projectId) {
        fileAssociationService.association(caseId, FileAssociationSourceUtil.SOURCE_TYPE_FUNCTIONAL_CASE, relateFileMetaIds, createFileLogRecord(logUrl, userId, projectId));
    }

    private FileLogRecord createFileLogRecord(String logUrl, String operator, String projectId) {
        return FileLogRecord.builder()
                .logModule(OperationLogModule.CASE_MANAGEMENT_CASE_CASE)
                .operator(operator)
                .projectId(projectId)
                .build();
    }


    /**
     * 取消关联 删除文件库文件和用例关联关系
     *
     * @param sourceId       sourceId
     * @param unLinkFilesIds unLinkFilesIds
     * @param logUrl         logUrl
     * @param userId         userId
     * @param projectId      projectId
     */
    public void unAssociation(String sourceId, List<String> unLinkFilesIds, String logUrl, String userId, String projectId) {
        fileAssociationService.deleteBySourceIdAndFileIds(sourceId, unLinkFilesIds, createFileLogRecord(logUrl, userId, projectId));
    }


    /**
     * 预览图片
     *
     * @param request request
     */
    public ResponseEntity<byte[]> downloadPreviewImgById(FunctionalCaseFileRequest request) {
        FunctionalCaseAttachmentExample example = new FunctionalCaseAttachmentExample();
        example.createCriteria().andFileIdEqualTo(request.getFileId()).andCaseIdEqualTo(request.getCaseId());
        List<FunctionalCaseAttachment> caseAttachments = functionalCaseAttachmentMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(caseAttachments)) {
            FunctionalCaseAttachment attachment = caseAttachments.getFirst();
            FileRequest fileRequest = new FileRequest();
            fileRequest.setFileName(attachment.getFileName());
            fileRequest.setFolder(DefaultRepositoryDir.getFunctionalCaseDir(request.getProjectId(), request.getCaseId()) + "/" + attachment.getFileId());
            fileRequest.setStorage(StorageType.MINIO.name());
            byte[] bytes = null;
            try {
                bytes = fileService.download(fileRequest);
            } catch (Exception e) {
                throw new MSException("get file error");
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFileName() + "\"")
                    .body(bytes);
        }
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/octet-stream")).body(null);
    }

    public byte[] getFileByte(FunctionalCaseFileRequest request) {
        FunctionalCaseAttachmentExample example = new FunctionalCaseAttachmentExample();
        example.createCriteria().andFileIdEqualTo(request.getFileId()).andCaseIdEqualTo(request.getCaseId());
        List<FunctionalCaseAttachment> caseAttachments = functionalCaseAttachmentMapper.selectByExample(example);
        byte[] bytes = null;
        if (CollectionUtils.isNotEmpty(caseAttachments)) {
            FunctionalCaseAttachment attachment = caseAttachments.getFirst();
            FileRequest fileRequest = new FileRequest();
            fileRequest.setFileName(attachment.getFileName());
            fileRequest.setFolder(DefaultRepositoryDir.getFunctionalCaseDir(request.getProjectId(), request.getCaseId()) + "/" + attachment.getFileId());
            fileRequest.setStorage(StorageType.MINIO.name());
            try {
                bytes = fileService.download(fileRequest);
            } catch (Exception e) {
                throw new MSException("get file error");
            }
        }
        return bytes;
    }

    public FunctionalCaseAttachment getAttachment(FunctionalCaseFileRequest request) {
        FunctionalCaseAttachmentExample example = new FunctionalCaseAttachmentExample();
        example.createCriteria().andFileIdEqualTo(request.getFileId()).andCaseIdEqualTo(request.getCaseId());
        List<FunctionalCaseAttachment> caseAttachments = functionalCaseAttachmentMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(caseAttachments)) {
            return caseAttachments.getFirst();
        }
        return new FunctionalCaseAttachment();
    }

    public Map<String, List<FileAssociation>> getFileAssociationByCaseIds(List<String> ids) {
        List<FileAssociation> fileAssociations = fileAssociationService.getFileAssociations(ids, FileAssociationSourceUtil.SOURCE_TYPE_FUNCTIONAL_CASE);
        Map<String, List<FileAssociation>> fileAssociationMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(fileAssociations)) {
            fileAssociationMap = fileAssociations.stream().collect(Collectors.groupingBy(FileAssociation::getSourceId));
        }
        return fileAssociationMap;
    }

    public void uploadOrAssociationFile(FunctionalCaseAssociationFileRequest request, MultipartFile file, String userId) {
        Optional.ofNullable(file).ifPresent(item -> this.uploadFile(request.getProjectId(), request.getCaseId(), Arrays.asList(file), Boolean.TRUE, userId));

        if (CollectionUtils.isNotEmpty(request.getFileIds())) {
            this.association(request.getFileIds(), request.getCaseId(), userId, UPLOAD_FILE, request.getProjectId());
        }
    }

    public void deleteFile(FunctionalCaseDeleteFileRequest request, String userId) {
        if (BooleanUtils.isTrue(request.getLocal())) {
            this.deleteCaseAttachment(Arrays.asList(request.getId()), request.getCaseId(), userId);
        } else {
            this.unAssociation(request.getCaseId(), Arrays.asList(request.getId()), DELETED_FILE, userId, request.getProjectId());
        }
    }

    public void uploadMinioFile(String caseId, String projectId, List<String> uploadFileIds, String userId, String fileSource) {
        if (CollectionUtils.isEmpty(uploadFileIds)) {
            return;
        }
        //过滤已上传过的
        FunctionalCaseAttachmentExample functionalCaseAttachmentExample = new FunctionalCaseAttachmentExample();
        functionalCaseAttachmentExample.createCriteria().andCaseIdEqualTo(caseId).andFileIdIn(uploadFileIds).andFileSourceEqualTo(fileSource);
        List<FunctionalCaseAttachment> existFiles = functionalCaseAttachmentMapper.selectByExample(functionalCaseAttachmentExample);
        List<String> existFileIds = existFiles.stream().map(FunctionalCaseAttachment::getFileId).distinct().toList();
        List<String> filIds = uploadFileIds.stream().filter(t -> !existFileIds.contains(t) && StringUtils.isNotBlank(t)).toList();
        if (CollectionUtils.isEmpty(filIds)) {
            return;
        }
        String functionalCaseDir = DefaultRepositoryDir.getFunctionalCaseDir(projectId, caseId);
        // 处理本地上传文件
        FileRepository defaultRepository = FileCenter.getDefaultRepository();
        String systemTempDir = DefaultRepositoryDir.getSystemTempDir();
        // 添加文件与功能用例的关联关系
        Map<String, String> addFileMap = new HashMap<>();
        LogUtils.info("开始上传富文本里的附件");
        List<FunctionalCaseAttachment> functionalCaseAttachments = filIds.stream().map(fileId -> {
            FunctionalCaseAttachment functionalCaseAttachment = new FunctionalCaseAttachment();
            String fileName = getTempFileNameByFileId(fileId);
            functionalCaseAttachment.setId(IDGenerator.nextStr());
            functionalCaseAttachment.setCaseId(caseId);
            functionalCaseAttachment.setFileId(fileId);
            functionalCaseAttachment.setFileName(fileName);
            functionalCaseAttachment.setFileSource(fileSource);
            long fileSize = 0;
            try {
                FileCopyRequest fileCopyRequest = new FileCopyRequest();
                fileCopyRequest.setFolder(systemTempDir + "/" + fileId);
                fileCopyRequest.setFileName(fileName);
                fileSize = defaultRepository.getFileSize(fileCopyRequest);
            } catch (Exception e) {
                LogUtils.error("读取文件大小失败");
            }
            functionalCaseAttachment.setSize(fileSize);
            functionalCaseAttachment.setLocal(true);
            functionalCaseAttachment.setCreateUser(userId);
            functionalCaseAttachment.setCreateTime(System.currentTimeMillis());
            addFileMap.put(fileId, fileName);
            return functionalCaseAttachment;
        }).toList();
        functionalCaseAttachmentMapper.batchInsert(functionalCaseAttachments);
        // 上传文件到对象存储
        LogUtils.info("上传文件到对象存储");
        uploadFileResource(functionalCaseDir, addFileMap, projectId, caseId);
        LogUtils.info("上传富文本里的附件结束");
    }

    /**
     * 根据文件ID，查询minio中对应目录下的文件名称
     */
    public String getTempFileNameByFileId(String fileId) {
        return commonFileService.getTempFileNameByFileId(fileId);
    }

    /**
     * 上传用例管理相关的资源文件
     *
     * @param folder  用例管理文件路径
     * @param fileMap key:fileId value:fileName
     */
    public void uploadFileResource(String folder, Map<String, String> fileMap, String projectId, String caseId) {
        if (MapUtils.isEmpty(fileMap)) {
            return;
        }
        for (String fileId : fileMap.keySet()) {
            try {
                String fileName = fileMap.get(fileId);
                if (StringUtils.isEmpty(fileName)) {
                    continue;
                }
                // 将临时文件移动到指定文件夹
                commonFileService.moveTempFileToFolder(fileId, fileName, folder);
                // 将文件从临时目录移动到指定的图片预览目录
                commonFileService.moveTempFileToImgReviewFolder(DefaultRepositoryDir.getFunctionalCasePreviewDir(projectId, caseId), fileId, fileName);
                // 这里不删除临时文件，批量评审需要保留，copy多次文件到正式目录
            } catch (Exception e) {
                LogUtils.error(e);
                throw new MSException(Translator.get("file_upload_fail"), e);
            }
        }
    }

    /**
     * 预览图片
     *
     * @param fileId 文件ID
     */
    public ResponseEntity<byte[]> downloadImgById(String projectId, String fileId, boolean compressed) {
        byte[] bytes;
        String fileName;
        FunctionalCaseAttachmentExample example = new FunctionalCaseAttachmentExample();
        example.createCriteria().andFileIdEqualTo(fileId);
        List<FunctionalCaseAttachment> caseAttachments = functionalCaseAttachmentMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(caseAttachments)) {
            //在临时文件获取
            fileName = getTempFileNameByFileId(fileId);
            bytes = commonFileService.downloadTempImg(fileId, fileName, compressed);
        } else {
            //在正式目录获取
            FunctionalCaseAttachment attachment = caseAttachments.getFirst();
            fileName = attachment.getFileName();
            FileRequest fileRequest = new FileRequest();
            fileRequest.setFileName(attachment.getFileName());
            if (compressed) {
                fileRequest.setFolder(DefaultRepositoryDir.getFunctionalCasePreviewDir(projectId, attachment.getCaseId()) + "/" + attachment.getFileId());
            } else {
                fileRequest.setFolder(DefaultRepositoryDir.getFunctionalCaseDir(projectId, attachment.getCaseId()) + "/" + attachment.getFileId());
            }
            fileRequest.setStorage(StorageType.MINIO.name());
            try {
                bytes = fileService.download(fileRequest);
            } catch (Exception e) {
                throw new MSException("get file error");
            }
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(bytes);
    }
}