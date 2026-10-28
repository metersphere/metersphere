package io.metersphere.api.controller;

import io.metersphere.api.service.ApiExecuteTaskTokenService;
import io.metersphere.api.service.ApiFileResourceService;
import io.metersphere.sdk.constants.ApiExecuteRunMode;
import io.metersphere.sdk.constants.StorageType;
import io.metersphere.sdk.dto.api.task.ApiExecuteFileInfo;
import io.metersphere.sdk.dto.api.task.GetRunScriptRequest;
import io.metersphere.sdk.dto.api.task.TaskInfo;
import io.metersphere.sdk.dto.api.task.TaskItem;
import io.metersphere.sdk.dto.api.task.TaskResourceFile;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @Author: jianxing
 * @CreateTime: 2023-12-13  15:59
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ApiExecuteResourceControllerTest extends BaseTest {

    private static final String BASE_PATH = "/api/execute/resource/";
    private static final String FILE = "file?fileId={0}";
    private static final String SCRIPT = "script";
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ApiFileResourceService apiFileResourceService;
    @Resource
    private ApiExecuteTaskTokenService apiExecuteTaskTokenService;

    @Override
    public String getBasePath() {
        return BASE_PATH;
    }

    @Test
    public void downloadFile() throws Exception {
        // 上传临时文件，并生成前端调试的文件清单
        String fileName = IDGenerator.nextStr() + "_file_upload.JPG";
        byte[] fileContent = "file_content".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", fileName, MediaType.APPLICATION_OCTET_STREAM_VALUE, fileContent);
        String fileId = apiFileResourceService.uploadTempFile(file);

        TaskItem taskItem = newTaskItem();
        TaskResourceFile taskResourceFile = new TaskResourceFile();
        ApiExecuteFileInfo fileInfo = new ApiExecuteFileInfo();
        fileInfo.setFileId(fileId);
        fileInfo.setFileName(fileName);
        fileInfo.setStorage(StorageType.MINIO.name());
        taskResourceFile.setLocalTempFiles(List.of(fileInfo));
        taskItem.setTaskResourceFile(taskResourceFile);
        apiExecuteTaskTokenService.initFileManifest(new TaskInfo(), taskItem);

        // 合法令牌可以下载文件清单中的文件
        mockMvc.perform(MockMvcRequestBuilders.post(getBasePath() + FILE, fileId)
                        .header(ApiExecuteTaskTokenService.TASK_TOKEN_HEADER, taskItem.getTaskToken()))
                .andExpect(status().isOk())
                .andExpect(content().bytes(fileContent));

        // 不传令牌，无法下载
        mockMvc.perform(MockMvcRequestBuilders.post(getBasePath() + FILE, fileId))
                .andExpect(status().isNotFound());

        // 令牌不存在，无法下载
        mockMvc.perform(MockMvcRequestBuilders.post(getBasePath() + FILE, fileId)
                        .header(ApiExecuteTaskTokenService.TASK_TOKEN_HEADER, UUID.randomUUID().toString()))
                .andExpect(status().isNotFound());

        // 清单中不存在的文件，无法下载
        mockMvc.perform(MockMvcRequestBuilders.post(getBasePath() + FILE, IDGenerator.nextStr())
                        .header(ApiExecuteTaskTokenService.TASK_TOKEN_HEADER, taskItem.getTaskToken()))
                .andExpect(status().isNotFound());

        // 非前端调试的任务只有令牌绑定，没有文件清单，无法下载
        TaskItem tokenOnlyTaskItem = newTaskItem();
        apiExecuteTaskTokenService.initTaskToken(tokenOnlyTaskItem);
        mockMvc.perform(MockMvcRequestBuilders.post(getBasePath() + FILE, fileId)
                        .header(ApiExecuteTaskTokenService.TASK_TOKEN_HEADER, tokenOnlyTaskItem.getTaskToken()))
                .andExpect(status().isNotFound());

        // 原漏洞的利用方式：使用任意存在的 redis key 作为 taskItemId，并指定任意存储路径，已无法下载
        String sessionLikeKey = "spring:session:sessions:" + UUID.randomUUID();
        stringRedisTemplate.opsForValue().set(sessionLikeKey, "session", 1, TimeUnit.MINUTES);
        mockMvc.perform(MockMvcRequestBuilders.post(getBasePath() + "file?taskItemId={0}", sessionLikeKey)
                        .header(ApiExecuteTaskTokenService.TASK_TOKEN_HEADER, sessionLikeKey))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getScript() throws Exception {
        // 普通任务只有令牌绑定，没有文件清单
        TaskItem taskItem = newTaskItem();
        apiExecuteTaskTokenService.initTaskToken(taskItem);
        String scriptKey = apiExecuteTaskTokenService.getScriptKey(taskItem.getTaskToken());
        stringRedisTemplate.opsForValue().set(scriptKey, "execute_script", 1, TimeUnit.DAYS);

        GetRunScriptRequest request = new GetRunScriptRequest();
        request.setTaskItem(taskItem);
        request.setRunMode(ApiExecuteRunMode.BACKEND_DEBUG.name());
        request.setNeedParseScript(false);
        mockMvc.perform(MockMvcRequestBuilders.post(getBasePath() + SCRIPT)
                        .content(JSON.toJSONString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.script").value("execute_script"));

        // 脚本使用随机令牌缓存，读取后删除
        Assertions.assertFalse(Boolean.TRUE.equals(stringRedisTemplate.hasKey(scriptKey)));

        // 伪造令牌，无法获取脚本
        TaskItem invalidTaskItem = newTaskItem();
        invalidTaskItem.setTaskToken(UUID.randomUUID().toString());
        request.setTaskItem(invalidTaskItem);
        mockMvc.perform(MockMvcRequestBuilders.post(getBasePath() + SCRIPT)
                        .content(JSON.toJSONString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());

        // 令牌与任务项不匹配，无法获取脚本
        TaskItem mismatchTaskItem = newTaskItem();
        mismatchTaskItem.setId(IDGenerator.nextStr());
        mismatchTaskItem.setTaskToken(taskItem.getTaskToken());
        request.setTaskItem(mismatchTaskItem);
        mockMvc.perform(MockMvcRequestBuilders.post(getBasePath() + SCRIPT)
                        .content(JSON.toJSONString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    private TaskItem newTaskItem() {
        TaskItem taskItem = new TaskItem();
        taskItem.setId(IDGenerator.nextStr());
        taskItem.setResourceId(IDGenerator.nextStr());
        return taskItem;
    }
}
