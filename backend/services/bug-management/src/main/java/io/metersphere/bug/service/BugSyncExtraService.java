package io.metersphere.bug.service;

import io.metersphere.bug.domain.BugLocalAttachment;
import io.metersphere.bug.domain.BugLocalAttachmentExample;
import io.metersphere.bug.dto.response.BugFileDTO;
import io.metersphere.bug.mapper.BugLocalAttachmentMapper;
import io.metersphere.plugin.platform.dto.PlatformAttachment;
import io.metersphere.plugin.platform.dto.request.SyncAttachmentToPlatformRequest;
import io.metersphere.plugin.platform.spi.Platform;
import io.metersphere.project.domain.FileAssociationExample;
import io.metersphere.project.mapper.FileAssociationMapper;
import io.metersphere.project.service.ProjectApplicationService;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.constants.StorageType;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.file.FileCenter;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class BugSyncExtraService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ProjectApplicationService projectApplicationService;
    @Resource
    private BugAttachmentService bugAttachmentService;
    @Resource
    private BugLocalAttachmentMapper bugLocalAttachmentMapper;
    @Resource
    private FileAssociationMapper fileAssociationMapper;

    private static final String SYNC_THIRD_PARTY_ISSUES_KEY = "ISSUE:SYNC";
    private static final String SYNC_THIRD_PARTY_ISSUES_ERROR_KEY = "ISSUE:SYNC:ERROR";

    /**
     * 设置手动同步缺陷唯一Key
     * @param projectId 项目ID
     */
    public void setSyncKey(String projectId) {
        stringRedisTemplate.opsForValue().set(SYNC_THIRD_PARTY_ISSUES_KEY + ":" + projectId, UUID.randomUUID().toString(), 60 * 10L, TimeUnit.SECONDS);
    }

    /**
     * 获取手动同步缺陷唯一Key
     * @param projectId 项目ID
     */
    public String getSyncKey(String projectId) {
        return stringRedisTemplate.opsForValue().get(SYNC_THIRD_PARTY_ISSUES_KEY + ":" + projectId);
    }

    /**
     * 删除手动同步缺陷唯一Key
     * @param projectId 项目ID
     */
    public void deleteSyncKey(String projectId) {
        stringRedisTemplate.delete(SYNC_THIRD_PARTY_ISSUES_KEY + ":" + projectId);
    }

    /**
     * 设置手动同步缺陷错误信息
     * @param projectId 项目ID
     */
    public void setSyncErrorMsg(String projectId, String errorMsg) {
        stringRedisTemplate.opsForValue().set(SYNC_THIRD_PARTY_ISSUES_ERROR_KEY + ":" + projectId, errorMsg, 30L, TimeUnit.SECONDS);
    }

    /**
     * 获取手动同步缺陷错误信息
     * @param projectId 项目ID
     */
    public String getSyncErrorMsg(String projectId) {
        return stringRedisTemplate.opsForValue().get(SYNC_THIRD_PARTY_ISSUES_ERROR_KEY + ":" + projectId);
    }

    /**
     * 删除手动同步缺陷错误信息
     * @param projectId 项目ID
     */
    public void deleteSyncErrorMsg(String projectId) {
        stringRedisTemplate.delete(SYNC_THIRD_PARTY_ISSUES_ERROR_KEY + ":" + projectId);
    }

    /**
     * 同步附件到平台
     * @param platformAttachments 平台附件参数
     * @param projectId 项目ID
     * @param tmpFilePath 临时文件路径
     */
    @Async
    public void syncAttachmentToPlatform(List<SyncAttachmentToPlatformRequest> platformAttachments, String projectId, File tmpFilePath) {
        // 平台缺陷需同步附件
        Platform platform = projectApplicationService.getPlatform(projectId, true);
        platformAttachments.forEach(platform::syncAttachmentToPlatform);
        tmpFilePath.deleteOnExit();
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
            List<BugFileDTO> allBugFiles = bugAttachmentService.getAllBugFiles(bugId);
            Set<String> attachmentsNameSet = allBugFiles.stream().map(BugFileDTO::getFileName).collect(Collectors.toSet());
            for (PlatformAttachment syncAttachment : syncAttachments) {
                String fileName = syncAttachment.getFileName();
                String fileKey = syncAttachment.getFileKey();
                platformAttachmentSet.add(fileName);
                if (!attachmentsNameSet.contains(fileName)) {
                    saveSyncAttachmentToMs(platform, bugId, fileName, fileKey, projectId);
                }
            }

            // 删除Jira中不存在的附件
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
                byte[] bytes;
                try {
                    // upload platform attachment to minio
                    bytes = in.readAllBytes();
                    FileCenter.getDefaultRepository().saveFile(bytes, buildBugFileRequest(projectId, bugId, fileName));
                } catch (Exception e) {
                    throw new MSException(e);
                }
                // save bug attachment relation
                BugLocalAttachment localAttachment = new BugLocalAttachment();
                localAttachment.setId(IDGenerator.nextStr());
                localAttachment.setBugId(bugId);
                localAttachment.setFileId(IDGenerator.nextStr());
                localAttachment.setFileName(fileName);
                localAttachment.setSize((long) bytes.length);
                localAttachment.setCreateTime(System.currentTimeMillis());
                localAttachment.setCreateUser("admin");
                bugLocalAttachmentMapper.insert(localAttachment);
            });
        } catch (Exception e) {
            LogUtils.error(e);
        }
    }

    /**
     * 删除MS中不存在的平台附件
     * @param platformAttachmentSet 已处理的平台附件集合
     * @param allMsAttachments 所有MS附件集合
     * @param bugId 缺陷ID
     * @param projectId 项目ID
     */
    private void deleteSyncAttachmentFromMs(Set<String> platformAttachmentSet, List<BugFileDTO> allMsAttachments, String bugId, String projectId) {
        try {
            // 删除MS中不存在的平台附件
            if (!CollectionUtils.isEmpty(allMsAttachments)) {
                List<BugFileDTO> deleteMsAttachments = allMsAttachments.stream()
                        .filter(msAttachment -> !platformAttachmentSet.contains(msAttachment.getFileName()))
                        .toList();
                List<String> unLinkIds = new ArrayList<>();
                List<String> deleteLocalIds = new ArrayList<>();
                deleteMsAttachments.forEach(deleteMsFile -> {
                    if (deleteMsFile.getAssociated()) {
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
                            FileCenter.getDefaultRepository().delete(buildBugFileRequest(projectId, bugId, bugFileDTO.getFileName()));
                        } catch (Exception e) {
                            throw new MSException(e);
                        }
                    });
                    BugLocalAttachmentExample example = new BugLocalAttachmentExample();
                    example.createCriteria().andIdIn(deleteLocalIds);
                    bugLocalAttachmentMapper.deleteByExample(example);
                }
            }
        } catch (Exception e) {
            LogUtils.error(e);
        }
    }

    /**
     * 构建文件库请求参数
     * @param projectId 项目ID
     * @param resourceId 资源ID(缺陷ID)
     * @param fileName 文件名
     * @return 构建文件库请求对象
     */
    private FileRequest buildBugFileRequest(String projectId, String resourceId, String fileName) {
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFolder(DefaultRepositoryDir.getBugDir(projectId, resourceId));
        fileRequest.setFileName(StringUtils.isEmpty(fileName) ? null : fileName);
        fileRequest.setStorage(StorageType.MINIO.name());
        return fileRequest;
    }
}
