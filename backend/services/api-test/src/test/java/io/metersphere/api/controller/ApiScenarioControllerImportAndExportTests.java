package io.metersphere.api.controller;

import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.domain.ApiScenarioExample;
import io.metersphere.api.dto.definition.ApiScenarioBatchExportRequest;
import io.metersphere.api.dto.export.MetersphereApiScenarioExportResponse;
import io.metersphere.api.dto.scenario.ApiScenarioDetail;
import io.metersphere.api.dto.scenario.ApiScenarioImportRequest;
import io.metersphere.api.mapper.ApiScenarioMapper;
import io.metersphere.api.service.ApiScenarioDataTransferService;
import io.metersphere.api.service.scenario.ApiScenarioService;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.functional.domain.ExportTask;
import io.metersphere.project.domain.Project;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.MsFileUtils;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.constants.ExportConstants;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.dto.AddProjectRequest;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.manager.ExportTaskManager;
import io.metersphere.system.service.CommonProjectService;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApiScenarioControllerImportAndExportTests extends BaseTest {

    private static final String URL_POST_IMPORT = "/api/scenario/import";

    private static final String URL_POST_EXPORT = "/api/scenario/export/";

    private static Project project;

    @Resource
    private CommonProjectService commonProjectService;
    @Resource
    private ApiScenarioDataTransferService apiScenarioDataTransferService;
    @Resource
    private ApiScenarioService apiScenarioService;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @BeforeEach
    public void initTestData() {
        //文件管理专用项目
        if (project == null) {
            AddProjectRequest initProject = new AddProjectRequest();
            initProject.setOrganizationId("100001");
            initProject.setName("场景导入专用");
            initProject.setDescription("场景导入专用项目");
            initProject.setEnable(true);
            initProject.setUserIds(List.of("admin"));
            project = commonProjectService.add(initProject, "admin", "/organization-project/add", OperationLogModule.SETTING_ORGANIZATION_PROJECT);
        }
    }

    @Test
    @Order(0)
    public void baseTest() throws Exception {
        try {
            apiScenarioDataTransferService.exportScenario(null, null, null);
        } catch (Exception ignore) {
        }
    }

    @Test
    @Order(1)
    public void testImport() throws Exception {
        ApiScenarioImportRequest request = new ApiScenarioImportRequest();
        request.setProjectId(project.getId());
        request.setType("jmeter");
        FileInputStream inputStream = new FileInputStream(new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/import-scenario/jmeter/simple.jmx")).getPath()));
        MockMultipartFile file = new MockMultipartFile("file", "simple.jmx", MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        paramMap.add("file", file);
        this.requestMultipartWithOkAndReturn(URL_POST_IMPORT, paramMap);


        request.setType("metersphere");
        inputStream = new FileInputStream(Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/import-scenario/metersphere/simple.ms")).getPath());
        file = new MockMultipartFile("file", "simple.ms", MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        paramMap.add("file", file);
        this.requestMultipartWithOkAndReturn(URL_POST_IMPORT, paramMap);

        request.setCoverData(true);
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        paramMap.add("file", file);
        this.requestMultipartWithOkAndReturn(URL_POST_IMPORT, paramMap);


        inputStream = new FileInputStream(Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/file_update_upload.JPG")).getPath());
        file = new MockMultipartFile("file", "simple.JPG", MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        paramMap.add("file", file);
        this.requestMultipart(URL_POST_IMPORT, paramMap);

        request.setType("har");
        inputStream = new FileInputStream(Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/import-scenario/har/simple.har")).getPath());
        file = new MockMultipartFile("simple.har", "simple.har", MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        paramMap.add("file", file);
        this.requestMultipartWithOkAndReturn(URL_POST_IMPORT, paramMap);

        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andProjectIdEqualTo(project.getId()).andNameEqualTo("simple").andDeletedEqualTo(false);
        ApiScenario scenario = apiScenarioMapper.selectByExample(example).getFirst();
        ApiScenarioDetail detail = apiScenarioService.get(scenario.getId());
        Assertions.assertEquals(detail.getSteps().size(), 4);
    }

    @Resource
    private ExportTaskManager exportTaskManager;

    @Test
    @Order(2)
    public void testExport() throws Exception {
        MsFileUtils.deleteDir("/tmp/api-scenario-export/");

        List<Boolean> exportAllRelatedData = new ArrayList<>() {{
            this.add(true);
            this.add(false);
        }};
        for (Boolean isAllRelatedData : exportAllRelatedData) {
            ApiScenarioBatchExportRequest exportRequest = new ApiScenarioBatchExportRequest();
            String fileId = IDGenerator.nextStr();
            exportRequest.setProjectId(project.getId());
            exportRequest.setFileId(fileId);
            exportRequest.setSelectAll(true);
            exportRequest.setExportAllRelatedData(isAllRelatedData);
            MvcResult mvcResult = this.requestPostWithOkAndReturn(URL_POST_EXPORT + "metersphere", exportRequest);
            String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);

            JSON.parseObject(returnData, ResultHolder.class).getData().toString();
            Assertions.assertTrue(StringUtils.isNotBlank(fileId));
            List<ExportTask> taskList = exportTaskManager.getExportTasks(exportRequest.getProjectId(), null, null, "admin", fileId);
            while (CollectionUtils.isEmpty(taskList)) {
                Thread.sleep(1000);
                taskList = exportTaskManager.getExportTasks(exportRequest.getProjectId(), null, null, "admin", fileId);
            }

            ExportTask task = taskList.getFirst();
            while (!StringUtils.equalsIgnoreCase(task.getState(), ExportConstants.ExportState.SUCCESS.name())) {
                Thread.sleep(1000);
                task = exportTaskManager.getExportTasks(exportRequest.getProjectId(), null, null, "admin", fileId).getFirst();
            }

            mvcResult = this.download(exportRequest.getProjectId(), fileId);

            byte[] fileBytes = mvcResult.getResponse().getContentAsByteArray();

            File zipFile = new File("/tmp/api-scenario-export/downloadFiles.zip");
            FileUtils.writeByteArrayToFile(zipFile, fileBytes);

            File[] files = MsFileUtils.unZipFile(zipFile, "/tmp/api-scenario-export/unzip/");
            assert files != null;
            Assertions.assertEquals(files.length, 1);
            String fileContent = FileUtils.readFileToString(files[0], StandardCharsets.UTF_8);

            MetersphereApiScenarioExportResponse exportResponse = ApiDataUtils.parseObject(fileContent, MetersphereApiScenarioExportResponse.class);

            Assertions.assertEquals(exportResponse.getExportScenarioList().size(), 7);

            MsFileUtils.deleteDir("/tmp/api-scenario-export/");
        }

        // jmx export
        ApiScenarioBatchExportRequest exportRequest = new ApiScenarioBatchExportRequest();
        String fileId = IDGenerator.nextStr();
        exportRequest.setProjectId(project.getId());
        exportRequest.setFileId(fileId);
        exportRequest.setSelectAll(true);
        exportRequest.setExportAllRelatedData(false);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(URL_POST_EXPORT + "jmeter", exportRequest);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        JSON.parseObject(returnData, ResultHolder.class).getData().toString();
        Assertions.assertTrue(StringUtils.isNotBlank(fileId));
        List<ExportTask> taskList = exportTaskManager.getExportTasks(exportRequest.getProjectId(), null, null, "admin", fileId);
        while (CollectionUtils.isEmpty(taskList)) {
            Thread.sleep(1000);
            taskList = exportTaskManager.getExportTasks(exportRequest.getProjectId(), null, null, "admin", fileId);
        }
        ExportTask task = taskList.getFirst();
        while (!StringUtils.equalsIgnoreCase(task.getState(), ExportConstants.ExportState.SUCCESS.name())) {
            Thread.sleep(1000);
            task = exportTaskManager.getExportTasks(exportRequest.getProjectId(), null, null, "admin", fileId).getFirst();
        }

        mvcResult = this.download(exportRequest.getProjectId(), fileId);

        byte[] fileBytes = mvcResult.getResponse().getContentAsByteArray();

        File zipFile = new File("/tmp/api-scenario-export/downloadFiles.zip");
        FileUtils.writeByteArrayToFile(zipFile, fileBytes);

        File[] files = MsFileUtils.unZipFile(zipFile, "/tmp/api-scenario-export/unzip/");
        assert files != null;
        Assertions.assertEquals(files.length, 7);
        MsFileUtils.deleteDir("/tmp/api-scenario-export/");
    }

    private MvcResult download(String projectId, String fileId) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get("/api/scenario/download/file/" + projectId + "/" + fileId)
                .header(SessionConstants.HEADER_TOKEN, sessionId)
                .header(SessionConstants.CSRF_TOKEN, csrfToken)).andReturn();
    }
}