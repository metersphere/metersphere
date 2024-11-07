package io.metersphere.api.controller;

import io.metersphere.api.constants.ApiScenarioStepType;
import io.metersphere.api.domain.*;
import io.metersphere.api.dto.definition.ApiReportBatchRequest;
import io.metersphere.api.dto.definition.ApiReportPageRequest;
import io.metersphere.api.dto.scenario.ApiScenarioDTO;
import io.metersphere.api.dto.scenario.ApiScenarioReportDTO;
import io.metersphere.api.dto.scenario.ApiScenarioReportDetailDTO;
import io.metersphere.api.dto.share.ApiReportShareRequest;
import io.metersphere.api.dto.share.ShareInfoDTO;
import io.metersphere.api.mapper.ApiScenarioReportDetailBlobMapper;
import io.metersphere.api.mapper.ApiScenarioReportDetailMapper;
import io.metersphere.api.mapper.ApiScenarioReportLogMapper;
import io.metersphere.api.mapper.ApiScenarioReportMapper;
import io.metersphere.api.service.scenario.ApiScenarioReportService;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.project.domain.ProjectApplication;
import io.metersphere.project.domain.ProjectApplicationExample;
import io.metersphere.project.mapper.ProjectApplicationMapper;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.ResultStatus;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.constants.ShareInfoType;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.domain.EnvironmentExample;
import io.metersphere.sdk.domain.EnvironmentGroup;
import io.metersphere.sdk.domain.ShareInfo;
import io.metersphere.sdk.mapper.EnvironmentGroupMapper;
import io.metersphere.sdk.mapper.EnvironmentMapper;
import io.metersphere.sdk.mapper.ShareInfoMapper;
import io.metersphere.sdk.util.CalculateUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.TestResourcePool;
import io.metersphere.system.domain.TestResourcePoolExample;
import io.metersphere.system.mapper.TestResourcePoolMapper;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ApiScenarioReportControllerTests extends BaseTest {

    @Resource
    private ApiScenarioReportService apiScenarioReportService;
    @Resource
    private ApiScenarioReportMapper apiScenarioReportMapper;
    @Resource
    private ApiScenarioReportDetailMapper apiScenarioReportDetailMapper;
    @Resource
    private ApiScenarioReportDetailBlobMapper apiScenarioReportDetailBlobMapper;
    @Resource
    private ShareInfoMapper shareInfoMapper;
    @Resource
    private ProjectApplicationMapper projectApplicationMapper;
    @Resource
    private ApiScenarioReportLogMapper apiScenarioReportLogMapper;
    @Resource
    private TestResourcePoolMapper testResourcePoolMapper;
    @Resource
    private EnvironmentMapper environmentMapper;
    @Resource
    private EnvironmentGroupMapper environmentGroupMapper;

    private static final String BASIC = "/api/report/scenario";
    private static final String PAGE = BASIC + "/page";
    private static final String RENAME = BASIC + "/rename/";
    private static final String DELETE = BASIC + "/delete/";
    private static final String GET = BASIC + "/get/";
    private static final String BATCH_DELETE = BASIC + "/batch/delete";
    private static final String BATCH_PARAM = BASIC + "/batch-param";
    private static final String DETAIL = BASIC + "/get/detail/";
    private static final String EXPORT_REPORT = BASIC + "/export/{0}";
    private static final String BATCH_EXPORT_REPORT = BASIC + "/batch-export";
    private static final String TASK_STEP = BASIC + "/task-step/{0}";
    private static final String TASK_REPORT = BASIC + "/task-report/";

    @Test
    @Order(1)
    public void testInsert() throws Exception {
        List<ApiScenarioReport> reports = new ArrayList<>();
        List<ApiScenarioRecord> records = new ArrayList<>();
        for (int i = 0; i < 2515; i++) {
            ApiScenarioReport scenarioReport = new ApiScenarioReport();
            scenarioReport.setId("scenario-report-id" + i);
            scenarioReport.setProjectId(DEFAULT_PROJECT_ID);
            scenarioReport.setName("scenario-report-name" + i);
            scenarioReport.setStartTime(System.currentTimeMillis());
            scenarioReport.setCreateUser("admin");
            scenarioReport.setUpdateUser("admin");
            if (i % 50 == 0) {
                scenarioReport.setStatus(ResultStatus.SUCCESS.name());
            } else if (i % 39 == 0) {
                scenarioReport.setStatus(ResultStatus.ERROR.name());
            }
            scenarioReport.setUpdateTime(System.currentTimeMillis());
            scenarioReport.setPoolId("api-pool-id" + i);
            scenarioReport.setEnvironmentId("api-environment-id" + i);
            scenarioReport.setRunMode("api-run-mode" + i);
            scenarioReport.setTriggerMode("api-trigger-mode" + i);
            reports.add(scenarioReport);
            ApiScenarioRecord apiScenarioRecord = new ApiScenarioRecord();
            apiScenarioRecord.setApiScenarioId("scenario-record-id" + i);
            apiScenarioRecord.setApiScenarioReportId(scenarioReport.getId());
            records.add(apiScenarioRecord);
        }
        apiScenarioReportService.insertApiScenarioReport(reports, records);

        List<ApiScenarioReportStep> steps = new ArrayList<>();
        for (int i = 0; i < 1515; i++) {
            ApiScenarioReportStep apiScenarioReportStep = new ApiScenarioReportStep();
            apiScenarioReportStep.setStepId("scenario-report-step-id" + i);
            apiScenarioReportStep.setReportId("scenario-report-id" + i);
            apiScenarioReportStep.setSort(0L);
            apiScenarioReportStep.setStepType("case");
            steps.add(apiScenarioReportStep);
        }
        apiScenarioReportService.insertApiScenarioReportStep(steps);
    }

    private MvcResult responsePost(String url, Object param) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
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
        ApiReportPageRequest request = new ApiReportPageRequest();
        request.setCurrent(1);
        request.setPageSize(10);
        request.setProjectId(DEFAULT_PROJECT_ID);
        MvcResult mvcResult = responsePost(PAGE, request);
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
            put("status", List.of(ResultStatus.SUCCESS.name(), ResultStatus.ERROR.name()));
        }});
        mvcResult = responsePost(PAGE, request);
        returnPager = parseObjectFromMvcResult(mvcResult, Pager.class);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        Assertions.assertTrue(((List<ApiReport>) returnPager.getList()).size() <= request.getPageSize());
        List<ApiReport> list = JSON.parseArray(JSON.toJSONString(returnPager.getList()), ApiReport.class);
        list.forEach(apiReport -> {
            Assertions.assertTrue(apiReport.getStatus().equals(ResultStatus.SUCCESS.name()) || apiReport.getStatus().equals(ResultStatus.ERROR.name()));
        });

        //校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_REPORT_READ, PAGE, request);

        // 顺便查找一下通过率
        // 查统计数据
        List<String> apiScenarioIds = Collections.singletonList("scenario-record-id0");
        requestPostWithOk("/api/scenario/statistics", apiScenarioIds);
        Assertions.assertEquals(CalculateUtils.reportPercentage(1, 999999999), "0.01%");
        Assertions.assertEquals(CalculateUtils.reportPercentage(999999998, 999999999), "99.99%");
        Assertions.assertNotEquals(CalculateUtils.reportPercentage(499999998, 999999999), "100%");
    }

    @Override
	protected ResultActions requestGetWithOk(String url, Object... uriVariables) throws Exception {
        return mockMvc.perform(getRequestBuilder(url, uriVariables))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @Order(3)
    public void testRename() throws Exception {
        // @@请求成功
        String newName = "scenario-report-new-name";
        requestPost(RENAME + "scenario-report-id0", newName);
        ApiScenarioReport apiReport = apiScenarioReportMapper.selectByPrimaryKey("scenario-report-id0");
        Assertions.assertNotNull(apiReport);
        Assertions.assertEquals(apiReport.getName(), newName);
        mockMvc.perform(getPostRequestBuilder(RENAME + "api-report", newName))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_REPORT_UPDATE, RENAME + "scenario-report-id0", newName);
    }

    @Test
    @Order(4)
    public void testDeleted() throws Exception {
        // @@请求成功
        requestGetWithOk(DELETE + "scenario-report-id0");
        ApiScenarioReport apiReport = apiScenarioReportMapper.selectByPrimaryKey("scenario-report-id0");
        Assertions.assertNotNull(apiReport);
        Assertions.assertTrue(apiReport.getDeleted());
        mockMvc.perform(getRequestBuilder(DETAIL + "api-report"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_REPORT_DELETE, DELETE + "scenario-report-id1");
    }

    @Test
    @Order(5)
    public void testBatch() throws Exception {
        // @@请求成功
        ApiReportBatchRequest request = new ApiReportBatchRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setSelectIds(List.of("scenario-report-id3"));
        request.setExcludeIds(List.of("scenario-report-id3"));
        responsePost(BATCH_DELETE, request);
        request.setSelectIds(List.of("scenario-report-id4"));
        responsePost(BATCH_DELETE, request);
        ApiScenarioReport apiReport = apiScenarioReportMapper.selectByPrimaryKey("scenario-report-id4");
        Assertions.assertNotNull(apiReport);
        Assertions.assertTrue(apiReport.getDeleted());
        request.setSelectAll(true);
        responsePost(BATCH_DELETE, request);
        responsePost(BATCH_PARAM, request);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_REPORT_DELETE, BATCH_DELETE, request);
    }


    @Test
    @Order(6)
    public void testGet() throws Exception {
        // @@请求成功
        EnvironmentExample environmentExample = new EnvironmentExample();
        environmentExample.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andMockEqualTo(true);
        List<Environment> environments = environmentMapper.selectByExample(environmentExample);

        TestResourcePoolExample example = new TestResourcePoolExample();
        example.createCriteria().andNameEqualTo("默认资源池");
        List<TestResourcePool> testResourcePools = testResourcePoolMapper.selectByExample(example);

        List<ApiScenarioReport> reports = new ArrayList<>();
        ApiScenarioReport scenarioReport = new ApiScenarioReport();
        scenarioReport.setId("test-scenario-report-id");
        scenarioReport.setProjectId(DEFAULT_PROJECT_ID);
        scenarioReport.setName("test-scenario-report-name");
        scenarioReport.setStartTime(System.currentTimeMillis());
        scenarioReport.setCreateUser("admin");
        scenarioReport.setUpdateUser("admin");
        scenarioReport.setUpdateTime(System.currentTimeMillis());
        scenarioReport.setPoolId(testResourcePools.getFirst().getId());
        scenarioReport.setEnvironmentId(environments.getFirst().getId());
        scenarioReport.setRunMode("api-run-mode");
        scenarioReport.setTriggerMode("api-trigger-mode");
        reports.add(scenarioReport);
        ApiScenarioRecord apiScenarioRecord = new ApiScenarioRecord();
        apiScenarioRecord.setApiScenarioId("test-scenario-record-id");
        apiScenarioRecord.setApiScenarioReportId(scenarioReport.getId());
        apiScenarioReportService.insertApiScenarioReport(reports, List.of(apiScenarioRecord));
        List<ApiScenarioReportStep> steps = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            ApiScenarioReportStep apiScenarioReportStep = new ApiScenarioReportStep();
            apiScenarioReportStep.setStepId("test-scenario-report-step-id" + i);
            apiScenarioReportStep.setReportId("test-scenario-report-id");
            apiScenarioReportStep.setSort((long) i);
            if (i % 2 == 0) {
                apiScenarioReportStep.setStepType(ApiScenarioStepType.API_CASE.name());
                apiScenarioReportStep.setParentId("NONE");
            } else if (i % 3 == 0) {
                apiScenarioReportStep.setStepType(ApiScenarioStepType.IF_CONTROLLER.name());
                apiScenarioReportStep.setParentId("test-scenario-report-step-id" + (i - 1));
            } else {
                apiScenarioReportStep.setStepType(ApiScenarioStepType.API_SCENARIO.name());
                apiScenarioReportStep.setParentId("test-scenario-report-step-id" + (i - 2));
            }
            steps.add(apiScenarioReportStep);
        }
        apiScenarioReportService.insertApiScenarioReportStep(steps);
        List<ApiScenarioReportDetail> details = new ArrayList<>();
        for (int i = 1; i < 20; i++) {
            ApiScenarioReportDetail apiReportDetail = new ApiScenarioReportDetail();
            apiReportDetail.setId("test-report-detail-id" + i + 2);
            apiReportDetail.setReportId("test-scenario-report-id");
            apiReportDetail.setStepId("test-scenario-report-step-id" + i);
            apiReportDetail.setSort((long) i);
            if (i % 2 == 0) {
                apiReportDetail.setStatus(ResultStatus.SUCCESS.name());
                apiReportDetail.setResponseSize(1L);
                apiReportDetail.setRequestTime(2L);
            } else if (i % 3 == 0) {
                apiReportDetail.setStatus(null);
                apiReportDetail.setResponseSize(0L);
                apiReportDetail.setRequestTime(2L);
            } else {
                apiReportDetail.setStatus(ResultStatus.FAKE_ERROR.name());
                apiReportDetail.setResponseSize(1L);
                apiReportDetail.setRequestTime(2L);
            }
            details.add(apiReportDetail);
        }
        apiScenarioReportDetailMapper.batchInsert(details);

        //插入console 资源池 环境
        ApiScenarioReportLog apiScenarioReportLog = new ApiScenarioReportLog();
        apiScenarioReportLog.setId(IDGenerator.nextStr());
        apiScenarioReportLog.setReportId("test-scenario-report-id");
        apiScenarioReportLog.setConsole("console".getBytes());
        apiScenarioReportLogMapper.insert(apiScenarioReportLog);


        MvcResult mvcResult = this.requestGetWithOk(GET + "test-scenario-report-id")
                .andReturn();
        ApiScenarioReportDTO apiReportDTO = ApiDataUtils.parseObject(JSON.toJSONString(parseResponse(mvcResult).get("data")), ApiScenarioReportDTO.class);

        Assertions.assertNotNull(apiReportDTO);
        Assertions.assertEquals(apiReportDTO.getId(), "test-scenario-report-id");

        ApiScenarioReport scenarioReport1 = apiScenarioReportMapper.selectByPrimaryKey("test-scenario-report-id");
        scenarioReport1.setEnvironmentId(null);
        scenarioReport1.setPoolId(null);
        apiScenarioReportMapper.updateByPrimaryKeySelective(scenarioReport1);
        this.requestGetWithOk(GET + "test-scenario-report-id");
        scenarioReport1.setEnvironmentId("env_id");
        apiScenarioReportMapper.updateByPrimaryKeySelective(scenarioReport1);
        this.requestGetWithOk(GET + "test-scenario-report-id");

        //插入一个环境组
        EnvironmentGroup environmentGroup = new EnvironmentGroup();
        environmentGroup.setId("env_group_id");
        environmentGroup.setProjectId(DEFAULT_PROJECT_ID);
        environmentGroup.setName("env_group_name");
        environmentGroup.setCreateUser("admin");
        environmentGroup.setUpdateUser("admin");
        environmentGroup.setUpdateTime(System.currentTimeMillis());
        environmentGroup.setCreateTime(System.currentTimeMillis());
        environmentGroup.setPos(1L);
        environmentGroupMapper.insert(environmentGroup);
        scenarioReport1.setEnvironmentId("env_group_id");
        apiScenarioReportMapper.updateByPrimaryKeySelective(scenarioReport1);
        this.requestGetWithOk(GET + "test-scenario-report-id");
        environmentGroupMapper.deleteByPrimaryKey("env_group_id");

        mockMvc.perform(getRequestBuilder(GET + "test"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());

        mockMvc.perform(getRequestBuilder(GET + "scenario-report-id200000"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_REPORT_READ, GET + "test-scenario-report-id");
    }

    @Test
    @Order(7)
    public void testGetDetail() throws Exception {
        // @@请求成功
        List<ApiScenarioReportDetail> reports = new ArrayList<>();
        List<ApiScenarioReportDetailBlob> reportBlogs = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            ApiScenarioReportDetail apiReportDetail = new ApiScenarioReportDetail();
            apiReportDetail.setId("test-report-detail-id" + i);
            apiReportDetail.setReportId("test-scenario-report-id");
            apiReportDetail.setStepId("test-scenario-report-step-id1");
            apiReportDetail.setStatus("success");
            apiReportDetail.setResponseSize(0L);
            apiReportDetail.setRequestTime((long) i);
            apiReportDetail.setSort((long) i);
            reports.add(apiReportDetail);

            reportBlogs.add(new ApiScenarioReportDetailBlob() {{
                setId(apiReportDetail.getId());
                setReportId(apiReportDetail.getReportId());
                setContent("{\"resourceId\":\"\",\"stepId\":null,\"threadName\":\"Thread Group\",\"name\":\"HTTP Request1\",\"url\":\"https://www.baidu.com/\",\"requestSize\":195,\"startTime\":1705570589125,\"endTime\":1705570589310,\"error\":1,\"headers\":\"Connection: keep-alive\\nContent-Length: 0\\nContent-Type: application/x-www-form-urlencoded; charset=UTF-8\\nHost: www.baidu.com\\nUser-Agent: Apache-HttpClient/4.5.14 (Java/21)\\n\",\"cookies\":\"\",\"body\":\"POST https://www.baidu.com/\\n\\nPOST data:\\n\\n\\n[no cookies]\\n\",\"status\":\"ERROR\",\"method\":\"POST\",\"assertionTotal\":1,\"passAssertionsTotal\":0,\"subRequestResults\":[],\"responseResult\":{\"responseCode\":\"200\",\"responseMessage\":\"OK\",\"responseTime\":185,\"latency\":180,\"responseSize\":2559,\"headers\":\"HTTP/1.1 200 OK\\nContent-Length: 2443\\nContent-Type: text/html\\nServer: bfe\\nDate: Thu, 18 Jan 2024 09:36:29 GMT\\n\",\"body\":\"<!DOCTYPE html>\\r\\n<!--STATUS OK--><html> <head><meta http-equiv=content-type content=text/html;charset=utf-8><meta http-equiv=X-UA-Compatible content=IE=Edge><meta content=always name=referrer><link rel=stylesheet type=text/css href=https://ss1.bdstatic.com/5eN1bjq8AAUYm2zgoY3K/r/www/cache/bdorz/baidu.min.css><title>百度一下，你就知道</title></head> <body link=#0000cc> <div id=wrapper> <div id=head> <div class=head_wrapper> <div class=s_form> <div class=s_form_wrapper> <div id=lg> <img hidefocus=true src=//www.baidu.com/img/bd_logo1.png width=270 height=129> </div> <form id=form name=f action=//www.baidu.com/s class=fm> <input type=hidden name=bdorz_come value=1> <input type=hidden name=ie value=utf-8> <input type=hidden name=f value=8> <input type=hidden name=rsv_bp value=1> <input type=hidden name=rsv_idx value=1> <input type=hidden name=tn value=baidu><span class=\\\"bg s_ipt_wr\\\"><input id=kw name=wd class=s_ipt value maxlength=255 autocomplete=off autofocus=autofocus></span><span class=\\\"bg s_btn_wr\\\"><input type=submit id=su value=百度一下 class=\\\"bg s_btn\\\" autofocus></span> </form> </div> </div> <div id=u1> <a href=http://news.baidu.com name=tj_trnews class=mnav>新闻</a> <a href=https://www.hao123.com name=tj_trhao123 class=mnav>hao123</a> <a href=http://map.baidu.com name=tj_trmap class=mnav>地图</a> <a href=http://v.baidu.com name=tj_trvideo class=mnav>视频</a> <a href=http://tieba.baidu.com name=tj_trtieba class=mnav>贴吧</a> <noscript> <a href=http://www.baidu.com/bdorz/login.gif?login&amp;tpl=mn&amp;u=http%3A%2F%2Fwww.baidu.com%2f%3fbdorz_come%3d1 name=tj_login class=lb>登录</a> </noscript> <script>document.write('<a href=\\\"http://www.baidu.com/bdorz/login.gif?login&tpl=mn&u='+ encodeURIComponent(window.location.href+ (window.location.search === \\\"\\\" ? \\\"?\\\" : \\\"&\\\")+ \\\"bdorz_come=1\\\")+ '\\\" name=\\\"tj_login\\\" class=\\\"lb\\\">登录</a>');\\r\\n                </script> <a href=//www.baidu.com/more/ name=tj_briicon class=bri style=\\\"display: block;\\\">更多产品</a> </div> </div> </div> <div id=ftCon> <div id=ftConw> <p id=lh> <a href=http://home.baidu.com>关于百度</a> <a href=http://ir.baidu.com>About Baidu</a> </p> <p id=cp>&copy;2017&nbsp;Baidu&nbsp;<a href=http://www.baidu.com/duty/>使用百度前必读</a>&nbsp; <a href=http://jianyi.baidu.com/ class=cp-feedback>意见反馈</a>&nbsp;京ICP证030173号&nbsp; <img src=//www.baidu.com/img/gs.gif> </p> </div> </div> </div> </body> </html>\\r\\n\",\"contentType\":\"text/html\",\"vars\":null,\"imageUrl\":null,\"socketInitTime\":14,\"dnsLookupTime\":0,\"tcpHandshakeTime\":0,\"sslHandshakeTime\":0,\"transferStartTime\":166,\"downloadTime\":5,\"bodySize\":2443,\"headerSize\":116,\"assertions\":[{\"name\":\"JSON Assertion\",\"content\":null,\"script\":null,\"message\":\"Expected to find an object with property ['test'] in path $ but found 'java.lang.String'. This is not a json object according to the JsonProvider: 'com.jayway.jsonpath.spi.json.JsonSmartJsonProvider'.\",\"pass\":false}]},\"isSuccessful\":false,\"fakeErrorMessage\":\"\",\"fakeErrorCode\":null}\n".getBytes());

            }});
        }
        apiScenarioReportDetailMapper.batchInsert(reports);
        apiScenarioReportDetailBlobMapper.batchInsert(reportBlogs);

        MvcResult mvcResult = this.requestGetWithOk(DETAIL + "test-scenario-report-id" + "/" + "test-scenario-report-step-id1")
                .andReturn();
        List<ApiScenarioReportDTO> data = ApiDataUtils.parseArray(JSON.toJSONString(parseResponse(mvcResult).get("data")), ApiScenarioReportDTO.class);
        Assertions.assertNotNull(data);

        reports = new ArrayList<>();
        reportBlogs = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            ApiScenarioReportDetail apiReportDetail = new ApiScenarioReportDetail();
            apiReportDetail.setId("test-report-detail-id-loop" + i);
            apiReportDetail.setReportId("test-report-detail-id-loop");
            apiReportDetail.setStepId("test-report-detail-id-loop");
            apiReportDetail.setStatus("success");
            apiReportDetail.setResponseSize(0L);
            apiReportDetail.setRequestTime((long) i);
            apiReportDetail.setSort((long) i);
            reports.add(apiReportDetail);

            reportBlogs.add(new ApiScenarioReportDetailBlob() {{
                setId(apiReportDetail.getId());
                setReportId(apiReportDetail.getReportId());
                setContent("{\"resourceId\":\"\",\"stepId\":null,\"threadName\":\"Thread Group\",\"name\":\"HTTP Request1\",\"url\":\"https://www.baidu.com/\",\"requestSize\":195,\"startTime\":1705570589125,\"endTime\":1705570589310,\"error\":1,\"headers\":\"Connection: keep-alive\\nContent-Length: 0\\nContent-Type: application/x-www-form-urlencoded; charset=UTF-8\\nHost: www.baidu.com\\nUser-Agent: Apache-HttpClient/4.5.14 (Java/21)\\n\",\"cookies\":\"\",\"body\":\"POST https://www.baidu.com/\\n\\nPOST data:\\n\\n\\n[no cookies]\\n\",\"status\":\"ERROR\",\"method\":\"POST\",\"assertionTotal\":1,\"passAssertionsTotal\":0,\"subRequestResults\":[],\"responseResult\":{\"responseCode\":\"200\",\"responseMessage\":\"OK\",\"responseTime\":185,\"latency\":180,\"responseSize\":2559,\"headers\":\"HTTP/1.1 200 OK\\nContent-Length: 2443\\nContent-Type: text/html\\nServer: bfe\\nDate: Thu, 18 Jan 2024 09:36:29 GMT\\n\",\"body\":\"<!DOCTYPE html>\\r\\n<!--STATUS OK--><html> <head><meta http-equiv=content-type content=text/html;charset=utf-8><meta http-equiv=X-UA-Compatible content=IE=Edge><meta content=always name=referrer><link rel=stylesheet type=text/css href=https://ss1.bdstatic.com/5eN1bjq8AAUYm2zgoY3K/r/www/cache/bdorz/baidu.min.css><title>百度一下，你就知道</title></head> <body link=#0000cc> <div id=wrapper> <div id=head> <div class=head_wrapper> <div class=s_form> <div class=s_form_wrapper> <div id=lg> <img hidefocus=true src=//www.baidu.com/img/bd_logo1.png width=270 height=129> </div> <form id=form name=f action=//www.baidu.com/s class=fm> <input type=hidden name=bdorz_come value=1> <input type=hidden name=ie value=utf-8> <input type=hidden name=f value=8> <input type=hidden name=rsv_bp value=1> <input type=hidden name=rsv_idx value=1> <input type=hidden name=tn value=baidu><span class=\\\"bg s_ipt_wr\\\"><input id=kw name=wd class=s_ipt value maxlength=255 autocomplete=off autofocus=autofocus></span><span class=\\\"bg s_btn_wr\\\"><input type=submit id=su value=百度一下 class=\\\"bg s_btn\\\" autofocus></span> </form> </div> </div> <div id=u1> <a href=http://news.baidu.com name=tj_trnews class=mnav>新闻</a> <a href=https://www.hao123.com name=tj_trhao123 class=mnav>hao123</a> <a href=http://map.baidu.com name=tj_trmap class=mnav>地图</a> <a href=http://v.baidu.com name=tj_trvideo class=mnav>视频</a> <a href=http://tieba.baidu.com name=tj_trtieba class=mnav>贴吧</a> <noscript> <a href=http://www.baidu.com/bdorz/login.gif?login&amp;tpl=mn&amp;u=http%3A%2F%2Fwww.baidu.com%2f%3fbdorz_come%3d1 name=tj_login class=lb>登录</a> </noscript> <script>document.write('<a href=\\\"http://www.baidu.com/bdorz/login.gif?login&tpl=mn&u='+ encodeURIComponent(window.location.href+ (window.location.search === \\\"\\\" ? \\\"?\\\" : \\\"&\\\")+ \\\"bdorz_come=1\\\")+ '\\\" name=\\\"tj_login\\\" class=\\\"lb\\\">登录</a>');\\r\\n                </script> <a href=//www.baidu.com/more/ name=tj_briicon class=bri style=\\\"display: block;\\\">更多产品</a> </div> </div> </div> <div id=ftCon> <div id=ftConw> <p id=lh> <a href=http://home.baidu.com>关于百度</a> <a href=http://ir.baidu.com>About Baidu</a> </p> <p id=cp>&copy;2017&nbsp;Baidu&nbsp;<a href=http://www.baidu.com/duty/>使用百度前必读</a>&nbsp; <a href=http://jianyi.baidu.com/ class=cp-feedback>意见反馈</a>&nbsp;京ICP证030173号&nbsp; <img src=//www.baidu.com/img/gs.gif> </p> </div> </div> </div> </body> </html>\\r\\n\",\"contentType\":\"text/html\",\"vars\":null,\"imageUrl\":null,\"socketInitTime\":14,\"dnsLookupTime\":0,\"tcpHandshakeTime\":0,\"sslHandshakeTime\":0,\"transferStartTime\":166,\"downloadTime\":5,\"bodySize\":2443,\"headerSize\":116,\"assertions\":[{\"name\":\"JSON Assertion\",\"content\":null,\"script\":null,\"message\":\"Expected to find an object with property ['test'] in path $ but found 'java.lang.String'. This is not a json object according to the JsonProvider: 'com.jayway.jsonpath.spi.json.JsonSmartJsonProvider'.\",\"pass\":false}]},\"isSuccessful\":false,\"fakeErrorMessage\":\"\",\"fakeErrorCode\":null}\n".getBytes());
            }});

        }
        apiScenarioReportDetailMapper.batchInsert(reports);
        apiScenarioReportDetailBlobMapper.batchInsert(reportBlogs);

        mockMvc.perform(getRequestBuilder(DETAIL + "test-report-detail-id-loop" + "/" + "test-report-detail-id-loop_2"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(getRequestBuilder(DETAIL + "test" + "/" + "test"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(getRequestBuilder(DETAIL + "api-report-id10"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_REPORT_READ, GET + "api-report-id0");
    }

    @Test
    @Order(8)
    public void generateUrl() throws Exception {
        ApiReportShareRequest shareInfo = new ApiReportShareRequest();
        shareInfo.setReportId("test-scenario-report-id");
        shareInfo.setProjectId(DEFAULT_PROJECT_ID);
        shareInfo.setShareType(ShareInfoType.API_SHARE_REPORT.name());
        MvcResult mvcResult = responsePost("/api/report/share/gen", shareInfo);
        ShareInfoDTO shareInfoDTO = parseObjectFromMvcResult(mvcResult, ShareInfoDTO.class);
        Assertions.assertNotNull(shareInfoDTO);
        Assertions.assertNotNull(shareInfoDTO.getShareUrl());
        Assertions.assertNotNull(shareInfoDTO.getId());
        String shareId = shareInfoDTO.getId();
        MvcResult mvcResult1 = this.requestGetWithOk(BASIC + "/share/" + shareId + "/" + "test-scenario-report-id")
                .andReturn();
        ApiScenarioReportDTO apiReportDTO = ApiDataUtils.parseObject(JSON.toJSONString(parseResponse(mvcResult1).get("data")), ApiScenarioReportDTO.class);
        Assertions.assertNotNull(apiReportDTO);
        Assertions.assertEquals(apiReportDTO.getId(), "test-scenario-report-id");

        this.requestGetWithOk("/api/report/share/get/" + shareId)
                .andReturn();

        ApiScenarioReport scenarioReport = apiScenarioReportMapper.selectByPrimaryKey("test-scenario-report-id");
        scenarioReport.setDeleted(true);
        apiScenarioReportMapper.updateByPrimaryKeySelective(scenarioReport);
        this.requestGetWithOk("/api/report/share/get/" + shareId)
                .andReturn();
        scenarioReport.setDeleted(false);
        apiScenarioReportMapper.updateByPrimaryKeySelective(scenarioReport);

        mockMvc.perform(getRequestBuilder("/api/report/share/get/" + "test"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mvcResult = this.requestGetWithOk(BASIC + "/share/detail/" + shareId + "/" + "test-scenario-report-id" + "/" + "test-scenario-report-step-id1")
                .andReturn();
        List<ApiScenarioReportDetailDTO> data = ApiDataUtils.parseArray(JSON.toJSONString(parseResponse(mvcResult).get("data")), ApiScenarioReportDetailDTO.class);
        Assertions.assertNotNull(data);
        //过期时间时间戳  1702950953000  2023-12-19 09:55:53
        ShareInfo shareInfo1 = shareInfoMapper.selectByPrimaryKey(shareId);
        shareInfo1.setUpdateTime(1702950953000L);
        shareInfoMapper.updateByPrimaryKey(shareInfo1);

        mockMvc.perform(getRequestBuilder(BASIC + "/share/" + shareId + "/" + "test-scenario-report-id"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());


        shareInfo = new ApiReportShareRequest();
        shareInfo.setReportId("test-scenario-report-id");
        shareInfo.setProjectId(DEFAULT_PROJECT_ID);
        shareInfo.setShareType(ShareInfoType.API_SHARE_REPORT.name());
        mvcResult = responsePost("/api/report/share/gen", shareInfo);
        shareInfoDTO = parseObjectFromMvcResult(mvcResult, ShareInfoDTO.class);
        Assertions.assertNotNull(shareInfoDTO);
        Assertions.assertNotNull(shareInfoDTO.getShareUrl());
        Assertions.assertNotNull(shareInfoDTO.getId());
        shareId = shareInfoDTO.getId();

        this.requestGetWithOk("/api/report/share/get-share-time/" + DEFAULT_PROJECT_ID)
                .andReturn();
        ProjectApplicationExample projectApplicationExample = new ProjectApplicationExample();
        projectApplicationExample.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andTypeEqualTo(ShareInfoType.API_SHARE_REPORT.name());
        List<ProjectApplication> projectApplications = projectApplicationMapper.selectByExample(projectApplicationExample);
        if (projectApplications.isEmpty()) {
            ProjectApplication projectApplication = new ProjectApplication();
            projectApplication.setProjectId(DEFAULT_PROJECT_ID);
            projectApplication.setType(ShareInfoType.API_SHARE_REPORT.name());
            projectApplication.setTypeValue("1D");
            projectApplicationMapper.insert(projectApplication);
        }
        this.requestGetWithOk("/api/report/share/get-share-time/" + DEFAULT_PROJECT_ID)
                .andReturn();

        mvcResult1 = this.requestGetWithOk(BASIC + "/share/" + shareId + "/" + "test-scenario-report-id")
                .andReturn();
        apiReportDTO = ApiDataUtils.parseObject(JSON.toJSONString(parseResponse(mvcResult1).get("data")), ApiScenarioReportDTO.class);
        Assertions.assertNotNull(apiReportDTO);
        Assertions.assertEquals(apiReportDTO.getId(), "test-scenario-report-id");

        //项目当前设置了分享时间  并且过期
        shareInfo1 = shareInfoMapper.selectByPrimaryKey(shareId);
        shareInfo1.setUpdateTime(1702950953000L);
        shareInfoMapper.updateByPrimaryKey(shareInfo1);

        mockMvc.perform(getRequestBuilder(BASIC + "/share/" + shareId + "/" + "test-scenario-report-id"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @Order(9)
    public void testExportReport() throws Exception {
        this.requestPost(EXPORT_REPORT, null, "scenario-report-id1");
    }

    @Test
    @Order(10)
    public void testBatchExportReport() throws Exception {
        ApiReportBatchRequest request = new ApiReportBatchRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setSelectAll(true);
        this.requestPost(BATCH_EXPORT_REPORT, request);
    }

    @Test
    @Order(11)
    @Sql(scripts = {"/dml/init_scenario_task_item_test.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void getTaskStep() throws Exception {
        this.requestGet(TASK_STEP, "scenario_1");
        this.requestGet(TASK_STEP, "scenario_3");
    }

    @Test
    @Order(12)
    public void getTaskReport() throws Exception {
        this.requestGet(TASK_REPORT + "test-scenario-report-id" + "/" + "test-scenario-report-step-id1");
    }
}
