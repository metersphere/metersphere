package io.metersphere.api.controller;

import io.metersphere.sdk.constants.TaskCenterResourceType;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.dto.taskcenter.request.OrganizationTaskCenterPageRequest;
import io.metersphere.system.dto.taskcenter.request.ProjectTaskCenterPageRequest;
import io.metersphere.system.dto.taskcenter.request.TaskCenterPageRequest;
import io.metersphere.system.utils.Pager;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ApiTaskCenterControllerTests extends BaseTest {

    private static final String BASE_PATH = "/task/center/api/";
    private final static String REAL_TIME_PROJECT_PAGE = BASE_PATH + "project/real-time/page";
    private final static String REAL_TIME_ORG_PAGE = BASE_PATH + "org/real-time/page";
    private final static String REAL_TIME_SYSTEM_PAGE = BASE_PATH + "system/real-time/page";

    @Test
    @Order(9)
    @Sql(scripts = {"/dml/init_api_definition.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void getPage() throws Exception {
        doTaskCenterPageByProject("KEYWORD", TaskCenterResourceType.API_CASE.toString());
        doTaskCenterPageByProject("FILTER", TaskCenterResourceType.API_CASE.toString());
        doTaskCenterPageByOrg("KEYWORD", TaskCenterResourceType.API_CASE.toString());
        doTaskCenterPageByOrg("FILTER", TaskCenterResourceType.API_CASE.toString());
        doTaskCenterPageBySystem("KEYWORD", TaskCenterResourceType.API_CASE.toString());
        doTaskCenterPageBySystem("FILTER", TaskCenterResourceType.API_CASE.toString());

        doTaskCenterPageByProject("KEYWORD", TaskCenterResourceType.API_SCENARIO.toString());
        doTaskCenterPageByProject("FILTER", TaskCenterResourceType.API_SCENARIO.toString());
        doTaskCenterPageByOrg("KEYWORD", TaskCenterResourceType.API_SCENARIO.toString());
        doTaskCenterPageByOrg("FILTER", TaskCenterResourceType.API_SCENARIO.toString());
        doTaskCenterPageBySystem("KEYWORD", TaskCenterResourceType.API_SCENARIO.toString());
        doTaskCenterPageBySystem("FILTER", TaskCenterResourceType.API_SCENARIO.toString());
    }

    private void doTaskCenterPageByProject(String search, String moduleType) throws Exception {
        ProjectTaskCenterPageRequest request = new ProjectTaskCenterPageRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setModuleType(moduleType);
        request.setCurrent(1);
        request.setPageSize(10);
        request.setSort(Map.of("startTime", "asc"));
        // "KEYWORD", "FILTER"
        switch (search) {
            case "KEYWORD" -> configureKeywordSearch(request);
            case "FILTER" -> configureFilterSearch(request);
            default -> {}
        }

        MvcResult mvcResult = this.requestPostWithOkAndReturn(REAL_TIME_PROJECT_PAGE, request);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        Pager<?> pageData = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
        // 返回值不为空
        Assertions.assertNotNull(pageData);
        // 返回值的页码和当前页码相同
        Assertions.assertEquals(pageData.getCurrent(), request.getCurrent());
        // 返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(JSON.parseArray(JSON.toJSONString(pageData.getList())).size() <= request.getPageSize());

    }

    private void doTaskCenterPageByOrg(String search, String moduleType) throws Exception {
        OrganizationTaskCenterPageRequest request = new OrganizationTaskCenterPageRequest();
        request.setOrganizationId(DEFAULT_ORGANIZATION_ID);
        request.setModuleType(moduleType);
        request.setCurrent(1);
        request.setPageSize(10);
        request.setSort(Map.of("startTime", "asc"));
        // KEYWORD", "FILTER"
        switch (search) {
            case "KEYWORD" -> configureKeywordSearch(request);
            case "FILTER" -> configureFilterSearch(request);
            default -> {}
        }

        MvcResult mvcResult = this.requestPostWithOkAndReturn(REAL_TIME_ORG_PAGE, request);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        Pager<?> pageData = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
        // 返回值不为空
        Assertions.assertNotNull(pageData);
        // 返回值的页码和当前页码相同
        Assertions.assertEquals(pageData.getCurrent(), request.getCurrent());
        // 返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(JSON.parseArray(JSON.toJSONString(pageData.getList())).size() <= request.getPageSize());

    }

    private void doTaskCenterPageBySystem(String search, String moduleType) throws Exception {
       TaskCenterPageRequest request = new TaskCenterPageRequest();
        request.setModuleType(moduleType);
        request.setCurrent(1);
        request.setPageSize(10);
        request.setSort(Map.of("startTime", "asc"));
        // "KEYWORD", "FILTER"
        switch (search) {
            case "KEYWORD" -> configureKeywordSearch(request);
            case "FILTER" -> configureFilterSearch(request);
            default -> {}
        }

        MvcResult mvcResult = this.requestPostWithOkAndReturn(REAL_TIME_SYSTEM_PAGE, request);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        Pager<?> pageData = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
        // 返回值不为空
        Assertions.assertNotNull(pageData);
        // 返回值的页码和当前页码相同
        Assertions.assertEquals(pageData.getCurrent(), request.getCurrent());
        // 返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(JSON.parseArray(JSON.toJSONString(pageData.getList())).size() <= request.getPageSize());

    }

    private void configureKeywordSearch(TaskCenterPageRequest request) {
        request.setKeyword("18");
        request.setSort(Map.of("triggerMode", "asc"));
    }

    private void configureFilterSearch(TaskCenterPageRequest request) {
        Map<String, List<String>> filters = new HashMap<>();
        request.setSort(Map.of());
        filters.put("triggerMode", List.of("MANUAL"));
        request.setFilter(filters);
    }

}
