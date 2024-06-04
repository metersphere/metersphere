package io.metersphere.system.controller;

import io.metersphere.sdk.constants.ScheduleResourceType;
import io.metersphere.sdk.constants.ScheduleType;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.Schedule;
import io.metersphere.system.dto.taskcenter.enums.ScheduleTagType;
import io.metersphere.system.dto.taskcenter.request.TaskCenterScheduleBatchRequest;
import io.metersphere.system.dto.taskcenter.request.TaskCenterSchedulePageRequest;
import io.metersphere.system.mapper.ScheduleMapper;
import io.metersphere.system.schedule.ScheduleService;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
class TaskCenterScheduleControllerTests extends BaseTest {

    private static final String BASE_PATH = "/task/center/";
    private final static String SCHEDULED_PROJECT_PAGE = BASE_PATH + "project/schedule/page";
    private final static String SCHEDULED_ORG_PAGE = BASE_PATH + "org/schedule/page";
    private final static String SCHEDULED_SYSTEM_PAGE = BASE_PATH + "system/schedule/page";


    private static final ResultMatcher ERROR_REQUEST_MATCHER = status().is5xxServerError();

    @Resource
    ScheduleMapper scheduleMapper;

    @Resource
    ScheduleService scheduleService;


    @Test
    @Order(9)
    @Sql(scripts = {"/dml/init_task_center.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    void getPage() throws Exception {
        doTaskCenterSchedulePage("KEYWORD", SCHEDULED_PROJECT_PAGE, ScheduleTagType.API_IMPORT.toString());
        doTaskCenterSchedulePage("FILTER", SCHEDULED_PROJECT_PAGE, ScheduleTagType.API_IMPORT.toString());
        doTaskCenterSchedulePage("KEYWORD", SCHEDULED_ORG_PAGE, ScheduleTagType.API_IMPORT.toString());
        doTaskCenterSchedulePage("FILTER", SCHEDULED_ORG_PAGE, ScheduleTagType.API_IMPORT.toString());
        doTaskCenterSchedulePage("KEYWORD", SCHEDULED_SYSTEM_PAGE, ScheduleTagType.API_IMPORT.toString());
        doTaskCenterSchedulePage("FILTER", SCHEDULED_SYSTEM_PAGE, ScheduleTagType.API_IMPORT.toString());

        doTaskCenterSchedulePage("KEYWORD", SCHEDULED_PROJECT_PAGE, ScheduleTagType.API_SCENARIO.toString());
        doTaskCenterSchedulePage("FILTER", SCHEDULED_PROJECT_PAGE, ScheduleTagType.API_SCENARIO.toString());
        doTaskCenterSchedulePage("KEYWORD", SCHEDULED_ORG_PAGE, ScheduleTagType.API_SCENARIO.toString());
        doTaskCenterSchedulePage("FILTER", SCHEDULED_ORG_PAGE, ScheduleTagType.API_SCENARIO.toString());
        doTaskCenterSchedulePage("KEYWORD", SCHEDULED_SYSTEM_PAGE, ScheduleTagType.API_SCENARIO.toString());
        doTaskCenterSchedulePage("FILTER", SCHEDULED_SYSTEM_PAGE, ScheduleTagType.API_SCENARIO.toString());

        doTaskCenterSchedulePage("KEYWORD", SCHEDULED_PROJECT_PAGE, ScheduleTagType.TEST_PLAN.toString());
        doTaskCenterSchedulePage("FILTER", SCHEDULED_PROJECT_PAGE, ScheduleTagType.TEST_PLAN.toString());
        doTaskCenterSchedulePage("KEYWORD", SCHEDULED_ORG_PAGE, ScheduleTagType.TEST_PLAN.toString());
        doTaskCenterSchedulePage("FILTER", SCHEDULED_ORG_PAGE, ScheduleTagType.TEST_PLAN.toString());
        doTaskCenterSchedulePage("KEYWORD", SCHEDULED_SYSTEM_PAGE, ScheduleTagType.TEST_PLAN.toString());
        doTaskCenterSchedulePage("FILTER", SCHEDULED_SYSTEM_PAGE, ScheduleTagType.TEST_PLAN.toString());


        this.requestGet("/task/center/system/schedule/total");
        this.requestGet("/task/center/org/schedule/total");
        this.requestGet("/task/center/project/schedule/total");

        this.requestGet("/task/center/system/real/total");
        this.requestGet("/task/center/org/real/total");
        this.requestGet("/task/center/project/real/total");

    }

    private void doTaskCenterSchedulePage(String search, String url, String scheduleTagType) throws Exception {
        TaskCenterSchedulePageRequest request = new TaskCenterSchedulePageRequest();
        request.setScheduleTagType(scheduleTagType);
        request.setCurrent(1);
        request.setPageSize(10);
        request.setSort(Map.of("createTime", "asc"));
        // "KEYWORD", "FILTER"
        switch (search) {
            case "KEYWORD" -> configureKeywordSearch(request);
            case "FILTER" -> configureFilterSearch(request);
            default -> {
            }
        }

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .header(SessionConstants.CURRENT_PROJECT, DEFAULT_PROJECT_ID)
                        .header(SessionConstants.CURRENT_ORGANIZATION, DEFAULT_ORGANIZATION_ID)
                        .content(JSON.toJSONString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();

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

    private void doTaskCenterSchedulePageError(String url, String scheduleTagType) throws Exception {
        TaskCenterSchedulePageRequest request = new TaskCenterSchedulePageRequest();
        request.setScheduleTagType(scheduleTagType);
        request.setCurrent(1);
        request.setPageSize(10);
        request.setSort(Map.of("createTime", "asc"));
        configureKeywordSearch(request);

        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .header(SessionConstants.CURRENT_PROJECT, "DEFAULT_PROJECT_ID")
                        .header(SessionConstants.CURRENT_ORGANIZATION, "DEFAULT_ORGANIZATION_ID")
                        .content(JSON.toJSONString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(ERROR_REQUEST_MATCHER);
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
    @Order(10)
    void getPageError() throws Exception {
        doTaskCenterSchedulePageError(SCHEDULED_PROJECT_PAGE, ScheduleTagType.API_IMPORT.toString());
        doTaskCenterSchedulePageError(SCHEDULED_ORG_PAGE, ScheduleTagType.API_IMPORT.toString());
    }

    @Test
    @Order(12)
    void testDel() throws Exception {
        LogUtils.info("delete Schedule test");
        String scheduleId = "1";
        // @@请求成功
        this.requestGet("/task/center/system/schedule/delete/" + "API_IMPORT/" + scheduleId);
        Schedule schedule = scheduleMapper.selectByPrimaryKey(scheduleId);
        Assertions.assertNull(schedule);
        this.requestGet("/task/center/org/schedule/delete/" + "API_IMPORT/" + "4");
        this.requestGet("/task/center/project/schedule/delete/" + "API_SCENARIO/" + "2");
        this.requestGet("/task/center/system/schedule/delete/" + "API_SCENARIO/" + "schedule-121", ERROR_REQUEST_MATCHER);
    }

    @Test
    @Order(13)
    void testEnable() throws Exception {
        //先导入数据
        Schedule schedule = new Schedule();
        schedule.setName("test-schedule-switch");
        schedule.setResourceId("test-schedule-switch");
        schedule.setEnable(true);
        schedule.setValue("0 0/1 * * * ?");
        schedule.setKey("test-resource-id");
        schedule.setCreateUser("admin");
        schedule.setProjectId(DEFAULT_PROJECT_ID);
        schedule.setConfig("{}");
        schedule.setJob("io.metersphere.api.service.schedule.SwaggerUrlImportJob");
        schedule.setType(ScheduleType.CRON.name());
        schedule.setResourceType(ScheduleResourceType.API_IMPORT.name());

        scheduleService.addSchedule(schedule);
        scheduleService.getSchedule(schedule.getId());
        scheduleService.editSchedule(schedule);
        scheduleService.getScheduleByResource(schedule.getResourceId(), schedule.getJob());
        this.requestGet("/task/center/system/schedule/switch/" + "API_IMPORT/" + "test-schedule-switch");
        this.requestGet("/task/center/org/schedule/switch/" + "API_IMPORT/" + "test-schedule-switch");
        this.requestGet("/task/center/project/schedule/switch/" + "API_IMPORT/" + "test-schedule-switch");
        this.requestPost("/task/center/system/schedule/update/" + "API_IMPORT/" + "test-schedule-switch", "/0 0/2 * * * ?");
        this.requestPost("/task/center/org/schedule/update/" + "API_IMPORT/" + "test-schedule-switch", "/0 0/2 * * * ?");
        this.requestPost("/task/center/project/schedule/update/" + "API_IMPORT/" + "test-schedule-switch", "/0 0/2 * * * ?");

        //批量操作
        TaskCenterScheduleBatchRequest request = new TaskCenterScheduleBatchRequest();
        request.setSelectIds(List.of("test-schedule-switch"));
        request.setSelectAll(false);
        this.requestPost("/task/center/system/schedule/batch-enable", request);
        request.setSelectAll(true);
        this.requestPost("/task/center/system/schedule/batch-disable", request);
        this.requestPost("/task/center/org/schedule/batch-enable", request);
        this.requestPost("/task/center/org/schedule/batch-disable", request);
        this.requestPost("/task/center/project/schedule/batch-enable", request);
        this.requestPost("/task/center/project/schedule/batch-disable", request);


    }
}
