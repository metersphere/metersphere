package io.metersphere.api.service;

import io.metersphere.api.constants.ApiResourceType;
import io.metersphere.api.domain.ApiFileResource;
import io.metersphere.api.domain.ApiFileResourceExample;
import io.metersphere.api.dto.debug.ApiFileResourceUpdateRequest;
import io.metersphere.api.mapper.ApiFileResourceMapper;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.file.FileCenter;
import io.metersphere.system.file.FileRequest;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-16  16:49
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ApiFileResourceService {
    @Resource
    private ApiFileResourceMapper apiFileResourceMapper;

    /**
     * 上传接口相关的资源文件
     * @param folder
     * @param files
     */
    public void uploadFileResource(String folder, List<String> addFileIds, List<MultipartFile> files) {
        if (CollectionUtils.isEmpty(files) || CollectionUtils.isEmpty(addFileIds)) {
            return;
        }
        int size = addFileIds.size() > files.size() ? addFileIds.size() : files.size();
        for (int i = 0; i < size; i++) {
            String fileId = addFileIds.get(i);
            MultipartFile file = files.get(i);
            FileRequest fileRequest = new FileRequest();
            fileRequest.setFileName(file.getOriginalFilename());
            // 按ID建文件夹，避免文件名重复
            fileRequest.setFolder(folder + "/" + fileId);
            try {
                FileCenter.getDefaultRepository()
                        .saveFile(file, fileRequest);
            } catch (Exception e) {
                LogUtils.error(e);
                throw new MSException(Translator.get("file_upload_fail"));
            }
        }
    }

    /**
     * 添加接口与文件的关联关系
     */
    public void addFileResource(ApiFileResourceUpdateRequest resourceUpdateRequest, List<MultipartFile> files) {
        List<String> addFileIds = resourceUpdateRequest.getAddFileIds();
        String resourceId = resourceUpdateRequest.getResourceId();
        String projectId = resourceUpdateRequest.getProjectId();
        ApiResourceType apiResourceType = resourceUpdateRequest.getApiResourceType();
        if (CollectionUtils.isEmpty(addFileIds) || CollectionUtils.isEmpty(files)) {
            return;
        }

        // 添加文件与接口的关联关系
        int size = addFileIds.size() > files.size() ? addFileIds.size() : files.size();
        List<ApiFileResource> apiFileResources = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            String fileId = addFileIds.get(i);
            MultipartFile file = files.get(i);
            ApiFileResource apiFileResource = new ApiFileResource();
            apiFileResource.setFileId(fileId);
            apiFileResource.setResourceId(resourceId);
            apiFileResource.setResourceType(apiResourceType.name());
            apiFileResource.setProjectId(projectId);
            apiFileResource.setCreateTime(System.currentTimeMillis());
            apiFileResource.setFileName(file.getOriginalFilename());
            apiFileResources.add(apiFileResource);
        }
        apiFileResourceMapper.batchInsert(apiFileResources);

        // 上传文件到对象存储
        uploadFileResource(resourceUpdateRequest.getFolder(), addFileIds, files);
    }

    /**
     * 更新接口时，更新接口与文件的关联关系
     */
    public void updateFileResource(ApiFileResourceUpdateRequest resourceUpdateRequest, List<MultipartFile> files) {
        // fileIds == null ，则不修改，如果是空数组则删除所有关联的文件
        List<String> fileIds = resourceUpdateRequest.getFileIds();
        String resourceId = resourceUpdateRequest.getResourceId();
        if (fileIds == null) {
            return;
        }
        // 查询原本关联的文件
        ApiFileResourceExample example = new ApiFileResourceExample();
        example.createCriteria().andResourceIdEqualTo(resourceId);
        List<ApiFileResource> apiFileResources = apiFileResourceMapper.selectByExample(example);
        List<String> originFileIds = apiFileResources.stream()
                .map(ApiFileResource::getFileId)
                .collect(Collectors.toList());

        // 删除没用的文件
        List<String> deleteFileIds = ListUtils.subtract(originFileIds, fileIds);
        deleteFileResource(deleteFileIds, resourceUpdateRequest);

        // 上传新的文件
        addFileResource(resourceUpdateRequest, files);
    }

    private void deleteFileResource(List<String> deleteFileIds, ApiFileResourceUpdateRequest resourceUpdateRequest) {
        if (CollectionUtils.isEmpty(deleteFileIds)) {
            return;
        }

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

    public void deleteByResourceId(String apiDebugDir, String resourceId) {
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
    }

    public List<ApiFileResource> getByResourceId(String resourceId) {
        ApiFileResourceExample example = new ApiFileResourceExample();
        example.createCriteria()
                .andResourceIdEqualTo(resourceId);
        return apiFileResourceMapper.selectByExample(example);
    }
    public List<String> getFileIdsByResourceId(String resourceId) {
        return getByResourceId(resourceId).stream()
                .map(ApiFileResource::getFileId)
                .collect(Collectors.toList());
    }

}
