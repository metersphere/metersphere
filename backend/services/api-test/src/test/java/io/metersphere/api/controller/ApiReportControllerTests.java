package io.metersphere.api.controller;

import io.metersphere.api.domain.ApiReport;
import io.metersphere.api.domain.ApiReportExample;
import io.metersphere.api.domain.ApiReportStep;
import io.metersphere.api.domain.ApiReportStepExample;
import io.metersphere.api.dto.scenario.ApiScenarioDTO;
import io.metersphere.api.mapper.ApiReportMapper;
import io.metersphere.api.mapper.ApiReportStepMapper;
import io.metersphere.api.service.definition.ApiReportService;
import io.metersphere.sdk.constants.ApiReportStatus;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.dto.sdk.BasePageRequest;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ApiReportControllerTests extends BaseTest {

    @Resource
    private ApiReportService apiReportService;
    @Resource
    private ApiReportMapper apiReportMapper;
    @Resource
    private ApiReportStepMapper apiReportStepMapper;

    @Test
    @Order(1)
    public void testInsert() {
        List<ApiReport> reports = new ArrayList<>();
        for (int i = 0; i < 2515; i++) {
            ApiReport apiReport = new ApiReport();
            apiReport.setId("api-report-id" + i);
            apiReport.setProjectId(DEFAULT_PROJECT_ID);
            apiReport.setName("api-report-name" + i);
            apiReport.setStartTime(System.currentTimeMillis());
            apiReport.setResourceId("api-resource-id" + i);
            apiReport.setCreateUser("admin");
            apiReport.setUpdateUser("admin");
            apiReport.setUpdateTime(System.currentTimeMillis());
            apiReport.setPoolId("api-pool-id" + i);
            apiReport.setEnvironmentId("api-environment-id" + i);
            apiReport.setRunMode("api-run-mode" + i);
            if (i % 50 == 0) {
                apiReport.setStatus(ApiReportStatus.SUCCESS.name());
            } else if (i % 39 == 0) {
                apiReport.setStatus(ApiReportStatus.ERROR.name());
            }
            apiReport.setTriggerMode("api-trigger-mode" + i);
            apiReport.setVersionId("api-version-id" + i);
            reports.add(apiReport);
        }
        apiReportService.insertApiReport(reports);
        List<ApiReport> reports1 = apiReportMapper.selectByExample(new ApiReportExample());
        Assertions.assertEquals(reports1.size(), 2515);

        List<ApiReportStep> steps = new ArrayList<>();
        for (int i = 0; i < 1515; i++) {
            ApiReportStep apiReportStep = new ApiReportStep();
            apiReportStep.setStepId("api-report-step-id" + i);
            apiReportStep.setReportId("api-report-id" + i);
            apiReportStep.setSort(0L);
            apiReportStep.setStepType("case");
            steps.add(apiReportStep);
        }
        apiReportService.insertApiReportStep(steps);
        List<ApiReportStep> steps1 = apiReportStepMapper.selectByExample(new ApiReportStepExample());
        Assertions.assertEquals(steps1.size(), 1515);
    }

    private MvcResult responsePost(String url, Object param) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .header(SessionConstants.CURRENT_PROJECT, DEFAULT_PROJECT_ID)
                        .content(JSON.toJSONString(param))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    public static <T> T parseObjectFromMvcResult(MvcResult mvcResult, Class<T> parseClass) {
        try {
            String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
            ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
            //返回请求正常
            Assertions.assertNotNull(resultHolder);
            return JSON.parseObject(JSON.toJSONString(resultHolder.getData()), parseClass);
        } catch (Exception ignore) {
        }
        return null;
    }

    @Test
    @Order(2)
    public void testGetPage() throws Exception {
        BasePageRequest request = new BasePageRequest();
        request.setCurrent(1);
        request.setPageSize(10);
        MvcResult mvcResult = responsePost("/api/report/case/page", request);
        Pager<?> returnPager = parseObjectFromMvcResult(mvcResult, Pager.class);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(returnPager.getCurrent(), request.getCurrent());
        ;
        //返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(((List<ApiScenarioDTO>) returnPager.getList()).size() <= request.getPageSize());
        //过滤
        request.setFilter(new HashMap<>() {{
            put("status", List.of("SUCCESS", "ERROR"));
        }});
        mvcResult = responsePost("/api/report/case/page", request);
        returnPager = parseObjectFromMvcResult(mvcResult, Pager.class);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        Assertions.assertTrue(((List<ApiReport>) returnPager.getList()).size() <= request.getPageSize());
        List<ApiReport> list = JSON.parseArray(JSON.toJSONString(returnPager.getList()), ApiReport.class);
        list.forEach(apiReport -> {
            Assertions.assertTrue(apiReport.getStatus().equals("SUCCESS") || apiReport.getStatus().equals("ERROR"));
        });

    }

}
