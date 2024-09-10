package io.metersphere.api.controller;

import io.metersphere.api.dto.definition.ApiDefinitionBatchExportRequest;
import io.metersphere.api.dto.scenario.ApiScenarioImportRequest;
import io.metersphere.project.domain.Project;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.dto.AddProjectRequest;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.service.CommonProjectService;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
            //            ArrayList<String> moduleList = new ArrayList<>(List.of("workstation", "testPlan", "bugManagement", "caseManagement", "apiTest", "uiTest", "loadTest"));
            //            Project updateProject = new Project();
            //            updateProject.setId(importProject.getId());
            //            updateProject.setModuleSetting(JSON.toJSONString(moduleList));
            //            projectMapper.updateByPrimaryKeySelective(updateProject);
        }
    }

    @Test
    @Order(1)
    public void testImport() throws Exception {
        Map<String, String> importTypeAndSuffix = new LinkedHashMap<>();
        //        importTypeAndSuffix.put("metersphere", "json");
        importTypeAndSuffix.put("jmeter", "jmx");
        for (Map.Entry<String, String> entry : importTypeAndSuffix.entrySet()) {
            ApiScenarioImportRequest request = new ApiScenarioImportRequest();
            request.setProjectId(project.getId());
            request.setType(entry.getKey());
            String importType = entry.getKey();
            String fileSuffix = entry.getValue();
            FileInputStream inputStream = new FileInputStream(new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/import-scenario/" + importType + "/simple." + fileSuffix)).getPath()));
            MockMultipartFile file = new MockMultipartFile("file", "simple." + fileSuffix, MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
            MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
            paramMap.add("request", JSON.toJSONString(request));
            paramMap.add("file", file);
            this.requestMultipartWithOkAndReturn(URL_POST_IMPORT, paramMap);
        }
    }

    @Test
    @Order(1)
    public void testExport() throws Exception {
        ApiDefinitionBatchExportRequest exportRequest = new ApiDefinitionBatchExportRequest();
        String fileId = IDGenerator.nextStr();
        exportRequest.setProjectId(project.getId());
        exportRequest.setFileId(fileId);
        exportRequest.setSelectAll(true);
        exportRequest.setExportApiCase(true);
        exportRequest.setExportApiMock(true);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(URL_POST_EXPORT + "metersphere", exportRequest);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }
}