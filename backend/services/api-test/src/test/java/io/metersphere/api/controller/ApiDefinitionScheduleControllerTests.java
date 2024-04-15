package io.metersphere.api.controller;

import io.metersphere.api.domain.ApiDefinitionSwagger;
import io.metersphere.api.domain.ApiDefinitionSwaggerExample;
import io.metersphere.api.dto.definition.ApiScheduleDTO;
import io.metersphere.api.dto.definition.request.ApiScheduleRequest;
import io.metersphere.api.mapper.ApiDefinitionSwaggerMapper;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.domain.Schedule;
import io.metersphere.system.domain.ScheduleExample;
import io.metersphere.system.mapper.ScheduleMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ApiDefinitionScheduleControllerTests extends BaseTest {

    private static final String BASE_PATH = "/api/definition/schedule/";
    private final static String ADD = BASE_PATH + "add";
    private final static String UPDATE = BASE_PATH + "update";
    private final static String GET = BASE_PATH + "get/";
    @Resource
    private ApiDefinitionSwaggerMapper apiDefinitionSwaggerMapper;
    @Resource
    private ScheduleMapper scheduleMapper;

    private static String ID;
    private static String SCHEDULE_ID;
    private static final ResultMatcher BAD_REQUEST_MATCHER = status().isBadRequest();
    private static final ResultMatcher ERROR_REQUEST_MATCHER = status().is5xxServerError();

    @Order(1)
    @Test
    public void add() throws Exception {
        ApiScheduleRequest request = new ApiScheduleRequest();
        request.setName("定时任务1");
        request.setValue("0 0/1 * * * ?");
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setCoverData(true);
        request.setSwaggerUrl("https://petstore3.swagger.io/api/v3/openapi.json");
        this.requestPostWithOk(ADD, request);
        ApiDefinitionSwaggerExample example = new ApiDefinitionSwaggerExample();
        example.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID);
        List<ApiDefinitionSwagger> apiDefinitionSwaggers = apiDefinitionSwaggerMapper.selectByExample(example);
        assert apiDefinitionSwaggers.size() == 1;
        ID = apiDefinitionSwaggers.getFirst().getId();
        ScheduleExample scheduleExample = new ScheduleExample();
        scheduleExample.createCriteria().andResourceIdEqualTo(apiDefinitionSwaggers.getFirst().getId());
        List<Schedule> schedules = scheduleMapper.selectByExample(scheduleExample);
        assert schedules.size() == 1;
        SCHEDULE_ID = schedules.getFirst().getId();

        request = new ApiScheduleRequest();
        request.setName("定时任务2");
        request.setValue("0 0/1 * * * ?");
        request.setProjectId(DEFAULT_PROJECT_ID);
        this.requestPost(ADD, request, BAD_REQUEST_MATCHER);
        request.setSwaggerUrl("https://petstore3.swagger.io/api/v3/openapi.json");
        request.setName(null);
        this.requestPost(ADD, request, BAD_REQUEST_MATCHER);
        request.setName("定时任务2");
        request.setValue(null);
        this.requestPost(ADD, request, BAD_REQUEST_MATCHER);
        request.setValue("0 0/1 * * * ?");
        request.setProjectId(null);
        this.requestPost(ADD, request, BAD_REQUEST_MATCHER);
    }

    @Order(2)
    @Test
    public void update() throws Exception {
        ApiScheduleRequest request = new ApiScheduleRequest();
        request.setName("定时任务2");
        request.setValue("0 0/1 * * * ?");
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setCoverData(true);
        request.setId(ID);
        request.setSwaggerUrl("https://petstore3.swagger.io/api/v3/openapi.json");
        this.requestPostWithOk(UPDATE, request);
        ApiDefinitionSwagger apiDefinitionSwagger = apiDefinitionSwaggerMapper.selectByPrimaryKey(ID);
        assert apiDefinitionSwagger.getName().equals("定时任务2");

        request.setId("123");
        this.requestPost(UPDATE, request, ERROR_REQUEST_MATCHER);
        request.setId(null);
        this.requestPost(UPDATE, request, BAD_REQUEST_MATCHER);
        request.setId("123");
        request.setName(null);
        this.requestPost(UPDATE, request, BAD_REQUEST_MATCHER);
        request.setName("定时任务2");
        request.setValue(null);
        this.requestPost(UPDATE, request, BAD_REQUEST_MATCHER);
        request.setValue("0 0/1 * * * ?");
        request.setProjectId(null);
        this.requestPost(UPDATE, request, BAD_REQUEST_MATCHER);
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setSwaggerUrl(null);
        this.requestPost(UPDATE, request, BAD_REQUEST_MATCHER);
    }

    @Order(3)
    @Test
    public void get() throws Exception {
        this.requestGet(GET + "123", ERROR_REQUEST_MATCHER);
        MvcResult mvcResult = this.requestGetWithOk(GET + ID)
                .andReturn();
        ApiScheduleDTO apiScheduleDTO = ApiDataUtils.parseObject(JSON.toJSONString(parseResponse(mvcResult).get("data")), ApiScheduleDTO.class);
        assert apiScheduleDTO.getId().equals(ID);
        assert apiScheduleDTO.getName().equals("定时任务2");
        assert apiScheduleDTO.getValue().equals("0 0/1 * * * ?");
        assert apiScheduleDTO.getSwaggerUrl().equals("https://petstore3.swagger.io/api/v3/openapi.json");
    }

    @Order(4)
    @Test
    public void switchSchedule() throws Exception {
        this.requestGet(BASE_PATH + "switch/123", ERROR_REQUEST_MATCHER);
        this.requestGetWithOk(BASE_PATH + "switch/" + SCHEDULE_ID);
        Schedule schedule = scheduleMapper.selectByPrimaryKey(SCHEDULE_ID);
        //断言为false
        assert !schedule.getEnable();
        this.requestGetWithOk(BASE_PATH + "switch/" + SCHEDULE_ID);
        schedule = scheduleMapper.selectByPrimaryKey(SCHEDULE_ID);
        //断言为true
        assert schedule.getEnable();
    }

    @Order(6)
    @Test
    public void deleteSchedule() throws Exception {
        this.requestGet(BASE_PATH + "delete/123", ERROR_REQUEST_MATCHER);
        this.requestGetWithOk(BASE_PATH + "delete/" + SCHEDULE_ID);
        ApiDefinitionSwaggerExample example = new ApiDefinitionSwaggerExample();
        example.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID);
        List<ApiDefinitionSwagger> apiDefinitionSwaggers = apiDefinitionSwaggerMapper.selectByExample(example);
        assert apiDefinitionSwaggers.isEmpty();
        ScheduleExample scheduleExample = new ScheduleExample();
        scheduleExample.createCriteria().andResourceIdEqualTo(ID);
        List<Schedule> schedules = scheduleMapper.selectByExample(scheduleExample);
        assert schedules.isEmpty();
    }

}
