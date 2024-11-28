package io.metersphere.bug.service;

import com.google.common.collect.Maps;
import io.metersphere.bug.domain.Bug;
import io.metersphere.bug.domain.BugLocalAttachment;
import io.metersphere.bug.domain.BugLocalAttachmentExample;
import io.metersphere.bug.dto.request.BugDeleteFileRequest;
import io.metersphere.bug.dto.request.BugFileSourceRequest;
import io.metersphere.bug.dto.request.BugFileTransferRequest;
import io.metersphere.bug.dto.request.BugUploadFileRequest;
import io.metersphere.bug.dto.response.BugFileDTO;
import io.metersphere.bug.enums.BugAttachmentSourceType;
import io.metersphere.bug.enums.BugPlatform;
import io.metersphere.bug.mapper.BugLocalAttachmentMapper;
import io.metersphere.bug.mapper.BugMapper;
import io.metersphere.plugin.platform.dto.PlatformAttachment;
import io.metersphere.plugin.platform.dto.request.SyncAttachmentToPlatformRequest;
import io.metersphere.plugin.platform.enums.SyncAttachmentType;
import io.metersphere.plugin.platform.spi.Platform;
import io.metersphere.project.domain.FileAssociation;
import io.metersphere.project.domain.FileAssociationExample;
import io.metersphere.project.domain.FileMetadata;
import io.metersphere.project.domain.FileMetadataExample;
import io.metersphere.project.dto.filemanagement.FileAssociationDTO;
import io.metersphere.project.dto.filemanagement.FileLogRecord;
import io.metersphere.project.mapper.FileAssociationMapper;
import io.metersphere.project.mapper.FileMetadataMapper;
import io.metersphere.project.service.FileAssociationService;
import io.metersphere.project.service.FileMetadataService;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.constants.LocalRepositoryDir;
import io.metersphere.sdk.constants.StorageType;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.file.FileCenter;
import io.metersphere.sdk.file.FileCopyRequest;
import io.metersphere.sdk.file.FileRepository;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.util.FileAssociationSourceUtil;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.MsFileUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.service.CommonFileService;
import io.metersphere.system.service.FileService;
import io.metersphere.system.service.UserToolService;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author song-cc-rock
 */
@Service
public class BugAttachmentService {

    @Resource
    private BugMapper bugMapper;
    @Resource
    private FileService fileService;
    @Resource
    private FileMetadataMapper fileMetadataMapper;
    @Resource
    private BugPlatformService bugPlatformService;
    @Resource
    private FileMetadataService fileMetadataService;
    @Resource
    private FileAssociationMapper fileAssociationMapper;
    @Resource
    private FileAssociationService fileAssociationService;
    @Resource
    private BugLocalAttachmentMapper bugLocalAttachmentMapper;
    @Resource
    private CommonFileService commonFileService;
    @Resource
    private UserToolService userToolService;

    /**
     * 查询缺陷的附件集合
     * @param bugId 缺陷ID
     * @return 缺陷附件关系集合
     */
    public List<BugFileDTO> getAllBugFiles(String bugId) {
        List<BugFileDTO> bugFiles = new ArrayList<>();
        BugLocalAttachmentExample localAttachmentExample = new BugLocalAttachmentExample();
        localAttachmentExample.createCriteria().andBugIdEqualTo(bugId).andSourceEqualTo(BugAttachmentSourceType.ATTACHMENT.name());
        List<BugLocalAttachment> bugLocalAttachments = bugLocalAttachmentMapper.selectByExample(localAttachmentExample);
        if (!CollectionUtils.isEmpty(bugLocalAttachments)) {
            bugLocalAttachments.forEach(localFile -> {
                BugFileDTO localFileDTO = BugFileDTO.builder().refId(localFile.getId()).fileId(localFile.getFileId()).fileName(localFile.getFileName()).fileType(getLocalFileType(localFile.getFileName()))
                        .fileSize(localFile.getSize()).createTime(localFile.getCreateTime()).createUser(localFile.getCreateUser()).local(true).build();
                bugFiles.add(localFileDTO);
            });
        }
        FileAssociationExample associationExample = new FileAssociationExample();
        associationExample.createCriteria().andSourceIdEqualTo(bugId).andSourceTypeEqualTo(FileAssociationSourceUtil.SOURCE_TYPE_BUG);
        List<FileAssociation> fileAssociations = fileAssociationMapper.selectByExample(associationExample);
        if (!CollectionUtils.isEmpty(fileAssociations)) {
            List<String> associateFileIds = fileAssociations.stream().map(FileAssociation::getFileId).toList();
            Map<String, FileMetadata> fileMetadataMap = getLinkFileMetaMap(associateFileIds);
            fileAssociations.forEach(associatedFile -> {
                FileMetadata associatedFileMetadata = fileMetadataMap.get(associatedFile.getFileId());
                if (associatedFileMetadata != null) {
                    BugFileDTO associatedFileDTO = BugFileDTO.builder().refId(associatedFile.getId()).fileId(associatedFile.getFileId()).fileName(associatedFileMetadata.getName() + "." + associatedFileMetadata.getType())
                            .fileType(associatedFileMetadata.getType()).fileSize(associatedFileMetadata.getSize()).createTime(associatedFileMetadata.getCreateTime())
                            .createUser(associatedFileMetadata.getCreateUser()).local(false).build();
                    bugFiles.add(associatedFileDTO);
                }
            });
        }
        if (CollectionUtils.isEmpty(bugFiles)) {
            return bugFiles;
        }
        List<String> userIds = bugFiles.stream().map(BugFileDTO::getCreateUser).distinct().toList();
        Map<String, String> userMap = userToolService.getUserMapByIds(userIds);
        return bugFiles.stream().peek(file -> file.setCreateUserName(userMap.get(file.getCreateUser()))).toList();
    }

    /**
     * 上传附件->缺陷 (同步至平台)
     * @param request 缺陷关联文件请求参数
     * @param file 文件
     * @param currentUser 当前用户
     */
    public void uploadFile(BugUploadFileRequest request, MultipartFile file, String currentUser) {
        Bug bug = bugMapper.selectByPrimaryKey(request.getBugId());
        List<SyncAttachmentToPlatformRequest> platformAttachments = new ArrayList<>();
        if (file == null) {
            // 缺陷与文件库关联
            if (CollectionUtils.isEmpty(request.getSelectIds())) {
                return;
            }
            List<SyncAttachmentToPlatformRequest> syncLinkFiles = uploadLinkFile(bug.getId(), bug.getPlatformBugId(), request.getProjectId(), request.getSelectIds(), currentUser, bug.getPlatform(), false);
            platformAttachments.addAll(syncLinkFiles);
        } else {
            // 上传文件
            List<SyncAttachmentToPlatformRequest> syncLocalFiles = uploadLocalFile(bug.getId(), bug.getPlatformBugId(), request.getProjectId(), file, currentUser, bug.getPlatform());
            platformAttachments.addAll(syncLocalFiles);
        }

        // 同步至第三方(异步调用)
        if (!StringUtils.equals(bug.getPlatform(), BugPlatform.LOCAL.getName())) {
            bugPlatformService.syncAttachmentToPlatform(platformAttachments, request.getProjectId());
        }
    }

    /**
     * 删除或取消关联附件->缺陷 (同步至平台)
     * @param request 删除文件请求参数
     */
    public void deleteFile(BugDeleteFileRequest request) {
        Bug bug = bugMapper.selectByPrimaryKey(request.getBugId());
        List<SyncAttachmentToPlatformRequest> platformAttachments = new ArrayList<>();
        if (request.getAssociated()) {
            // 取消关联
            List<SyncAttachmentToPlatformRequest> syncLinkFiles = unLinkFile(bug.getPlatformBugId(), request.getProjectId(), request.getRefId(), bug.getCreateUser(), bug.getPlatform(), false);
            platformAttachments.addAll(syncLinkFiles);
        } else {
            // 删除本地上传的文件
            List<SyncAttachmentToPlatformRequest> syncLocalFiles =
                    deleteLocalFile(bug.getId(), bug.getPlatformBugId(), request.getProjectId(), request.getRefId(), bug.getPlatform());
            platformAttachments.addAll(syncLocalFiles);
        }
        // 同步至第三方(异步调用)
        if (!StringUtils.equals(bug.getPlatform(), BugPlatform.LOCAL.getName())) {
            bugPlatformService.syncAttachmentToPlatform(platformAttachments, request.getProjectId());
        }
    }

    /**
     * 下载或预览本地文件
     * @param request 文件请求参数
     * @return 文件字节流
     */
    public ResponseEntity<byte[]> downloadOrPreview(BugFileSourceRequest request) {
        BugLocalAttachment attachment = getLocalFile(request);
        if (attachment == null) {
            return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/octet-stream")).body(null);
        }
        byte[] bytes = getLocalFileBytes(attachment, request.getProjectId(), request.getBugId());
        String name = URLEncoder.encode(attachment.getFileName(), StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + name + "\";" + "filename*=utf-8''"+ name)
                .body(bytes);
    }

    /**
     * 转存附件至文件库
     * @param request 请求参数
     * @param currentUser 当前用户
     * @return 文件ID
     */
    public String transfer(BugFileTransferRequest request, String currentUser) {
        BugLocalAttachment attachment = getLocalFile(request);
        if (attachment == null) {
            throw new MSException(Translator.get("file.transfer.failed"));
        }
        byte[] bytes = getLocalFileBytes(attachment, request.getProjectId(), request.getBugId());
        String fileId;
        try {
            FileAssociationDTO association = new FileAssociationDTO(request.getFileName(), attachment.getFileName(), bytes, attachment.getBugId(),
                    FileAssociationSourceUtil.SOURCE_TYPE_BUG, createFileLogRecord(currentUser, request.getProjectId()));
            association.setModuleId(request.getModuleId());
            fileId = fileAssociationService.transferAndAssociation(association);
            // 删除本地上传的附件
            deleteLocalFile(request.getBugId(), null, request.getProjectId(), attachment.getId(), null);
        } catch (MSException e) {
            throw e;
        } catch (Exception e) {
            throw new MSException(Translator.get("file.transfer.failed"));
        }
        return fileId;
    }

    /**
     * 更新文件至最新版本
     * @param request 请求参数
     * @param currentUser 当前用户
     * @return 文件ID
     */
    public String upgrade(BugDeleteFileRequest request, String currentUser) {
        Bug bug = bugMapper.selectByPrimaryKey(request.getBugId());
        // 取消关联附件->同步
        List<SyncAttachmentToPlatformRequest> syncUnlinkFiles = unLinkFile(bug.getPlatformBugId(), request.getProjectId(), request.getRefId(), currentUser, bug.getPlatform(), true);
        // 更新后的文件需要同步
        String upgradeFileId = fileAssociationService.upgrade(request.getRefId(), createFileLogRecord(currentUser, request.getProjectId()));
        // 关联附件->同步
        List<SyncAttachmentToPlatformRequest> syncLinkFiles = uploadLinkFile(bug.getId(), bug.getPlatformBugId(), request.getProjectId(), List.of(upgradeFileId), currentUser, bug.getPlatform(), true);
        List<SyncAttachmentToPlatformRequest> platformAttachments = Stream.concat(syncUnlinkFiles.stream(), syncLinkFiles.stream()).toList();
        if (!StringUtils.equals(bug.getPlatform(), BugPlatform.LOCAL.getName())) {
            bugPlatformService.syncAttachmentToPlatform(platformAttachments, request.getProjectId());
        }
        return upgradeFileId;
    }

    /**
     * 同步平台附件到MS
     * @param platform 平台对象
     * @param attachmentMap 平台附件缺陷集合
     * @param projectId 项目ID
     */
    @Async
    public void syncAttachmentToMs(Platform platform, Map<String, List<PlatformAttachment>> attachmentMap, String projectId) {
        for (String bugId : attachmentMap.keySet()) {
            List<PlatformAttachment> syncAttachments = attachmentMap.get(bugId);
            // 获取所有MS附件
            Set<String> platformAttachmentSet = new HashSet<>();
            List<BugFileDTO> allBugFiles = getAllBugFiles(bugId);
            Set<String> attachmentsNameSet = allBugFiles.stream().map(BugFileDTO::getFileName).collect(Collectors.toSet());
            for (PlatformAttachment syncAttachment : syncAttachments) {
                String fileName = syncAttachment.getFileName();
                String fileKey = syncAttachment.getFileKey();
                platformAttachmentSet.add(fileName);
                if (!attachmentsNameSet.contains(fileName)) {
                    saveSyncAttachmentToMs(platform, bugId, fileName, fileKey, projectId);
                }
            }

            // 删除三方平台中不存在的附件
            deleteSyncAttachmentFromMs(platformAttachmentSet, allBugFiles, bugId, projectId);
        }
    }

    /**
     * 保存同步附件到MS
     * @param platform 平台对象
     * @param bugId 缺陷ID
     * @param fileName 附件名称
     * @param fileKey 附件唯一Key
     * @param projectId 项目ID
     */
    public void saveSyncAttachmentToMs(Platform platform, String bugId, String fileName, String fileKey, String projectId) {
        try {
            platform.getAttachmentContent(fileKey, (in) -> {
                if (in == null) {
                    return;
                }
                String fileId = IDGenerator.nextStr();
                byte[] bytes;
                try {
                    // upload platform attachment to minio
                    bytes = in.readAllBytes();
                    FileCenter.getDefaultRepository().saveFile(bytes, buildBugFileRequest(projectId, bugId, fileId, fileName, false));
                } catch (Exception e) {
                    throw new MSException(e.getMessage());
                }
                // save bug attachment relation
                BugLocalAttachment localAttachment = new BugLocalAttachment();
                localAttachment.setId(IDGenerator.nextStr());
                localAttachment.setBugId(bugId);
                localAttachment.setFileId(fileId);
                localAttachment.setFileName(fileName);
                localAttachment.setSize((long) bytes.length);
                localAttachment.setCreateTime(System.currentTimeMillis());
                localAttachment.setCreateUser("admin");
                localAttachment.setSource(BugAttachmentSourceType.ATTACHMENT.name());
                bugLocalAttachmentMapper.insert(localAttachment);
            });
        } catch (Exception e) {
            LogUtils.error(e.getMessage());
            throw new MSException(e.getMessage());
        }
    }

    /**
     * 删除MS中不存在的平台附件
     * @param platformAttachmentSet 已处理的平台附件集合
     * @param allMsAttachments 所有MS附件集合
     * @param bugId 缺陷ID
     * @param projectId 项目ID
     */
    public void deleteSyncAttachmentFromMs(Set<String> platformAttachmentSet, List<BugFileDTO> allMsAttachments, String bugId, String projectId) {
        try {
            // 删除MS中不存在的平台附件
            if (!CollectionUtils.isEmpty(allMsAttachments)) {
                List<BugFileDTO> deleteMsAttachments = allMsAttachments.stream()
                        .filter(msAttachment -> !platformAttachmentSet.contains(msAttachment.getFileName()))
                        .toList();
                List<String> unLinkIds = new ArrayList<>();
                List<String> deleteLocalIds = new ArrayList<>();
                deleteMsAttachments.forEach(deleteMsFile -> {
                    if (!deleteMsFile.getLocal()) {
                        unLinkIds.add(deleteMsFile.getRefId());
                    } else {
                        deleteLocalIds.add(deleteMsFile.getRefId());
                    }
                });
                if (!CollectionUtils.isEmpty(unLinkIds)) {
                    FileAssociationExample example = new FileAssociationExample();
                    example.createCriteria().andIdIn(unLinkIds);
                    fileAssociationMapper.deleteByExample(example);
                }
                if (!CollectionUtils.isEmpty(deleteLocalIds)) {
                    Map<String, BugFileDTO> localFileMap = deleteMsAttachments.stream().collect(Collectors.toMap(BugFileDTO::getRefId, f -> f));
                    deleteLocalIds.forEach(deleteLocalId -> {
                        try {
                            BugFileDTO bugFileDTO = localFileMap.get(deleteLocalId);
                            FileCenter.getDefaultRepository().delete(buildBugFileRequest(projectId, bugId, bugFileDTO.getFileId(), bugFileDTO.getFileName(), false));
                        } catch (Exception e) {
                            throw new MSException(e.getMessage());
                        }
                    });
                    BugLocalAttachmentExample example = new BugLocalAttachmentExample();
                    example.createCriteria().andIdIn(deleteLocalIds);
                    bugLocalAttachmentMapper.deleteByExample(example);
                }
            }
        } catch (Exception e) {
            LogUtils.error(e.getMessage());
            throw new MSException(e.getMessage());
        }
    }

    /**
     * 获取本地文件字节流
     * @param attachment 本地附件信息
     * @param projectId 项目ID
     * @param bugId 缺陷ID
     * @return 文件字节流
     */
    public byte[] getLocalFileBytes(BugLocalAttachment attachment, String projectId, String bugId) {
        FileRequest fileRequest = buildBugFileRequest(projectId, bugId, attachment.getFileId(), attachment.getFileName(), false);
        byte[] bytes;
        try {
            bytes = fileService.download(fileRequest);
        } catch (Exception e) {
            throw new MSException("download file error!");
        }
        return bytes;
    }

    /**
     * 上传关联的文件(同步至平台)
     * @param bugId 缺陷ID
     * @param platformBugKey 平台缺陷ID
     * @param projectId 项目ID
     * @param linkFileIds 关联文件ID集合
     * @param currentUser 创建人
     * @param platformName 平台名称
     * @return 同步至平台的附件集合
     */
    private List<SyncAttachmentToPlatformRequest> uploadLinkFile(String bugId, String platformBugKey, String projectId,
                                                                 List<String> linkFileIds, String currentUser, String platformName, boolean syncOnly) {
        if (!syncOnly) {
            fileAssociationService.association(bugId, FileAssociationSourceUtil.SOURCE_TYPE_BUG, linkFileIds, createFileLogRecord(currentUser, projectId));
        }
        // 同步新关联的附件至平台
        List<SyncAttachmentToPlatformRequest> linkSyncFiles = new ArrayList<>();
        if (!StringUtils.equals(platformName, BugPlatform.LOCAL.getName())) {
            Map<String, FileMetadata> fileMetadataMap = getLinkFileMetaMap(linkFileIds);
            linkFileIds.forEach(fileId -> {
                // 平台同步附件集合
                FileMetadata meta = fileMetadataMap.get(fileId);
                if (meta != null) {
                    try {
                        File uploadTmpFile = new File(FilenameUtils.normalize(LocalRepositoryDir.getBugTmpDir() + File.separator + meta.getName() + "." + meta.getType()));
                        byte[] fileByte = fileMetadataService.getFileByte(meta);
                        FileUtils.writeByteArrayToFile(uploadTmpFile, fileByte);
                        linkSyncFiles.add(new SyncAttachmentToPlatformRequest(platformBugKey, uploadTmpFile, SyncAttachmentType.UPLOAD.syncOperateType()));
                    } catch (IOException e) {
                        throw new MSException(Translator.get("bug_attachment_upload_error"));
                    }
                }
            });
        }
        return linkSyncFiles;
    }

    /**
     * 上传本地的文件(同步至平台)
     * @param bugId 缺陷ID
     * @param platformBugKey 平台缺陷ID
     * @param projectId 项目ID
     * @param file 上传的本地文件
     * @param currentUser 创建人
     * @param platformName 平台名称
     * @return 同步至平台的附件集合
     */
    private List<SyncAttachmentToPlatformRequest> uploadLocalFile(String bugId, String platformBugKey, String projectId,
                                                                  MultipartFile file, String currentUser, String platformName) {
        MsFileUtils.validateFileName(file.getOriginalFilename());
        BugLocalAttachment record = new BugLocalAttachment();
        record.setId(IDGenerator.nextStr());
        record.setBugId(bugId);
        record.setFileId(IDGenerator.nextStr());
        record.setFileName(file.getOriginalFilename());
        record.setSize(file.getSize());
        record.setSource(BugAttachmentSourceType.ATTACHMENT.name());
        record.setCreateTime(System.currentTimeMillis());
        record.setCreateUser(currentUser);
        bugLocalAttachmentMapper.insert(record);
        List<SyncAttachmentToPlatformRequest> localSyncFiles = new ArrayList<>();
        FileRequest fileRequest = buildBugFileRequest(projectId, bugId, record.getFileId(), file.getOriginalFilename(), false);
        try {
            fileService.upload(file, fileRequest);
            if (!StringUtils.equals(platformName, BugPlatform.LOCAL.getName())) {
                // 非本地平台，同步附件到平台
                File uploadTmpFile = new File(FilenameUtils.normalize(LocalRepositoryDir.getBugTmpDir() + File.separator + file.getOriginalFilename()));
                FileUtils.writeByteArrayToFile(uploadTmpFile, file.getBytes());
                localSyncFiles.add(new SyncAttachmentToPlatformRequest(platformBugKey, uploadTmpFile, SyncAttachmentType.UPLOAD.syncOperateType()));
            }
        } catch (Exception e) {
            throw new MSException(Translator.get("bug_attachment_upload_error"));
        }
        return localSyncFiles;
    }

    /**
     * 取消关联文件(同步至平台)
     * @param platformBugKey 平台缺陷ID
     * @param projectId 项目ID
     * @param refId 取消关联的文件引用ID
     * @param currentUser 创建人
     * @param platformName 平台名称
     * @return 同步至平台的附件集合
     */
    private List<SyncAttachmentToPlatformRequest> unLinkFile(String platformBugKey, String projectId,
                                                                 String refId, String currentUser, String platformName, boolean syncOnly) {
        List<SyncAttachmentToPlatformRequest> linkSyncFiles = new ArrayList<>();
        FileAssociation association = fileAssociationMapper.selectByPrimaryKey(refId);
        FileMetadataExample example = new FileMetadataExample();
        example.createCriteria().andIdEqualTo(association.getFileId());
        FileMetadata fileMetadata = fileMetadataMapper.selectByExample(example).getFirst();
        // 取消关联的附件同步至平台
        if (!StringUtils.equals(platformName, BugPlatform.LOCAL.getName())) {
            File deleteTmpFile = new File(FilenameUtils.normalize(LocalRepositoryDir.getBugTmpDir() + File.separator + fileMetadata.getName() + "." + fileMetadata.getType()));
            linkSyncFiles.add(new SyncAttachmentToPlatformRequest(platformBugKey, deleteTmpFile, SyncAttachmentType.DELETE.syncOperateType()));
        }
        // 取消关联的附件, FILE_ASSOCIATION表
        if (!syncOnly) {
            fileAssociationService.deleteByIds(List.of(refId), createFileLogRecord(currentUser, projectId));
        }
        return linkSyncFiles;
    }

    /**
     * 删除本地上传的文件(同步至平台)
     * @param bugId 缺陷ID
     * @param platformBugKey 平台缺陷ID
     * @param projectId 项目ID
     * @param refId 关联ID
     * @param platformName 平台名称
     * @return 同步至平台的附件集合
     */
    private List<SyncAttachmentToPlatformRequest> deleteLocalFile(String bugId, String platformBugKey, String projectId,
                                 String refId, String platformName) {
        List<SyncAttachmentToPlatformRequest> syncLocalFiles = new ArrayList<>();
        BugLocalAttachment localAttachment = bugLocalAttachmentMapper.selectByPrimaryKey(refId);
        // 删除本地上传的附件, BUG_LOCAL_ATTACHMENT表
        FileRequest fileRequest = buildBugFileRequest(projectId, bugId, localAttachment.getFileId(), localAttachment.getFileName(), false);
        try {
            // 删除MINIO附件
            fileService.deleteFile(fileRequest);
            // 删除的本地的附件同步至平台
            if (!StringUtils.equals(platformName, BugPlatform.LOCAL.getName())) {
                File deleteTmpFile = new File(FilenameUtils.normalize(LocalRepositoryDir.getBugTmpDir() + File.separator + localAttachment.getFileName()));
                syncLocalFiles.add(new SyncAttachmentToPlatformRequest(platformBugKey, deleteTmpFile, SyncAttachmentType.DELETE.syncOperateType()));
            }
        } catch (Exception e) {
            throw new MSException(Translator.get("bug_attachment_delete_error"));
        }
        bugLocalAttachmentMapper.deleteByPrimaryKey(refId);
        return syncLocalFiles;
    }



    /**
     * 获取本地文件类型
     * @param fileName 文件名
     * @return 文件类型
     */
    private String getLocalFileType(String fileName) {
        int i = fileName.lastIndexOf(".");
        if (i > 0) {
            return fileName.substring(i + 1);
        } else {
            return StringUtils.EMPTY;
        }
    }

    /**
     *
     * @param operator 操作人
     * @param projectId 项目ID
     * @return 文件操作日志记录
     */
    private FileLogRecord createFileLogRecord(String operator, String projectId){
        return FileLogRecord.builder()
                .logModule(OperationLogModule.BUG_MANAGEMENT_INDEX)
                .operator(operator)
                .projectId(projectId)
                .build();
    }

    /**
     * 构建缺陷文件请求
     * @param projectId 项目ID
     * @param resourceId 资源ID
     * @param fileId 文件ID
     * @param fileName 文件名称
     * @param compress 是否压缩预览目录
     * @return 文件请求对象
     */
    private FileRequest buildBugFileRequest(String projectId, String resourceId, String fileId, String fileName, boolean compress) {
        FileRequest fileRequest = new FileRequest();
        if (compress) {
            fileRequest.setFolder(DefaultRepositoryDir.getBugPreviewDir(projectId, resourceId) + "/" + fileId);
        } else {
            fileRequest.setFolder(DefaultRepositoryDir.getBugDir(projectId, resourceId) + "/" + fileId);
        }
        fileRequest.setFileName(StringUtils.isEmpty(fileName) ? null : fileName);
        fileRequest.setStorage(StorageType.MINIO.name());
        return fileRequest;
    }

    /**
     * 获取关联文件数据
     * @param linkFileIds 关联文件ID集合
     * @return 文件集合
     */
    private Map<String, FileMetadata> getLinkFileMetaMap(List<String> linkFileIds) {
        FileMetadataExample metadataExample = new FileMetadataExample();
        metadataExample.createCriteria().andIdIn(linkFileIds);
        List<FileMetadata> fileMetadataList = fileMetadataMapper.selectByExample(metadataExample);
        return fileMetadataList.stream().collect(Collectors.toMap(FileMetadata::getId, v -> v));
    }

    /**
     * 获取本地文件
     * @param request 请求参数
     * @return 本地文件信息
     */
    private BugLocalAttachment getLocalFile(BugFileSourceRequest request) {
        BugLocalAttachmentExample example = new BugLocalAttachmentExample();
        example.createCriteria().andFileIdEqualTo(request.getFileId()).andBugIdEqualTo(request.getBugId());
        List<BugLocalAttachment> bugLocalAttachments = bugLocalAttachmentMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(bugLocalAttachments)) {
            return null;
        }
        return bugLocalAttachments.getFirst();
    }

    /**
     * 转存临时文件
     * @param bugId 缺陷ID
     * @param projectId 项目ID
     * @param uploadFileIds 上传的文件ID集合
     * @param userId 用户ID
     * @param source 文件来源
     */
    public void transferTmpFile(String bugId, String projectId, List<String> uploadFileIds, String userId, String source) {
        if (CollectionUtils.isEmpty(uploadFileIds)) {
            return;
        }
        //过滤已上传过的
        BugLocalAttachmentExample bugLocalAttachmentExample = new BugLocalAttachmentExample();
        bugLocalAttachmentExample.createCriteria().andBugIdEqualTo(bugId).andFileIdIn(uploadFileIds).andSourceEqualTo(source);
        List<BugLocalAttachment> existFiles = bugLocalAttachmentMapper.selectByExample(bugLocalAttachmentExample);
        List<String> existFileIds = existFiles.stream().map(BugLocalAttachment::getFileId).distinct().toList();
        List<String> fileIds = uploadFileIds.stream().filter(t -> !existFileIds.contains(t) && StringUtils.isNotBlank(t)).toList();
        if (CollectionUtils.isEmpty(fileIds)) {
            return;
        }
        // 处理本地上传文件
        FileRepository defaultRepository = FileCenter.getDefaultRepository();
        String systemTempDir = DefaultRepositoryDir.getSystemTempDir();
        // 添加文件与功能用例的关联关系
        Map<String, String> addFileMap = Maps.newHashMapWithExpectedSize(8);
        LogUtils.info("开始上传富文本里的附件");
        List<BugLocalAttachment> localAttachments = new ArrayList<>();
        for (String fileId : fileIds) {
            String fileName = getTempFileNameByFileId(fileId);
            if (StringUtils.isEmpty(fileName)) {
                continue;
            }
            BugLocalAttachment localAttachment = new BugLocalAttachment();
            localAttachment.setId(IDGenerator.nextStr());
            localAttachment.setBugId(bugId);
            localAttachment.setFileId(fileId);
            localAttachment.setFileName(fileName);
            localAttachment.setSource(source);
            long fileSize = 0;
            try {
                FileCopyRequest fileCopyRequest = new FileCopyRequest();
                fileCopyRequest.setFolder(systemTempDir + "/" + fileId);
                fileCopyRequest.setFileName(fileName);
                fileSize = defaultRepository.getFileSize(fileCopyRequest);
            } catch (Exception e) {
                LogUtils.error("读取文件大小失败");
            }
            localAttachment.setSize(fileSize);
            localAttachment.setCreateUser(userId);
            localAttachment.setCreateTime(System.currentTimeMillis());
            addFileMap.put(fileId, fileName);
            localAttachments.add(localAttachment);
        }
        if (!CollectionUtils.isEmpty(localAttachments)) {
            bugLocalAttachmentMapper.batchInsert(localAttachments);
        }
        // 上传文件到对象存储
        LogUtils.info("upload to minio start");
        String bugDir = DefaultRepositoryDir.getBugDir(projectId, bugId);
        String bugPreviewDir = DefaultRepositoryDir.getBugPreviewDir(projectId, bugId);
        commonFileService.saveReviewImgFromTempFile(bugDir, bugPreviewDir, addFileMap);
        LogUtils.info("upload to minio end");
    }

    /**
     * 根据文件ID，查询MINIO中对应目录下的文件名称
     */
    public String getTempFileNameByFileId(String fileId) {
        return commonFileService.getTempFileNameByFileId(fileId);
    }

    public ResponseEntity<byte[]> previewMd(String projectId, String fileId, boolean compressed) {
        byte[] bytes;
        String fileName;
        BugLocalAttachmentExample example = new BugLocalAttachmentExample();
        example.createCriteria().andFileIdEqualTo(fileId);
        List<BugLocalAttachment> bugAttachments = bugLocalAttachmentMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(bugAttachments)) {
            //在临时文件获取
            fileName = getTempFileNameByFileId(fileId);
            bytes = commonFileService.downloadTempImg(fileId, fileName, compressed);
        } else {
            //在正式目录获取
            BugLocalAttachment attachment = bugAttachments.getFirst();
            fileName = attachment.getFileName();
            FileRequest fileRequest = buildBugFileRequest(projectId, attachment.getBugId(), attachment.getFileId(), attachment.getFileName(), compressed);
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
