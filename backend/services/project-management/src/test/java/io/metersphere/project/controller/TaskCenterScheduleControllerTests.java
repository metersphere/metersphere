package io.metersphere.project.controller;

import io.metersphere.api.domain.ApiDefinitionSwagger;
import io.metersphere.api.mapper.ApiDefinitionSwaggerMapper;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.Schedule;
import io.metersphere.system.dto.taskcenter.enums.ScheduleTagType;
import io.metersphere.system.dto.taskcenter.request.OrganizationTaskCenterSchedulePageRequest;
import io.metersphere.system.dto.taskcenter.request.ProjectTaskCenterSchedulePageRequest;
import io.metersphere.system.dto.taskcenter.request.TaskCenterSchedulePageRequest;
import io.metersphere.system.mapper.ScheduleMapper;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
class TaskCenterScheduleControllerTests extends BaseTest {

    private static final String BASE_PATH = "/task/center/";
    private final static String SCHEDULED_PROJECT_PAGE = BASE_PATH + "project/scheduled/page";
    private final static String SCHEDULED_ORG_PAGE = BASE_PATH + "org/scheduled/page";
    private final static String SCHEDULED_SYSTEM_PAGE = BASE_PATH + "system/scheduled/page";

    private final static String SCHEDULED_DELETE = BASE_PATH + "scheduled/delete/";

    private static final ResultMatcher ERROR_REQUEST_MATCHER = status().is5xxServerError();

    @Resource
    ScheduleMapper scheduleMapper;

    @Resource
    ApiDefinitionSwaggerMapper apiDefinitionSwaggerMapper;

    @Test
    @Order(9)
    @Sql(scripts = {"/dml/init_task_center.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    void getPage() throws Exception {
        doTaskCenterSchedulePageByProject("KEYWORD", ScheduleTagType.API_IMPORT.toString());
        doTaskCenterSchedulePageByProject("FILTER", ScheduleTagType.API_IMPORT.toString());
        doTaskCenterSchedulePageByOrg("KEYWORD", ScheduleTagType.API_IMPORT.toString());
        doTaskCenterSchedulePageByOrg("FILTER", ScheduleTagType.API_IMPORT.toString());
        doTaskCenterSchedulePageBySystem("KEYWORD", ScheduleTagType.API_IMPORT.toString());
        doTaskCenterSchedulePageBySystem("FILTER", ScheduleTagType.API_IMPORT.toString());

        doTaskCenterSchedulePageByProject("KEYWORD", ScheduleTagType.TEST_RESOURCE.toString());
        doTaskCenterSchedulePageByProject("FILTER", ScheduleTagType.TEST_RESOURCE.toString());
        doTaskCenterSchedulePageByOrg("KEYWORD", ScheduleTagType.TEST_RESOURCE.toString());
        doTaskCenterSchedulePageByOrg("FILTER", ScheduleTagType.TEST_RESOURCE.toString());
        doTaskCenterSchedulePageBySystem("KEYWORD", ScheduleTagType.TEST_RESOURCE.toString());
        doTaskCenterSchedulePageBySystem("FILTER", ScheduleTagType.TEST_RESOURCE.toString());
    }

    private void doTaskCenterSchedulePageByProject(String search, String scheduleTagType) throws Exception {
        ProjectTaskCenterSchedulePageRequest request = new ProjectTaskCenterSchedulePageRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setScheduleTagType(scheduleTagType);
        request.setCurrent(1);
        request.setPageSize(10);
        request.setSort(Map.of("createTime", "asc"));
        // "KEYWORD", "FILTER"
        switch (search) {
            case "KEYWORD" -> configureKeywordSearch(request);
            case "FILTER" -> configureFilterSearch(request);
            default -> {}
        }

        MvcResult mvcResult = this.requestPostWithOkAndReturn(SCHEDULED_PROJECT_PAGE, request);
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

    private void doTaskCenterSchedulePageByOrg(String search, String scheduleTagType) throws Exception {
        OrganizationTaskCenterSchedulePageRequest request = new OrganizationTaskCenterSchedulePageRequest();
        request.setOrganizationId(DEFAULT_ORGANIZATION_ID);
        request.setScheduleTagType(scheduleTagType);
        request.setCurrent(1);
        request.setPageSize(10);
        request.setSort(Map.of("createTime", "asc"));
        // KEYWORD", "FILTER"
        switch (search) {
            case "KEYWORD" -> configureKeywordSearch(request);
            case "FILTER" -> configureFilterSearch(request);
            default -> {}
        }

        MvcResult mvcResult = this.requestPostWithOkAndReturn(SCHEDULED_ORG_PAGE, request);
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

    private void doTaskCenterSchedulePageBySystem(String search, String scheduleTagType) throws Exception {
       TaskCenterSchedulePageRequest request = new TaskCenterSchedulePageRequest();
        request.setScheduleTagType(scheduleTagType);
        request.setCurrent(1);
        request.setPageSize(10);
        request.setSort(Map.of());
        // "KEYWORD", "FILTER"
        switch (search) {
            case "KEYWORD" -> configureKeywordSearch(request);
            case "FILTER" -> configureFilterSearch(request);
            default -> {}
        }

        MvcResult mvcResult = this.requestPostWithOkAndReturn(SCHEDULED_SYSTEM_PAGE, request);
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

    private void configureKeywordSearch(TaskCenterSchedulePageRequest request) {
        request.setKeyword("Schedule");
        request.setSort(Map.of("resourceType", "API_SCENARIO"));
    }

    private void configureFilterSearch(TaskCenterSchedulePageRequest request) {
        Map<String, List<String>> filters = new HashMap<>();
        request.setSort(Map.of());
        filters.put("resourceType", List.of("API_SCENARIO", "API_IMPORT"));
        request.setFilter(filters);
    }


    @Test
    @Order(12)
    void testDel() throws Exception {
        LogUtils.info("delete Schedule test");
        String scheduleId = "1";
        Schedule oldSchedule = scheduleMapper.selectByPrimaryKey(scheduleId);
        // @@请求成功
        this.requestGet(SCHEDULED_DELETE + scheduleId);
        Schedule schedule = scheduleMapper.selectByPrimaryKey(scheduleId);
        Assertions.assertNull(schedule);
        if (ScheduleTagType.API_IMPORT.getNames().contains(oldSchedule.getType())) {
            ApiDefinitionSwagger apiDefinitionSwagger = apiDefinitionSwaggerMapper.selectByPrimaryKey(oldSchedule.getResourceId());
            Assertions.assertNull(apiDefinitionSwagger);
        }
        this.requestGet(SCHEDULED_DELETE + "schedule-121", ERROR_REQUEST_MATCHER);
    }

}
