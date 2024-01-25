package io.metersphere.api.service;

import io.metersphere.api.constants.ApiResourceType;
import io.metersphere.api.domain.ApiFileResource;
import io.metersphere.api.domain.ApiFileResourceExample;
import io.metersphere.api.dto.debug.ApiFileResourceUpdateRequest;
import io.metersphere.api.mapper.ApiFileResourceMapper;
import io.metersphere.project.dto.filemanagement.FileLogRecord;
import io.metersphere.project.service.FileAssociationService;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.file.FileCenter;
import io.metersphere.sdk.file.FileCopyRequest;
import io.metersphere.sdk.file.FileRepository;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-16  16:49
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ApiFileResourceService {
    @Resource
    private ApiFileResourceMapper apiFileResourceMapper;
    @Resource
    private FileAssociationService fileAssociationService;

    /**
     * 上传接口相关的资源文件
     *
     * @param folder
     * @param addFileMap key:fileId value:fileName
     */
    public void uploadFileResource(String folder, Map<String, String> addFileMap) {
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
     * 根据文件ID，查询minio中对应目录下的文件名称
     */
    public String getTempFileNameByFileId(String fileId) {
        FileRepository defaultRepository = FileCenter.getDefaultRepository();
        String systemTempDir = DefaultRepositoryDir.getSystemTempDir();
        try {
            FileRequest fileRequest = new FileRequest();
            fileRequest.setFolder(systemTempDir + "/" + fileId);
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
     * 添加接口与文件的关联关系
     */
    public void addFileResource(ApiFileResourceUpdateRequest resourceUpdateRequest) {
        List<String> uploadFileIds = resourceUpdateRequest.getUploadFileIds();
        String resourceId = resourceUpdateRequest.getResourceId();
        String projectId = resourceUpdateRequest.getProjectId();
        ApiResourceType apiResourceType = resourceUpdateRequest.getApiResourceType();

        // 处理本地上传文件
        if (CollectionUtils.isNotEmpty(uploadFileIds)) {
            // 添加文件与接口的关联关系
            Map<String, String> addFileMap = new HashMap<>();
            List<ApiFileResource> apiFileResources = uploadFileIds.stream().map(fileId -> {
                ApiFileResource apiFileResource = new ApiFileResource();
                String fileName = getTempFileNameByFileId(fileId);
                apiFileResource.setFileId(fileId);
                apiFileResource.setResourceId(resourceId);
                apiFileResource.setResourceType(apiResourceType.name());
                apiFileResource.setProjectId(projectId);
                apiFileResource.setCreateTime(System.currentTimeMillis());
                apiFileResource.setFileName(fileName);
                addFileMap.put(fileId, fileName);
                return apiFileResource;
            }).toList();
            apiFileResourceMapper.batchInsert(apiFileResources);

            // 上传文件到对象存储
            uploadFileResource(resourceUpdateRequest.getFolder(), addFileMap);
        }

        // 处理关联文件
        if (CollectionUtils.isNotEmpty(resourceUpdateRequest.getLinkFileIds())) {
            fileAssociationService.association(resourceId, resourceUpdateRequest.getFileAssociationSourceType(), resourceUpdateRequest.getLinkFileIds(),
                    createFileLogRecord(resourceUpdateRequest.getOperator(), projectId, resourceUpdateRequest.getLogModule()));
        }
    }

    public FileLogRecord createFileLogRecord(String operator, String projectId, String logModule) {
        return FileLogRecord.builder()
                .logModule(logModule)
                .operator(operator)
                .projectId(projectId)
                .build();
    }

    /**
     * 更新接口时，更新接口与文件的关联关系
     */
    public void updateFileResource(ApiFileResourceUpdateRequest resourceUpdateRequest) {
        // 删除没用的文件
        deleteFileResource(resourceUpdateRequest);
        // 上传新的文件
        addFileResource(resourceUpdateRequest);
    }

    private void deleteFileResource(ApiFileResourceUpdateRequest resourceUpdateRequest) {
        // 处理本地上传文件
        List<String> deleteFileIds = resourceUpdateRequest.getDeleteFileIds();
        if (CollectionUtils.isNotEmpty(deleteFileIds)) {
            // 删除关联关系
            ApiFileResourceExample example = new ApiFileResourceExample();
            example.createCriteria()
                    .andResourceIdEqualTo(resourceUpdateRequest.getResourceId())
                    .andFileIdIn(deleteFileIds);
            apiFileResourceMapper.deleteByExample(example);

            deleteFileIds.forEach(fileId -> {
                FileRequest request = new FileRequest();
                // 删除文件所在目录
                request.setFolder(resourceUpdateRequest.getFolder() + "/" + fileId);
                try {
                    FileCenter.getDefaultRepository().deleteFolder(request);
                } catch (Exception e) {
                    LogUtils.error(e);
                }
            });
        }

        // 处理关联文件
        List<String> unLinkRefIds = resourceUpdateRequest.getUnLinkRefIds();
        if (CollectionUtils.isNotEmpty(unLinkRefIds)) {
            fileAssociationService.deleteBySourceIdAndFileIds(resourceUpdateRequest.getResourceId(), unLinkRefIds,
                    createFileLogRecord(resourceUpdateRequest.getOperator(), resourceUpdateRequest.getProjectId(), resourceUpdateRequest.getLogModule()));
        }
    }

    /**
     * 删除资源下所有的文件或者关联关系
     *
     * @param apiDebugDir
     * @param resourceId
     * @param projectId
     * @param operator
     */
    public void deleteByResourceId(String apiDebugDir, String resourceId, String projectId, String operator, String logModule) {
        // 处理本地上传的文件
        ApiFileResourceExample example = new ApiFileResourceExample();
        example.createCriteria()
                .andResourceIdEqualTo(resourceId);
        apiFileResourceMapper.deleteByExample(example);
        FileRequest request = new FileRequest();
        request.setFolder(apiDebugDir);
        try {
            FileCenter.getDefaultRepository().deleteFolder(request);
        } catch (Exception e) {
            LogUtils.error(e);
        }

        // 处理关联文件
        FileLogRecord fileLogRecord = FileLogRecord.builder()
                .logModule(logModule)
                .operator(operator)
                .projectId(projectId)
                .build();
        fileAssociationService.deleteBySourceIds(List.of(resourceId), fileLogRecord);
    }

    public List<ApiFileResource> getByResourceId(String resourceId) {
        ApiFileResourceExample example = new ApiFileResourceExample();
        example.createCriteria()
                .andResourceIdEqualTo(resourceId);
        return apiFileResourceMapper.selectByExample(example);
    }

    /**
     * 上传临时文件
     * system/temp/{fileId}/{fileName}
     */
    public String uploadTempFile(MultipartFile file) {
        String fileId = IDGenerator.nextStr();
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFileName(file.getOriginalFilename());
        String systemTempDir = DefaultRepositoryDir.getSystemTempDir();
        fileRequest.setFolder(systemTempDir + "/" + fileId);
        try {
            FileCenter.getDefaultRepository()
                    .saveFile(file, fileRequest);
        } catch (Exception e) {
            LogUtils.error(e);
            throw new MSException(Translator.get("file_upload_fail"));
        }
        return fileId;
    }

    public void copyFileByResourceId(String sourceId, String sourceFolder, String targetId, String targetFolder) {
        List<ApiFileResource> files = getByResourceId(sourceId);
        if (!files.isEmpty()) {
            FileRepository defaultRepository = FileCenter.getDefaultRepository();
            List<ApiFileResource> apiFileResources = new ArrayList<>();
            files.forEach(item -> {
                try {
                    // 按ID建文件夹，避免文件名重复
                    FileCopyRequest fileCopyRequest = new FileCopyRequest();
                    fileCopyRequest.setCopyFolder(sourceFolder + "/" + item.getFileId());
                    fileCopyRequest.setCopyfileName(item.getFileName());
                    fileCopyRequest.setFileName(item.getFileName());
                    fileCopyRequest.setFolder(targetFolder + "/" + item.getFileId());
                    // 将文件从临时目录复制到资源目录
                    defaultRepository.copyFile(fileCopyRequest);
                    ApiFileResource apiFileResource = new ApiFileResource();
                    apiFileResource.setFileId(item.getFileId());
                    apiFileResource.setResourceId(targetId);
                    apiFileResource.setResourceType(item.getResourceType());
                    apiFileResource.setProjectId(item.getProjectId());
                    apiFileResource.setCreateTime(System.currentTimeMillis());
                    apiFileResource.setFileName(item.getFileName());
                    apiFileResources.add(apiFileResource);
                } catch (Exception e) {
                    LogUtils.error(e);
                    throw new MSException(Translator.get("file_copy_fail"));
                }
            });

            apiFileResourceMapper.batchInsert(apiFileResources);
        }
    }
}
