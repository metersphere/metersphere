package io.metersphere.api.controller;

import io.metersphere.api.constants.*;
import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.domain.ApiDefinitionExample;
import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.scenario.ApiScenarioAddRequest;
import io.metersphere.api.dto.scenario.ApiScenarioStepRequest;
import io.metersphere.api.dto.scenario.ApiScenarioUpdateRequest;
import io.metersphere.api.mapper.ApiDefinitionMapper;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.api.utils.ApiDefinitionUtils;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ExtBaseProjectVersionMapper;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.util.CalculateUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.dto.AddProjectRequest;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.service.CommonProjectService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApiCalculateTest extends BaseTest {
    private static Project project;

    @Resource
    private CommonProjectService commonProjectService;
    @Resource
    private ExtBaseProjectVersionMapper extBaseProjectVersionMapper;
    @Resource
    private ProjectMapper projectMapper;

    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;

    @BeforeEach
    public void initTestData() throws Exception {
        //测试计划专用项目
        if (project == null) {
            AddProjectRequest initProject = new AddProjectRequest();
            initProject.setOrganizationId("100001");
            initProject.setName("文件管理专用项目");
            initProject.setDescription("建国创建的文件管理专用项目");
            initProject.setEnable(true);
            initProject.setUserIds(List.of("admin"));
            project = commonProjectService.add(initProject, "admin", "/organization-project/add", OperationLogModule.SETTING_ORGANIZATION_PROJECT);

            ArrayList<String> moduleList = new ArrayList<>(List.of(new String[]{"workstation", "testPlan", "bugManagement", "caseManagement", "apiTest", "uiTest", "loadTest"}));
            Project updateProject = new Project();
            updateProject.setId(project.getId());
            updateProject.setModuleSetting(JSON.toJSONString(moduleList));
            projectMapper.updateByPrimaryKeySelective(updateProject);


            ApiScenarioAddRequest apiScenarioAddRequest = new ApiScenarioAddRequest();
            apiScenarioAddRequest.setProjectId(project.getId());
            apiScenarioAddRequest.setDescription("desc");
            apiScenarioAddRequest.setName("test name");
            apiScenarioAddRequest.setModuleId("default");
            apiScenarioAddRequest.setGrouped(false);
            apiScenarioAddRequest.setTags(List.of("tag1", "tag2"));
            apiScenarioAddRequest.setPriority("P0");
            apiScenarioAddRequest.setStatus(ApiScenarioStatus.COMPLETED.name());
            apiScenarioAddRequest.setSteps(new ArrayList<>());
            MvcResult scenarioMvcResult = this.requestPostWithOkAndReturn("/api/scenario/add", apiScenarioAddRequest);
            ApiScenario scenario = getResultData(scenarioMvcResult, ApiScenario.class);


            // 创建接口定义
            Map<String, List<String>> methodAndPath = Map.of(
                    "GET", List.of(
                            "/api/get-test/1",
                            "/api/get-test/2",
                            "/api/{/get-test}/3/withCase",
                            "/api/get-test/4/withCase",// 场景关联它的用例
                            "/api/get-test/5/never-compare",
                            "/{api}/{/get-test}/{6}",//这个一定会被匹配到
                            "/api/get-test/{7}",// 这个一定会被匹配到
                            "/api/get-test/8",// 场景关联它的自定义请求
                            "/api/get-test/9",// 场景关联这个接口
                            "/api/get-test/10"),
                    "POST", List.of(
                            "/post/api/test/1",
                            "/post/api/test/2",
                            "/post/api/{test}/3/withCase",
                            "/post/api/test/4/withCase",// 场景关联它的用例
                            "/post/api/test/5/never-compare",
                            "/{post}/{api}/{/get-test}/{6}", //这个一定会被匹配到
                            "/post/api/test/{7}", // 这个一定会被匹配到
                            "/post/api/test/8",     // 场景关联它的自定义请求
                            "/post/api/test/9",// 场景关联这个接口
                            "/post/api/test/10"
                    )
            );

            List<ApiScenarioStepRequest> steps = new ArrayList<>();
            Map<String, Object> steptDetailMap = new HashMap<>();
            // 创建接口用例
            for (Map.Entry<String, List<String>> entry : methodAndPath.entrySet()) {
                String method = entry.getKey();
                for (String path : entry.getValue()) {
                    // 创建接口定义
                    String defaultVersion = extBaseProjectVersionMapper.getDefaultVersion(project.getId());
                    ApiDefinitionAddRequest apiDefinitionAddRequest = new ApiDefinitionAddRequest();
                    apiDefinitionAddRequest.setName(method + "_" + path);
                    apiDefinitionAddRequest.setProtocol(ApiConstants.HTTP_PROTOCOL);
                    apiDefinitionAddRequest.setProjectId(project.getId());
                    apiDefinitionAddRequest.setMethod(method);
                    apiDefinitionAddRequest.setPath(path);
                    apiDefinitionAddRequest.setStatus(ApiDefinitionStatus.PROCESSING.name());
                    apiDefinitionAddRequest.setModuleId("default");
                    apiDefinitionAddRequest.setVersionId(defaultVersion);
                    apiDefinitionAddRequest.setDescription("描述内容");
                    apiDefinitionAddRequest.setTags(new LinkedHashSet<>(List.of("tag1", "tag2")));

                    MsHTTPElement msHttpElement = MsHTTPElementTest.getMsHttpElement();
                    msHttpElement.setBody(MsHTTPElementTest.getGeneralBody());
                    msHttpElement.setMethod(method);
                    msHttpElement.setPath(path);

                    apiDefinitionAddRequest.setRequest(JSON.parseObject(ApiDataUtils.toJSONString(msHttpElement)));
                    List<HttpResponse> msHttpResponse = MsHTTPElementTest.getMsHttpResponse();
                    apiDefinitionAddRequest.setResponse(msHttpResponse);

                    MvcResult mvcResult = this.requestPostWithOkAndReturn("/api/definition/add", apiDefinitionAddRequest);
                    ApiDefinition resultData = getResultData(mvcResult, ApiDefinition.class);

                    if (path.endsWith("/withCase")) {
                        ApiTestCaseAddRequest request = new ApiTestCaseAddRequest();
                        request.setApiDefinitionId(resultData.getId());
                        request.setName(resultData.getName() + "_case");
                        request.setProjectId(project.getId());
                        request.setPriority("P0");
                        request.setStatus(ApiDefinitionStatus.PROCESSING.name());
                        request.setTags(new LinkedHashSet<>(List.of("tag1", "tag2")));
                        request.setRequest(JSON.parseObject(ApiDataUtils.toJSONString(msHttpElement)));
                        MvcResult testCaseResult = this.requestPostWithOkAndReturn("/api/case/add", request);
                        ApiTestCase apiTestCase = getResultData(testCaseResult, ApiTestCase.class);

                        if (path.endsWith("/4/withCase")) {
                            ApiScenarioStepRequest stepRequest = new ApiScenarioStepRequest();
                            stepRequest.setId(IDGenerator.nextStr());
                            stepRequest.setVersionId(extBaseProjectVersionMapper.getDefaultVersion(project.getId()));
                            stepRequest.setConfig(new HashMap<>());
                            stepRequest.setEnable(true);
                            stepRequest.setStepType(ApiScenarioStepType.API_CASE.name());
                            stepRequest.setResourceId(apiTestCase.getId());
                            stepRequest.setName(apiTestCase.getName() + "_step");
                            stepRequest.setRefType(ApiScenarioStepRefType.REF.name());
                            stepRequest.setProjectId(project.getId());
                            stepRequest.setConfig(new HashMap<>() {{
                                this.put("protocol", "http");
                            }});
                            steps.add(stepRequest);
                            steptDetailMap.put(stepRequest.getId(), JSON.parseObject(ApiDataUtils.toJSONString(msHttpElement)));
                        }
                    } else if (path.endsWith("/8")) {

                        ApiScenarioStepRequest stepRequest = new ApiScenarioStepRequest();
                        stepRequest.setId(IDGenerator.nextStr());
                        stepRequest.setVersionId(extBaseProjectVersionMapper.getDefaultVersion(project.getId()));
                        stepRequest.setConfig(new HashMap<>());
                        stepRequest.setEnable(true);
                        stepRequest.setStepType(ApiScenarioStepType.CUSTOM_REQUEST.name());
                        stepRequest.setName("custom_step");
                        stepRequest.setRefType(ApiScenarioStepRefType.DIRECT.name());
                        stepRequest.setProjectId(project.getId());
                        stepRequest.setConfig(new HashMap<>() {{
                            this.put("protocol", "http");
                        }});
                        steps.add(stepRequest);

                        MsHTTPElement customElement = MsHTTPElementTest.getMsHttpElement();
                        customElement.setBody(MsHTTPElementTest.getGeneralBody());
                        customElement.setMethod(method);
                        customElement.setPath(path);
                        steptDetailMap.put(stepRequest.getId(), JSON.parseObject(ApiDataUtils.toJSONString(customElement)));
                    } else if (path.endsWith("/9")) {
                        ApiScenarioStepRequest stepRequest = new ApiScenarioStepRequest();
                        stepRequest.setId(IDGenerator.nextStr());
                        stepRequest.setVersionId(extBaseProjectVersionMapper.getDefaultVersion(project.getId()));
                        stepRequest.setConfig(new HashMap<>());
                        stepRequest.setEnable(true);
                        stepRequest.setStepType(ApiScenarioStepType.API.name());
                        stepRequest.setResourceId(resultData.getId());
                        stepRequest.setName(resultData.getName() + "_step");
                        stepRequest.setRefType(ApiScenarioStepRefType.REF.name());
                        stepRequest.setProjectId(project.getId());
                        stepRequest.setConfig(new HashMap<>() {{
                            this.put("protocol", "http");
                        }});
                        steps.add(stepRequest);
                        steptDetailMap.put(stepRequest.getId(), JSON.parseObject(ApiDataUtils.toJSONString(msHttpElement)));
                    }
                }
            }

            ApiScenarioUpdateRequest apiScenarioUpdateRequest = new ApiScenarioUpdateRequest();
            apiScenarioUpdateRequest.setId(scenario.getId());
            apiScenarioUpdateRequest.setProjectId(project.getId());
            apiScenarioUpdateRequest.setSteps(steps);
            apiScenarioUpdateRequest.setStepDetails(steptDetailMap);
            this.requestPostWithOkAndReturn("/api/scenario/update", apiScenarioUpdateRequest);

        }
    }

    /*
        本次接口定义相关数据：
        "GET"："/api/get-test/1",
                "/api/get-test/2",
                "/api/{/get-test}/3/withCase",
                "/api/get-test/4/withCase",// 场景关联它的用例
                "/api/get-test/5/never-compare",
                "/{api}/{/get-test}/{6}",//这个在接口程度覆盖率和场景程度覆盖率计算中一定会被匹配到
                "/api/get-test/{7}",// 这个在接口程度覆盖率和场景程度覆盖率计算中一定会被匹配到
                "/api/get-test/8",// 场景关联它的自定义请求
                "/api/get-test/9",// 场景关联这个接口
                "/api/get-test/10"

        "POST"："/post/api/test/1",
                "/post/api/test/2",
                "/post/api/{test}/3/withCase",
                "/post/api/test/4/withCase",// 场景关联它的用例
                "/post/api/test/5/never-compare",
                "/{post}/{api}/{/get-test}/{6}", //这个在接口程度覆盖率和场景程度覆盖率计算中一定会被匹配到
                "/post/api/test/{7}",   // 这个在接口程度覆盖率和场景程度覆盖率计算中一定会被匹配到
                "/post/api/test/8",     // 场景关联它的自定义请求
                "/post/api/test/9",     // 场景关联这个接口
                "/post/api/test/10"

     */
    @Test
    public void calculateTest() throws Exception {
        ApiDefinitionExample apiDefinitionExample = new ApiDefinitionExample();
        apiDefinitionExample.createCriteria().andProjectIdEqualTo(project.getId());
        Assertions.assertEquals(apiDefinitionMapper.countByExample(apiDefinitionExample), 20);

        MvcResult mvcResult = this.requestGetWithOkAndReturn("/api/definition/rage/" + project.getId());
        ApiCoverageDTO apiCoverageDTO = getResultData(mvcResult, ApiCoverageDTO.class);
        Assertions.assertEquals(20, apiCoverageDTO.getAllApiCount());
        Assertions.assertEquals(4, apiCoverageDTO.getCoverWithApiCase());
        Assertions.assertEquals(16, apiCoverageDTO.getUnCoverWithApiCase());
        Assertions.assertEquals(apiCoverageDTO.getApiCaseCoverage(), CalculateUtils.reportPercentage(apiCoverageDTO.getCoverWithApiCase(), apiCoverageDTO.getAllApiCount()));

        Assertions.assertEquals(8, apiCoverageDTO.getCoverWithApiScenario());
        Assertions.assertEquals(12, apiCoverageDTO.getUnCoverWithApiScenario());
        Assertions.assertEquals(apiCoverageDTO.getScenarioCoverage(), CalculateUtils.reportPercentage(apiCoverageDTO.getCoverWithApiScenario(), apiCoverageDTO.getAllApiCount()));

        Assertions.assertEquals(10, apiCoverageDTO.getCoverWithApiDefinition());
        Assertions.assertEquals(10, apiCoverageDTO.getUnCoverWithApiDefinition());
        Assertions.assertEquals(apiCoverageDTO.getApiCoverage(), CalculateUtils.reportPercentage(apiCoverageDTO.getCoverWithApiDefinition(), apiCoverageDTO.getAllApiCount()));

        Assertions.assertEquals("0.00%", CalculateUtils.reportPercentage(0, 0));

        // 表格筛选测试
        ApiDefinitionPageRequest request = new ApiDefinitionPageRequest();
        request.setProjectId(project.getId());
        request.setCurrent(1);
        request.setPageSize(10);
        request.setDeleted(false);
        request.setSort(Map.of("createTime", "asc"));
        request.setProtocols(List.of("HTTP"));
        Map<String, List<String>> filters = new HashMap<>();
        request.setSort(Map.of());
        filters.put("coverFrom", List.of(ApiCoverageConstants.API_DEFINITION));
        request.setFilter(filters);

        MvcResult pageResult = this.requestPostWithOkAndReturn("/api/definition/page", request);

        Pager<Object> result = JSON.parseObject(JSON.toJSONString(JSON.parseObject(
                pageResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()), Pager.class);
        Assertions.assertEquals(result.getTotal(), 10);

        filters = new HashMap<>();
        filters.put("unCoverFrom", List.of(ApiCoverageConstants.API_DEFINITION));
        request.setFilter(filters);
        pageResult = this.requestPostWithOkAndReturn("/api/definition/page", request);
        result = JSON.parseObject(JSON.toJSONString(JSON.parseObject(
                pageResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()), Pager.class);
        Assertions.assertEquals(result.getTotal(), 10);


        filters = new HashMap<>();
        filters.put("coverFrom", List.of(ApiCoverageConstants.API_CASE));
        request.setFilter(filters);
        pageResult = this.requestPostWithOkAndReturn("/api/definition/page", request);
        result = JSON.parseObject(JSON.toJSONString(JSON.parseObject(
                pageResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()), Pager.class);
        Assertions.assertEquals(result.getTotal(), 4);

        filters = new HashMap<>();
        filters.put("unCoverFrom", List.of(ApiCoverageConstants.API_CASE));
        request.setFilter(filters);
        pageResult = this.requestPostWithOkAndReturn("/api/definition/page", request);
        result = JSON.parseObject(JSON.toJSONString(JSON.parseObject(
                pageResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()), Pager.class);
        Assertions.assertEquals(result.getTotal(), 16);


        filters = new HashMap<>();
        filters.put("coverFrom", List.of(ApiCoverageConstants.API_SCENARIO));
        request.setFilter(filters);
        pageResult = this.requestPostWithOkAndReturn("/api/definition/page", request);
        result = JSON.parseObject(JSON.toJSONString(JSON.parseObject(
                pageResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()), Pager.class);
        Assertions.assertEquals(result.getTotal(), 8);

        filters = new HashMap<>();
        filters.put("unCoverFrom", List.of(ApiCoverageConstants.API_SCENARIO));
        request.setFilter(filters);
        pageResult = this.requestPostWithOkAndReturn("/api/definition/page", request);
        result = JSON.parseObject(JSON.toJSONString(JSON.parseObject(
                pageResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()), Pager.class);
        Assertions.assertEquals(result.getTotal(), 12);
    }

    @Test
    public void urlFetchTest() {
        List<String> fetchList = new ArrayList<>();
        fetchList.add("/etag/{etag}");
        fetchList.add("/html");
        Assertions.assertFalse(ApiDefinitionUtils.isUrlInList("/brave", fetchList));
        fetchList.add("https://www.abcde.com/etag/{etag}");
        Assertions.assertTrue(ApiDefinitionUtils.isUrlInList("/brave", fetchList));
        fetchList.remove("https://www.abcde.com/etag/{etag}");
        fetchList.add("http://www.abcde.com/etag/{etag}");
        Assertions.assertTrue(ApiDefinitionUtils.isUrlInList("/brave", fetchList));
    }
}
