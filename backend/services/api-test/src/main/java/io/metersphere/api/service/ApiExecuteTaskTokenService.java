package io.metersphere.api.service;

import io.metersphere.sdk.constants.ApiFileResourceType;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.constants.StorageType;
import io.metersphere.sdk.dto.api.task.ApiExecuteFileInfo;
import io.metersphere.sdk.dto.api.task.ApiExecuteFileResource;
import io.metersphere.sdk.dto.api.task.ApiExecuteTaskResource;
import io.metersphere.sdk.dto.api.task.TaskInfo;
import io.metersphere.sdk.dto.api.task.TaskItem;
import io.metersphere.sdk.dto.api.task.TaskProjectResource;
import io.metersphere.sdk.dto.api.task.TaskResourceFile;
import io.metersphere.sdk.util.JSON;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 任务访问令牌
 * 服务端为每个下发的任务项生成随机令牌，并按令牌缓存任务项与令牌的绑定关系。
 * 匿名接口（如获取执行脚本）通过令牌校验请求是否来自服务端下发的任务。
 * 前端调试（FRONTEND_DEBUG）时还会生成文件清单，执行器下载文件时只能下载清单中的文件，不能自行指定存储路径。
 */
@Service
public class ApiExecuteTaskTokenService {
    /**
     * 任务访问令牌的请求头
     */
    public static final String TASK_TOKEN_HEADER = "X-MS-TASK-TOKEN";
    private static final String SCRIPT_KEY_PREFIX = "api:execute:script:";
    private static final String TASK_TOKEN_KEY_PREFIX = "api:execute:task:";
    private static final long EXPIRED_DAYS = 1;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 脚本缓存 key
     * 使用随机令牌，避免通过任务项ID读取脚本
     */
    public String getScriptKey(String taskToken) {
        return SCRIPT_KEY_PREFIX + taskToken;
    }

    /**
     * 生成任务访问令牌，并缓存任务项与令牌的绑定关系
     * 所有下发的任务项都需要调用，匿名接口通过该绑定关系校验令牌
     */
    public String initTaskToken(TaskItem taskItem) {
        String taskToken = generateTaskToken(taskItem);
        // 使用 setIfAbsent，避免覆盖前端调试时已生成的文件清单
        stringRedisTemplate.opsForValue().setIfAbsent(getTaskTokenKey(taskToken),
                JSON.toJSONString(buildTaskResource(taskItem, null)), EXPIRED_DAYS, TimeUnit.DAYS);
        return taskToken;
    }

    /**
     * 生成任务项的文件清单
     * 只有前端调试的任务项，执行器才会通过接口下载文件
     *
     * @param taskInfo 任务信息，包含项目级别的资源
     * @param taskItem 任务项
     */
    public void initFileManifest(TaskInfo taskInfo, TaskItem taskItem) {
        String taskToken = generateTaskToken(taskItem);
        Map<String, ApiExecuteFileResource> files = new HashMap<>();
        addTaskFiles(files, taskItem.getTaskResourceFile());
        addTaskProjectFiles(files, taskItem.getRefProjectResource());
        if (taskInfo != null) {
            addTaskProjectFiles(files, taskInfo.getProjectResource());
        }
        saveTaskResource(taskToken, buildTaskResource(taskItem, files));
    }

    /**
     * 获取令牌绑定的任务项信息
     */
    public ApiExecuteTaskResource getTaskResource(String taskToken) {
        if (StringUtils.isBlank(taskToken)) {
            return null;
        }
        String json = stringRedisTemplate.opsForValue().get(getTaskTokenKey(taskToken));
        if (StringUtils.isBlank(json)) {
            return null;
        }
        return JSON.parseObject(json, ApiExecuteTaskResource.class);
    }

    /**
     * 获取文件清单中允许下载的文件
     */
    public ApiExecuteFileResource getFileResource(String taskToken, String fileId) {
        ApiExecuteTaskResource taskResource = getTaskResource(taskToken);
        if (taskResource == null || taskResource.getFiles() == null || StringUtils.isBlank(fileId)) {
            return null;
        }
        return taskResource.getFiles().get(fileId);
    }

    private String generateTaskToken(TaskItem taskItem) {
        if (StringUtils.isBlank(taskItem.getTaskToken())) {
            taskItem.setTaskToken(UUID.randomUUID().toString().replace("-", StringUtils.EMPTY));
        }
        return taskItem.getTaskToken();
    }

    private ApiExecuteTaskResource buildTaskResource(TaskItem taskItem, Map<String, ApiExecuteFileResource> files) {
        ApiExecuteTaskResource taskResource = new ApiExecuteTaskResource();
        taskResource.setTaskItemId(taskItem.getId());
        taskResource.setResourceId(taskItem.getResourceId());
        taskResource.setReportId(taskItem.getReportId());
        if (files != null) {
            taskResource.setFiles(files);
        }
        return taskResource;
    }

    /**
     * 获取任务访问令牌的缓存 key
     * value 是 ApiExecuteTaskResource，包含任务项ID、资源ID、报告ID和文件清单
     * 文件清单只有前端调试的任务项才会生成，执行器下载文件时只能下载清单中的文件，不能自行指定存储路径
     * @param taskToken
     * @return
     */
    private String getTaskTokenKey(String taskToken) {
        return TASK_TOKEN_KEY_PREFIX + taskToken;
    }

    private void saveTaskResource(String taskToken, ApiExecuteTaskResource taskResource) {
        stringRedisTemplate.opsForValue().set(getTaskTokenKey(taskToken), JSON.toJSONString(taskResource), EXPIRED_DAYS, TimeUnit.DAYS);
    }

    private void addTaskFiles(Map<String, ApiExecuteFileResource> files, TaskResourceFile taskResourceFile) {
        if (taskResourceFile == null) {
            return;
        }

        // 调试时未保存的临时文件: system/temp/{fileId}/{fileName}
        List<ApiExecuteFileInfo> localTempFiles = taskResourceFile.getLocalTempFiles();
        if (CollectionUtils.isNotEmpty(localTempFiles)) {
            localTempFiles.forEach(file -> addFile(files, file.getFileId(),
                    DefaultRepositoryDir.getSystemTempDir() + "/" + file.getFileId(),
                    file.getFileName(), StorageType.MINIO.name()));
        }

        // 本地上传的正式文件: {资源目录}/{fileId}/{fileName}
        List<ApiExecuteFileInfo> localFiles = taskResourceFile.getLocalFiles();
        if (CollectionUtils.isNotEmpty(localFiles)) {
            localFiles.forEach(file -> {
                String folder = getLocalFileFolder(file);
                if (folder == null) {
                    return;
                }
                addFile(files, file.getFileId(), folder + "/" + file.getFileId(), file.getFileName(), StorageType.MINIO.name());
            });
        }

        // 文件管理中的文件: project/{projectId}/file-management/{fileId}
        List<ApiExecuteFileInfo> refFiles = taskResourceFile.getRefFiles();
        if (CollectionUtils.isNotEmpty(refFiles)) {
            refFiles.forEach(file -> addFileManagementFile(files, file));
        }
    }

    private void addTaskProjectFiles(Map<String, ApiExecuteFileResource> files, TaskProjectResource projectResource) {
        if (projectResource == null) {
            return;
        }
        // 函数包也是文件管理中的文件
        if (CollectionUtils.isNotEmpty(projectResource.getFuncJars())) {
            projectResource.getFuncJars().forEach(file -> addFileManagementFile(files, file));
        }
        // 插件文件: system/plugin/{fileName}
        if (CollectionUtils.isNotEmpty(projectResource.getPluginFiles())) {
            projectResource.getPluginFiles().forEach(file -> addFile(files, file.getFileId(),
                    DefaultRepositoryDir.getPluginDir(), file.getFileName(), StorageType.MINIO.name()));
        }
    }

    /**
     * 文件管理中的文件，存储的名称是文件ID
     */
    private void addFileManagementFile(Map<String, ApiExecuteFileResource> files, ApiExecuteFileInfo file) {
        if (StringUtils.isAnyBlank(file.getFileId(), file.getProjectId()) || StorageType.isGit(file.getStorage())) {
            return;
        }
        addFile(files, file.getFileId(), DefaultRepositoryDir.getFileManagementDir(file.getProjectId()),
                file.getFileId(), getStorage(file.getStorage()));
    }

    /**
     * 本地上传的文件，根据资源类型确定存储目录
     */
    private String getLocalFileFolder(ApiExecuteFileInfo file) {
        if (StringUtils.isAnyBlank(file.getProjectId(), file.getResourceId(), file.getResourceType())) {
            return null;
        }
        ApiFileResourceType resourceType;
        try {
            resourceType = ApiFileResourceType.valueOf(file.getResourceType());
        } catch (IllegalArgumentException e) {
            return null;
        }
        return switch (resourceType) {
            case API -> DefaultRepositoryDir.getApiDefinitionDir(file.getProjectId(), file.getResourceId());
            case API_CASE -> DefaultRepositoryDir.getApiCaseDir(file.getProjectId(), file.getResourceId());
            case API_SCENARIO -> DefaultRepositoryDir.getApiScenarioDir(file.getProjectId(), file.getResourceId());
            case API_SCENARIO_STEP -> StringUtils.isBlank(file.getScenarioId()) ? null
                    : DefaultRepositoryDir.getApiScenarioStepDir(file.getProjectId(), file.getScenarioId(), file.getResourceId());
            case API_DEBUG -> DefaultRepositoryDir.getApiDebugDir(file.getProjectId(), file.getResourceId());
            case API_MOCK -> DefaultRepositoryDir.getApiMockDir(file.getProjectId(), file.getResourceId());
        };
    }

    private void addFile(Map<String, ApiExecuteFileResource> files, String fileId, String folder, String fileName, String storage) {
        if (StringUtils.isAnyBlank(fileId, folder, fileName, storage)) {
            return;
        }
        ApiExecuteFileResource fileResource = new ApiExecuteFileResource();
        fileResource.setFolder(folder);
        fileResource.setFileName(fileName);
        fileResource.setStorage(storage);
        files.put(fileId, fileResource);
    }

    private String getStorage(String storage) {
        return StringUtils.isBlank(storage) ? StorageType.MINIO.name() : storage.toUpperCase(Locale.ROOT);
    }
}
