package io.metersphere.system.controller;

import io.metersphere.api.domain.ApiDefinitionSwagger;
import io.metersphere.api.mapper.ApiDefinitionSwaggerMapper;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.Schedule;
import io.metersphere.system.dto.taskcenter.enums.ScheduleTagType;
import io.metersphere.system.dto.taskcenter.request.TaskCenterSchedulePageRequest;
import io.metersphere.system.mapper.ExtSwaggerMapper;
import io.metersphere.system.mapper.ScheduleMapper;
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


@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
class TaskCenterScheduleControllerTests extends BaseTest {

    private static final String BASE_PATH = "/task/center/";
    private final static String SCHEDULED_PROJECT_PAGE = BASE_PATH + "project/schedule/page";
    private final static String SCHEDULED_ORG_PAGE = BASE_PATH + "org/schedule/page";
    private final static String SCHEDULED_SYSTEM_PAGE = BASE_PATH + "system/schedule/page";
    private final static String SCHEDULED_DELETE = BASE_PATH + "schedule/delete/";

    private static final ResultMatcher ERROR_REQUEST_MATCHER = status().is5xxServerError();

    @Resource
    ScheduleMapper scheduleMapper;

    @Resource
    ExtSwaggerMapper extSwaggerMapper;



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

        doTaskCenterSchedulePage("KEYWORD", SCHEDULED_PROJECT_PAGE, ScheduleTagType.TEST_RESOURCE.toString());
        doTaskCenterSchedulePage("FILTER", SCHEDULED_PROJECT_PAGE, ScheduleTagType.TEST_RESOURCE.toString());
        doTaskCenterSchedulePage("KEYWORD", SCHEDULED_ORG_PAGE, ScheduleTagType.TEST_RESOURCE.toString());
        doTaskCenterSchedulePage("FILTER", SCHEDULED_ORG_PAGE, ScheduleTagType.TEST_RESOURCE.toString());
        doTaskCenterSchedulePage("KEYWORD", SCHEDULED_SYSTEM_PAGE, ScheduleTagType.TEST_RESOURCE.toString());
        doTaskCenterSchedulePage("FILTER", SCHEDULED_SYSTEM_PAGE, ScheduleTagType.TEST_RESOURCE.toString());
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
            default -> {}
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

    private void doTaskCenterSchedulePageError( String url, String scheduleTagType) throws Exception {
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
        Schedule oldSchedule = scheduleMapper.selectByPrimaryKey(scheduleId);
        // @@请求成功
        this.requestGet(SCHEDULED_DELETE + scheduleId);
        Schedule schedule = scheduleMapper.selectByPrimaryKey(scheduleId);
        Assertions.assertNull(schedule);
        if (ScheduleTagType.API_IMPORT.getNames().contains(oldSchedule.getType())) {
            int count = extSwaggerMapper.selectByPrimaryKey(oldSchedule.getResourceId());
            Assertions.assertTrue(count > 0);
        }
        this.requestGet(SCHEDULED_DELETE + "schedule-121", ERROR_REQUEST_MATCHER);
    }

}
