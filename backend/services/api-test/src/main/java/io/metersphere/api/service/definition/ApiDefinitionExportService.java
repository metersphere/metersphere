package io.metersphere.api.service.definition;

import io.metersphere.api.domain.ApiDefinitionModule;
import io.metersphere.api.domain.ApiDefinitionModuleExample;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.export.ApiExportResponse;
import io.metersphere.api.mapper.ApiDefinitionModuleMapper;
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
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.MsFileUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.constants.ExportConstants;
import io.metersphere.system.domain.User;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import io.metersphere.system.manager.ExportTaskManager;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.service.FileService;
import io.metersphere.system.service.NoticeSendService;
import io.metersphere.system.socket.ExportWebSocketHandler;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.utils.CustomFieldUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @author wx
 */
@Service
public class ApiDefinitionExportService {


    @Resource
    private ExtApiDefinitionMapper extApiDefinitionMapper;
    @Resource
    private ApiDefinitionModuleMapper apiDefinitionModuleMapper;
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
    private static final String EXPORT_CASE_TMP_DIR = "tmp";

    public ApiExportResponse genApiExportResponse(ApiDefinitionBatchExportRequest request, String type, String userId) {
        List<String> ids = this.getBatchExportApiIds(request, request.getProjectId(), userId);
        if (CollectionUtils.isEmpty(ids)) {
            return null;
        }
        List<ApiDefinitionWithBlob> list = this.selectAndSortByIds(ids);
        List<String> moduleIds = list.stream().map(ApiDefinitionWithBlob::getModuleId).toList();
        ApiDefinitionModuleExample example = new ApiDefinitionModuleExample();
        example.createCriteria().andIdIn(moduleIds);
        List<ApiDefinitionModule> definitionModules = apiDefinitionModuleMapper.selectByExample(example);
        Map<String, String> moduleMap = definitionModules.stream().collect(Collectors.toMap(ApiDefinitionModule::getId, ApiDefinitionModule::getName));
        return switch (type.toLowerCase()) {
            case "swagger" -> exportSwagger(request, list, moduleMap);
            case "metersphere" -> exportMetersphere(request, list, moduleMap);
            default -> new ApiExportResponse();
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
        } catch (InterruptedException e) {
            LogUtils.error("导出失败：" + e);
            throw new MSException(e);
        }
        return returnId;
    }

    @Resource
    private FileService fileService;

    public void uploadFileToMinio(String fileType, File file, String fileId) throws Exception {
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFileName(fileId.concat(".").concat(fileType));
        fileRequest.setFolder(DefaultRepositoryDir.getExportExcelTempDir());
        fileRequest.setStorage(StorageType.MINIO.name());
        try (FileInputStream inputStream = new FileInputStream(file)) {
            fileService.upload(inputStream, fileRequest);
        }
    }

    public String exportApiDefinitionZip(ApiDefinitionBatchExportRequest request, String exportType, String userId) throws Exception {
        File tmpDir = null;
        String fileType = "";
        try {
            User user = userMapper.selectByPrimaryKey(userId);
            noticeSendService.setLanguage(user.getLanguage());
            tmpDir = new File(LocalRepositoryDir.getSystemTempDir() + File.separatorChar + EXPORT_CASE_TMP_DIR + "_" + IDGenerator.nextStr());
            if (!tmpDir.exists() && !tmpDir.mkdirs()) {
                throw new MSException(Translator.get("upload_fail"));
            }
            //获取导出的ids集合
            List<String> ids = this.getBatchExportApiIds(request, request.getProjectId(), userId);
            if (CollectionUtils.isEmpty(ids)) {
                return null;
            }
            ApiExportResponse exportResponse = this.genApiExportResponse(request, exportType, userId);
            File createFile = new File(tmpDir.getPath() + File.separatorChar + request.getFileId() + ".json");
            if (!createFile.exists()) {
                try {
                    createFile.createNewFile();
                } catch (IOException e) {
                    throw new MSException(e);
                }
            }
            FileUtils.writeByteArrayToFile(createFile, JSON.toJSONString(exportResponse).getBytes());
            fileType = "json";
            uploadFileToMinio(fileType, createFile, request.getFileId());

            // 生成日志
            LogDTO logDTO = apiDefinitionLogService.exportExcelLog(request, exportType, userId, projectMapper.selectByPrimaryKey(request.getProjectId()).getOrganizationId());
            operationLogService.add(logDTO);

            List<ExportTask> exportTasks = exportTaskManager.getExportTasks(request.getProjectId(), ExportConstants.ExportType.API_DEFINITION.name(), ExportConstants.ExportState.PREPARED.toString(), userId, null);
            String taskId;
            if (CollectionUtils.isNotEmpty(exportTasks)) {
                taskId = exportTasks.getFirst().getId();
                exportTaskManager.updateExportTask(ExportConstants.ExportState.SUCCESS.name(), taskId, fileType);
            } else {
                taskId = MsgType.CONNECT.name();
            }

            ExportMsgDTO exportMsgDTO = new ExportMsgDTO(request.getFileId(), taskId, ids.size(), true, MsgType.EXEC_RESULT.name());
            ExportWebSocketHandler.sendMessageSingle(exportMsgDTO);
        } catch (Exception e) {
            List<ExportTask> exportTasks = exportTaskManager.getExportTasks(request.getProjectId(), ExportConstants.ExportType.API_DEFINITION.name(), ExportConstants.ExportState.PREPARED.toString(), userId, null);
            if (CollectionUtils.isNotEmpty(exportTasks)) {
                exportTaskManager.updateExportTask(ExportConstants.ExportState.ERROR.name(), exportTasks.getFirst().getId(), fileType);
            }
            ExportMsgDTO exportMsgDTO = new ExportMsgDTO(request.getFileId(), "", 0, false, MsgType.EXEC_RESULT.name());
            ExportWebSocketHandler.sendMessageSingle(exportMsgDTO);
            LogUtils.error(e);
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
            CustomFieldUtils.setBaseQueryRequestCustomMultipleFields(request.getCondition(), userId);
            List<String> ids = extApiDefinitionMapper.getIdsBySort(request, request.getProjectId(), protocols, request.getSortString());
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            return ids;
        } else {
            return request.getSelectIds();
        }
    }

    private ApiExportResponse exportSwagger(ApiDefinitionBatchRequest request, List<ApiDefinitionWithBlob> list, Map<String, String> moduleMap) {
        Project project = projectMapper.selectByPrimaryKey(request.getProjectId());
        Swagger3ExportParser swagger3Parser = new Swagger3ExportParser();
        try {
            return swagger3Parser.parse(list, project, moduleMap);
        } catch (Exception e) {
            throw new MSException(e);
        }
    }

    private ApiExportResponse exportMetersphere(ApiDefinitionBatchExportRequest request, List<ApiDefinitionWithBlob> list, Map<String, String> moduleMap) {
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

    //    public ResponseEntity<byte[]> downloadFile(String projectId, String fileId, String userId) {
    //        List<ExportTask> exportTasks = exportTaskManager.getExportTasks(projectId, ExportConstants.ExportType.API_DEFINITION.name(), ExportConstants.ExportState.SUCCESS.toString(), userId, fileId);
    //        if (CollectionUtils.isEmpty(exportTasks)) {
    //            return ResponseEntity.notFound().build();
    //        }
    //        ExportTask tasksFirst = exportTasks.getFirst();
    //        Project project = projectMapper.selectByPrimaryKey(projectId);
    //        byte[] bytes;
    //        FileRequest fileRequest = new FileRequest();
    //        fileRequest.setFileName(tasksFirst.getFileId().concat(".").concat("json"));
    //        fileRequest.setFolder(DefaultRepositoryDir.getExportExcelTempDir());
    //        fileRequest.setStorage(StorageType.MINIO.name());
    //        try {
    //            bytes = fileService.download(fileRequest);
    //        } catch (Exception e) {
    //            throw new MSException("get file error");
    //        }
    //        String fileName = "Metersphere_case_" + project.getName() + "." + tasksFirst.getFileType();
    //
    //        try {
    //            return ResponseEntity.ok()
    //                    .contentType(MediaType.parseMediaType("application/octet-stream"))
    //                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()))
    //                    .body(bytes);
    //        } catch (UnsupportedEncodingException e) {
    //            throw new MSException("Utf-8 encoding is not supported");
    //        }
    //    }

    public void downloadFile(String projectId, String fileId, String userId, HttpServletResponse httpServletResponse) {
        List<ExportTask> exportTasks = exportTaskManager.getExportTasks(projectId, ExportConstants.ExportType.API_DEFINITION.name(), ExportConstants.ExportState.SUCCESS.toString(), userId, fileId);
        if (CollectionUtils.isEmpty(exportTasks)) {
            return;
        }
        ExportTask tasksFirst = exportTasks.getFirst();
        Project project = projectMapper.selectByPrimaryKey(projectId);
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFileName(tasksFirst.getFileId().concat(".").concat("json"));
        fileRequest.setFolder(DefaultRepositoryDir.getExportExcelTempDir());
        fileRequest.setStorage(StorageType.MINIO.name());
        String fileName = "Metersphere_case_" + project.getName() + "." + tasksFirst.getFileType();
        try {
            InputStream fileInputStream = fileService.getFileAsStream(fileRequest);
            FileDownloadUtils.zipFilesWithResponse(fileName, fileInputStream, httpServletResponse);
        } catch (Exception e) {
            throw new MSException("get file error");
        }
    }
}
