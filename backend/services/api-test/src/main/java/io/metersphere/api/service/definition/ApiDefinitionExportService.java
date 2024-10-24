package io.metersphere.api.service.definition;

import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.export.ApiDefinitionExportResponse;
import io.metersphere.api.mapper.ExtApiDefinitionMapper;
import io.metersphere.api.mapper.ExtApiDefinitionMockMapper;
import io.metersphere.api.mapper.ExtApiTestCaseMapper;
import io.metersphere.api.parser.api.MetersphereExportParser;
import io.metersphere.api.parser.api.Swagger3ExportParser;
import io.metersphere.functional.domain.ExportTask;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.utils.FileDownloadUtils;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.dto.ExportMsgDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.util.*;
import io.metersphere.system.constants.ExportConstants;
import io.metersphere.system.domain.User;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import io.metersphere.system.manager.ExportTaskManager;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.service.FileService;
import io.metersphere.system.service.NoticeSendService;
import io.metersphere.system.socket.ExportWebSocketHandler;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


/**
 * @author wx
 */
@Service
public class ApiDefinitionExportService {

    @Resource
    private ExtApiDefinitionMapper extApiDefinitionMapper;
    @Resource
    private ExtApiTestCaseMapper extApiTestCaseMapper;
    @Resource
    private ExtApiDefinitionMockMapper extApiDefinitionMockMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ExportTaskManager exportTaskManager;
    @Resource
    private NoticeSendService noticeSendService;
    @Resource
    private ApiDefinitionLogService apiDefinitionLogService;
    @Resource
    private OperationLogService operationLogService;
    @Resource
    private ApiDefinitionImportService apiDefinitionImportService;
    @Resource
    private FileService fileService;

    private static final String EXPORT_CASE_TMP_DIR = "apiDefinition";

    public Map<String, String> buildModuleIdPathMap(String projectId) {
        List<BaseTreeNode> apiModules = apiDefinitionImportService.buildTreeData(projectId, null);
        Map<String, String> modulePathMap = new HashMap<>();
        apiModules.forEach(item -> {
            modulePathMap.put(item.getId(), item.getPath());
        });
        return modulePathMap;
    }

    public ApiDefinitionExportResponse genApiExportResponse(ApiDefinitionBatchExportRequest request, Map<String, String> moduleMap, String type, String userId) {
        List<ApiDefinitionWithBlob> list = this.selectAndSortByIds(request.getSelectIds());
        return switch (type.toLowerCase()) {
            case "swagger" -> exportSwagger(request, list, moduleMap);
            case "metersphere" -> exportMetersphere(request, list, moduleMap);
            default -> new ApiDefinitionExportResponse();
        };
    }

    public String exportApiDefinition(ApiDefinitionBatchExportRequest request, String type, String userId) {
        String returnId;
        try {
            exportTaskManager.exportCheck(request.getProjectId(), ExportConstants.ExportType.API_DEFINITION.toString(), userId);
            ExportTask exportTask = exportTaskManager.exportAsyncTask(
                    request.getProjectId(),
                    request.getFileId(), userId,
                    ExportConstants.ExportType.API_DEFINITION.name(), request, t -> {
                        try {
                            return exportApiDefinitionZip(request, type, userId);
                        } catch (Exception e) {
                            throw new MSException(e);
                        }
                    });
            returnId = exportTask.getId();
        } catch (Exception e) {
            LogUtils.error("导出失败：" + e);
            throw new MSException(e);
        }
        return returnId;
    }

    public String exportApiDefinitionZip(ApiDefinitionBatchExportRequest request, String exportType, String userId) throws Exception {
        // 为避免客户端未及时开启ws，此处延迟1s
        Thread.sleep(1000);
        File tmpDir = new File(LocalRepositoryDir.getSystemTempDir() + File.separatorChar + EXPORT_CASE_TMP_DIR + "_" + IDGenerator.nextStr());
        if (!tmpDir.mkdirs()) {
            throw new MSException(Translator.get("upload_fail"));
        }
        String fileType = "zip";
        try {
            User user = userMapper.selectByPrimaryKey(userId);
            noticeSendService.setLanguage(user.getLanguage());
            //获取导出的ids集合
            List<String> ids = this.getBatchExportApiIds(request, request.getProjectId(), userId);
            if (CollectionUtils.isEmpty(ids)) {
                return null;
            }

            Map<String, String> moduleMap = this.buildModuleIdPathMap(request.getProjectId());

            String fileFolder = tmpDir.getPath() + File.separatorChar + request.getFileId();

            AtomicInteger fileIndex = new AtomicInteger(1);
            SubListUtils.dealForSubList(ids, 1000, subList -> {
                request.setSelectIds(subList);
                ApiDefinitionExportResponse exportResponse = this.genApiExportResponse(request, moduleMap, exportType, userId);
                TempFileUtils.writeExportFile(fileFolder + File.separatorChar + "API_" + fileIndex.getAndIncrement() + ".json", exportResponse);
            });
            File zipFile = MsFileUtils.zipFile(tmpDir.getPath(), request.getFileId());
            if (zipFile == null) {
                return null;
            }
            fileService.upload(zipFile, new FileRequest(DefaultRepositoryDir.getExportApiTempDir(), StorageType.MINIO.name(), request.getFileId()));

            // 生成日志
            LogDTO logDTO = apiDefinitionLogService.exportExcelLog(request, exportType, userId, projectMapper.selectByPrimaryKey(request.getProjectId()).getOrganizationId());
            operationLogService.add(logDTO);

            String taskId = exportTaskManager.getExportTaskId(request.getProjectId(), ExportConstants.ExportType.API_DEFINITION.name(), ExportConstants.ExportState.PREPARED.toString(), userId, null, fileType);
            ExportWebSocketHandler.sendMessageSingle(
                    new ExportMsgDTO(request.getFileId(), taskId, ids.size(), true, MsgType.EXEC_RESULT.name())
            );
        } catch (Exception e) {
            LogUtils.error(e);
            List<ExportTask> exportTasks = exportTaskManager.getExportTasks(request.getProjectId(), ExportConstants.ExportType.API_DEFINITION.name(), ExportConstants.ExportState.PREPARED.toString(), userId, null);
            if (CollectionUtils.isNotEmpty(exportTasks)) {
                exportTaskManager.updateExportTask(ExportConstants.ExportState.ERROR.name(), exportTasks.getFirst().getId(), fileType);
            }
            ExportMsgDTO exportMsgDTO = new ExportMsgDTO(request.getFileId(), "", 0, false, MsgType.EXEC_RESULT.name());
            ExportWebSocketHandler.sendMessageSingle(exportMsgDTO);
            throw new MSException(e);
        } finally {
            MsFileUtils.deleteDir(tmpDir.getPath());
        }
        return null;
    }

    private List<ApiDefinitionWithBlob> selectAndSortByIds(List<String> ids) {
        Map<String, ApiDefinitionWithBlob> apiMap = extApiDefinitionMapper.selectApiDefinitionWithBlob(ids).stream().collect(Collectors.toMap(ApiDefinitionWithBlob::getId, v -> v));
        return ids.stream().map(apiMap::get).toList();
    }

    private List<String> getBatchExportApiIds(ApiDefinitionBatchExportRequest request, String exportType, String userId) {
        List<String> protocols = request.getProtocols();
        if (StringUtils.equalsIgnoreCase(exportType, "swagger")) {
            protocols = List.of(ModuleConstants.NODE_PROTOCOL_HTTP);
        }

        if (request.isSelectAll()) {
            String sortString = StringUtils.isBlank(request.getSortString()) ? "pos desc,id desc" : request.getSortString();
            List<String> ids = extApiDefinitionMapper.getIdsBySort(request, request.getProjectId(), protocols, sortString, false);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            return ids;
        } else {
            return request.getSelectIds();
        }
    }

    private ApiDefinitionExportResponse exportSwagger(ApiDefinitionBatchRequest request, List<ApiDefinitionWithBlob> list, Map<String, String> moduleMap) {
        Project project = projectMapper.selectByPrimaryKey(request.getProjectId());
        Swagger3ExportParser swagger3Parser = new Swagger3ExportParser();
        try {
            return swagger3Parser.parse(list, project, moduleMap);
        } catch (Exception e) {
            throw new MSException(e);
        }
    }

    private ApiDefinitionExportResponse exportMetersphere(ApiDefinitionBatchExportRequest request, List<ApiDefinitionWithBlob> list, Map<String, String> moduleMap) {
        try {
            List<String> apiIds = list.stream().map(ApiDefinitionWithBlob::getId).toList();
            List<ApiTestCaseWithBlob> apiTestCaseWithBlobs = new ArrayList<>();
            List<ApiMockWithBlob> apiMockWithBlobs = new ArrayList<>();
            if (request.isExportApiCase()) {
                apiTestCaseWithBlobs = extApiTestCaseMapper.selectAllDetailByApiIds(apiIds);
            }
            if (request.isExportApiMock()) {
                apiMockWithBlobs = extApiDefinitionMockMapper.selectAllDetailByApiIds(apiIds);
            }
            return new MetersphereExportParser().parse(list, apiTestCaseWithBlobs, apiMockWithBlobs, moduleMap);
        } catch (Exception e) {
            throw new MSException(e);
        }
    }

    public void stopExport(String taskId, String userId) {
        exportTaskManager.sendStopMessage(taskId, userId);
    }

    public void downloadFile(String projectId, String fileId, String userId, HttpServletResponse httpServletResponse) {
        List<ExportTask> exportTasks = exportTaskManager.getExportTasks(projectId, ExportConstants.ExportType.API_DEFINITION.name(), ExportConstants.ExportState.SUCCESS.toString(), userId, fileId);
        if (CollectionUtils.isEmpty(exportTasks)) {
            return;
        }
        ExportTask tasksFirst = exportTasks.getFirst();
        Project project = projectMapper.selectByPrimaryKey(projectId);
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFileName(tasksFirst.getFileId());
        fileRequest.setFolder(DefaultRepositoryDir.getExportApiTempDir());
        fileRequest.setStorage(StorageType.MINIO.name());
        String fileName = "Metersphere_api_" + project.getName() + "." + tasksFirst.getFileType();
        try {
            InputStream fileInputStream = fileService.getFileAsStream(fileRequest);
            FileDownloadUtils.zipFilesWithResponse(fileName, fileInputStream, httpServletResponse);
        } catch (Exception e) {
            throw new MSException("get file error");
        }
    }
}
