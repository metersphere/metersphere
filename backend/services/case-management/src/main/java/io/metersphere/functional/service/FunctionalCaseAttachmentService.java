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
import io.metersphere.project.service.FileService;
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
        List<String>fileIds = new ArrayList<>();
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

        if (CollectionUtils.isNotEmpty(attachmentDTOs)) {
            List<String> userList = attachmentDTOs.stream().map(FunctionalCaseAttachmentDTO::getCreateUser).toList();
            UserExample userExample = new UserExample();
            userExample.createCriteria().andIdIn(userList);
            List<User> users = userMapper.selectByExample(userExample);
            Map<String, String> collect = users.stream().collect(Collectors.toMap(User::getId, User::getName));
            attachmentDTOs.forEach(item -> {
                String userName = collect.get(item.getCreateUser());
                item.setCreateUserName(userName);
            });
        }
        functionalCaseDetailDTO.setAttachments(attachmentDTOs);
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
                .logModule(OperationLogModule.FUNCTIONAL_CASE)
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
            FunctionalCaseAttachment attachment = caseAttachments.get(0);
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
            FunctionalCaseAttachment attachment = caseAttachments.get(0);
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
            return caseAttachments.get(0);
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


    public String uploadTemp(MultipartFile file) {
        String fileName = StringUtils.trim(file.getOriginalFilename());
        if (file.getSize() > maxFileSize.toBytes()) {
            throw new MSException(Translator.get("file.size.is.too.large"));
        }
        if (StringUtils.isBlank(fileName)) {
            throw new MSException(Translator.get("file.name.cannot.be.empty"));
        }
        String fileId = IDGenerator.nextStr();
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFileName(file.getOriginalFilename());
        String systemTempDir = DefaultRepositoryDir.getSystemTempDir();
        fileRequest.setFolder(systemTempDir + "/" + fileId);
        try {
            FileCenter.getDefaultRepository()
                    .saveFile(file, fileRequest);
            String fileType = StringUtils.substring(fileName, fileName.lastIndexOf(".") + 1);

            if (TempFileUtils.isImage(fileType)) {
                //图片文件自动生成预览图
                byte[] previewImg = TempFileUtils.compressPic(file.getBytes());
                fileRequest.setFolder(DefaultRepositoryDir.getSystemTempCompressDir() + "/" + fileId);
                fileRequest.setStorage(StorageType.MINIO.toString());
                fileService.upload(previewImg, fileRequest);
            }
        } catch (Exception e) {
            LogUtils.error(e);
            throw new MSException(Translator.get("file_upload_fail"));
        }
        return fileId;
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
        uploadFileResource(functionalCaseDir, addFileMap, projectId, caseId);
    }

    /**
     * 根据文件ID，查询minio中对应目录下的文件名称
     */
    public String getTempFileNameByFileId(String fileId) {
        FileRepository defaultRepository = FileCenter.getDefaultRepository();
        try {
            FileRequest fileRequest = new FileRequest();
            fileRequest.setFolder(DefaultRepositoryDir.getSystemTempDir() + "/" + fileId);
            List<String> folderFileNames = defaultRepository.getFolderFileNames(fileRequest);
            if (CollectionUtils.isEmpty(folderFileNames)) {
                return null;
            }
            String[] pathSplit = folderFileNames.get(0).split("/");
            return pathSplit[pathSplit.length - 1];

        } catch (Exception e) {
            LogUtils.error(e);
            return null;
        }
    }

    /**
     * 上传用例管理相关的资源文件
     *
     * @param folder     用例管理文件路径
     * @param addFileMap key:fileId value:fileName
     */
    public void uploadFileResource(String folder, Map<String, String> addFileMap, String projectId, String caseId) {
        if (MapUtils.isEmpty(addFileMap)) {
            return;
        }
        FileRepository defaultRepository = FileCenter.getDefaultRepository();
        for (String fileId : addFileMap.keySet()) {
            String systemTempDir = DefaultRepositoryDir.getSystemTempDir();
            try {
                String fileName = addFileMap.get(fileId);
                if (StringUtils.isEmpty(fileName)) {
                    continue;
                }
                // 按ID建文件夹，避免文件名重复
                FileCopyRequest fileCopyRequest = new FileCopyRequest();
                fileCopyRequest.setCopyFolder(systemTempDir + "/" + fileId);
                fileCopyRequest.setCopyfileName(fileName);
                fileCopyRequest.setFileName(fileName);
                fileCopyRequest.setFolder(folder + "/" + fileId);
                // 将文件从临时目录复制到资源目录
                defaultRepository.copyFile(fileCopyRequest);

                String fileType = StringUtils.substring(fileName, fileName.lastIndexOf(".") + 1);
                if (TempFileUtils.isImage(fileType)) {
                    //图片文件自动生成预览图
                    byte[] file = defaultRepository.getFile(fileCopyRequest);
                    byte[] previewImg = TempFileUtils.compressPic(file);
                    fileCopyRequest.setFolder(DefaultRepositoryDir.getFunctionalCasePreviewDir(projectId, caseId) + "/" + fileId);
                    fileCopyRequest.setStorage(StorageType.MINIO.toString());
                    fileService.upload(previewImg, fileCopyRequest);
                }
                // 删除临时文件
                fileCopyRequest.setFolder(systemTempDir + "/" + fileId);
                fileCopyRequest.setFileName(fileName);
                defaultRepository.delete(fileCopyRequest);
            } catch (Exception e) {
                LogUtils.error(e);
                throw new MSException(Translator.get("file_upload_fail"));
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
            bytes = getPreviewImg(fileName, fileId, compressed);
        } else {
            //在正式目录获取
            FunctionalCaseAttachment attachment = caseAttachments.get(0);
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

    public byte[] getPreviewImg(String fileName, String fileId, boolean isCompressed) {
        String systemTempDir;
        if (isCompressed) {
            systemTempDir = DefaultRepositoryDir.getSystemTempCompressDir();
        } else {
            systemTempDir = DefaultRepositoryDir.getSystemTempDir();
        }
        FileRequest previewRequest = new FileRequest();
        previewRequest.setFileName(fileName);
        previewRequest.setStorage(StorageType.MINIO.name());
        previewRequest.setFolder(systemTempDir + "/" + fileId);
        byte[] previewImg = null;
        try {
            previewImg = fileService.download(previewRequest);
        } catch (Exception e) {
            LogUtils.error("获取预览图失败：{}", e);
        }

        if (previewImg == null || previewImg.length == 0) {
            try {
                if (isCompressed) {
                    previewImg = this.compressPicWithFileMetadata(fileName, fileId);
                    previewRequest.setFolder(DefaultRepositoryDir.getSystemTempCompressDir() + "/" + fileId);
                    fileService.upload(previewImg, previewRequest);
                }
                return previewImg;
            } catch (Exception e) {
                LogUtils.error("获取预览图失败：{}", e);
            }
        }
        return previewImg;
    }

    //获取文件并压缩的方法需要上锁，防止并发超过一定数量时内存溢出
    private synchronized byte[] compressPicWithFileMetadata(String fileName, String fileId) throws Exception {
        byte[] fileBytes = this.getFile(fileName, fileId);
        return TempFileUtils.compressPic(fileBytes);
    }

    public byte[] getFile(String fileName, String fileId) throws Exception {
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFileName(fileName);
        fileRequest.setFolder(DefaultRepositoryDir.getSystemTempDir() + "/" + fileId);
        fileRequest.setStorage(StorageType.MINIO.name());
        return fileService.download(fileRequest);
    }

}