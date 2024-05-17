package io.metersphere.plan.controller;

import io.metersphere.plan.domain.*;
import io.metersphere.plan.dto.TestPlanShareInfo;
import io.metersphere.plan.dto.request.*;
import io.metersphere.plan.dto.response.TestPlanReportPageResponse;
import io.metersphere.plan.mapper.TestPlanReportBugMapper;
import io.metersphere.plan.mapper.TestPlanReportFunctionCaseMapper;
import io.metersphere.plan.mapper.TestPlanReportMapper;
import io.metersphere.plan.mapper.TestPlanReportSummaryMapper;
import io.metersphere.plan.service.CleanupTestPlanReportServiceImpl;
import io.metersphere.plan.service.TestPlanReportService;
import io.metersphere.sdk.constants.ProjectApplicationType;
import io.metersphere.sdk.constants.ShareInfoType;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class TestPlanReportControllerTests extends BaseTest {

    private static final String LIST_PLAN_REPORT = "/test-plan/report/page";
    private static final String RENAME_PLAN_REPORT = "/test-plan/report/rename";
    private static final String DELETE_PLAN_REPORT = "/test-plan/report/delete";
    private static final String BATCH_DELETE_PLAN_REPORT = "/test-plan/report/batch-delete";
    private static final String GEN_PLAN_REPORT = "/test-plan/report/gen";
    private static final String GET_PLAN_REPORT = "/test-plan/report/get";
    private static final String EDIT_PLAN_REPORT = "/test-plan/report/edit";
    private static final String GET_PLAN_REPORT_DETAIL_BUG_PAGE = "/test-plan/report/detail/bug/page";
    private static final String GET_PLAN_REPORT_DETAIL_FUNCTIONAL_PAGE = "/test-plan/report/detail/functional/case/page";
    private static final String GEN_AND_SHARE = "/test-plan/report/share/gen";
    private static final String GET_SHARE_INFO = "/test-plan/report/share/get";
    private static final String GET_SHARE_TIME = "/test-plan/report/share/get-share-time";
    @Autowired
    private TestPlanReportMapper testPlanReportMapper;
    @Resource
    private TestPlanReportService testPlanReportService;

    private static String GEN_REPORT_ID;

    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_test_plan_report.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    void tesPagePlanReportSuccess() throws Exception {
        TestPlanReportPageRequest request = new TestPlanReportPageRequest();
        request.setProjectId("100001100001");
        request.setCurrent(1);
        request.setPageSize(10);
        request.setKeyword("1");
        MvcResult mvcResult = this.requestPostWithOkAndReturn(LIST_PLAN_REPORT, request);
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
        // 返回值中取出第一条数据, 并判断是否包含关键字default
        TestPlanReportPageResponse report = JSON.parseArray(JSON.toJSONString(pageData.getList()), TestPlanReportPageResponse.class).get(0);
        Assertions.assertTrue(StringUtils.contains(report.getName(), request.getKeyword()));
        // 覆盖排序, 及数据为空
        request.setSort(Map.of("tpr.create_time", "asc"));
        request.setKeyword("oasis");
        this.requestPost(LIST_PLAN_REPORT, request);
    }

    @Test
    @Order(2)
    void testPagePlanReportError() throws Exception {
        // 必填参数有误
        TestPlanReportPageRequest request = new TestPlanReportPageRequest();
        request.setCurrent(1);
        request.setPageSize(10);
        this.requestPost(LIST_PLAN_REPORT, request, status().isBadRequest());
        // 页码有误
        request.setProjectId("100001100001");
        request.setCurrent(0);
        request.setPageSize(10);
        this.requestPost(LIST_PLAN_REPORT, request, status().isBadRequest());
        // 页数有误
        request.setCurrent(1);
        request.setPageSize(1);
        this.requestPost(LIST_PLAN_REPORT, request, status().isBadRequest());
    }

    @Test
    @Order(3)
    void testRenamePlanReportSuccess() throws Exception {
        this.requestPostWithOk(RENAME_PLAN_REPORT + "/test-plan-report-id-1", "oasis");
    }

    @Test
    @Order(4)
    void testRenamePlanReportError() throws Exception {
        TestPlanReportEditRequest request = new TestPlanReportEditRequest();
        request.setId("test-plan-report-id-x");
        request.setName("oasis");
        request.setProjectId("100001100001");
        this.requestPost(RENAME_PLAN_REPORT, request, status().is5xxServerError());
    }

    @Test
    @Order(5)
    void testSharePlanReport() throws Exception {
        TestPlanReportShareRequest shareRequest = new TestPlanReportShareRequest();
        shareRequest.setReportId("test-plan-report-id-1");
        shareRequest.setProjectId("100001100001");
        shareRequest.setShareType(ShareInfoType.TEST_PLAN_SHARE_REPORT.name());
        this.requestPost(GEN_AND_SHARE, shareRequest);
        shareRequest.setLang(Locale.SIMPLIFIED_CHINESE.getLanguage());
        MvcResult mvcResult = this.requestPost(GEN_AND_SHARE, shareRequest).andReturn();
        String sortData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder sortHolder = JSON.parseObject(sortData, ResultHolder.class);
        TestPlanShareInfo shareInfo = JSON.parseObject(JSON.toJSONString(sortHolder.getData()), TestPlanShareInfo.class);
        Assertions.assertNotNull(shareInfo);
        this.requestGet(GET_SHARE_INFO + "/" + shareInfo.getId());
    }

    @Test
    @Order(6)
    void testGetShareInfo() throws Exception {
        // 报告被删除
        this.requestGet(GET_SHARE_INFO + "/share-1");
        // 分享链接未找到
        this.requestGet(GET_SHARE_INFO + "/share-2", status().is5xxServerError());
    }

    @Test
    @Order(7)
    void testGetShareTime() throws Exception {
        this.requestGet(GET_SHARE_TIME + "/100001100001");
        this.requestGet(GET_SHARE_TIME + "/100001100002");
    }

    @Test
    @Order(8)
    void testDeletePlanReport() throws Exception {
        TestPlanReportDeleteRequest request = new TestPlanReportDeleteRequest();
        request.setId("test-plan-report-id-1");
        request.setProjectId("100001100001");
        this.requestGet(DELETE_PLAN_REPORT + "/test-plan-report-id-1");
    }

    @Test
    @Order(9)
    void testBatchDeletePlanReport() throws Exception {
        TestPlanReportBatchRequest request = new TestPlanReportBatchRequest();
        request.setProjectId("100001100001");
        // 勾选部分, 并删除
        request.setSelectAll(false);
        request.setSelectIds(List.of("test-plan-report-id-2"));
        this.requestPostWithOk(BATCH_DELETE_PLAN_REPORT, request);
        // 全选并排除所有, 为空
        request.setSelectAll(true);
        request.setExcludeIds(List.of("test-plan-report-id-3", "test-plan-report-id-4"));
        this.requestPostWithOk(BATCH_DELETE_PLAN_REPORT, request);
        // 全选不排除
        request.setExcludeIds(null);
        this.requestPostWithOk(BATCH_DELETE_PLAN_REPORT, request);
    }

    @Test
    @Order(10)
    @Sql(scripts = {"/dml/init_test_plan_report_gen.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    void testGenReportError() throws Exception {
        TestPlanReportGenRequest genRequest = new TestPlanReportGenRequest();
        genRequest.setProjectId("100001100001");
        genRequest.setTestPlanId("plan_id_for_gen_report-x");
        this.requestPost(GEN_PLAN_REPORT, genRequest, status().is5xxServerError());
    }

    @Test
    @Order(11)
    void testGenReportSuccess() throws Exception {
        TestPlanReportGenRequest genRequest = new TestPlanReportGenRequest();
        genRequest.setProjectId("100001100001");
        genRequest.setTestPlanId("plan_id_for_gen_report_1");
        this.requestPost(GEN_PLAN_REPORT, genRequest);
        genRequest.setTestPlanId("plan_id_for_gen_report");
        MvcResult mvcResult = this.requestPostAndReturn(GEN_PLAN_REPORT, genRequest);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        //返回请求正常
        Assertions.assertNotNull(resultHolder);
        TestPlanReport report = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), TestPlanReport.class);
        GEN_REPORT_ID = report.getId();
    }

    @Test
    @Order(12)
    void testGetReportSuccess() throws Exception {
        this.requestGet(GET_PLAN_REPORT + "/" + GEN_REPORT_ID);
    }

    @Test
    @Order(13)
    void testPageReportDetailBugSuccess() throws Exception {
        TestPlanReportDetailPageRequest request = new TestPlanReportDetailPageRequest();
        request.setReportId(GEN_REPORT_ID);
        request.setCurrent(1);
        request.setPageSize(10);
        this.requestPostWithOk(GET_PLAN_REPORT_DETAIL_BUG_PAGE, request);
        request.setSort(Map.of("num", "asc"));
        this.requestPostWithOk(GET_PLAN_REPORT_DETAIL_BUG_PAGE, request);
    }

    @Test
    @Order(14)
    void testPageReportDetailFunctionalCaseSuccess() throws Exception {
        TestPlanReportDetailPageRequest request = new TestPlanReportDetailPageRequest();
        request.setReportId(GEN_REPORT_ID);
        request.setCurrent(1);
        request.setPageSize(10);
        this.requestPostWithOk(GET_PLAN_REPORT_DETAIL_FUNCTIONAL_PAGE, request);
        request.setSort(Map.of("num", "asc"));
        this.requestPostWithOk(GET_PLAN_REPORT_DETAIL_FUNCTIONAL_PAGE, request);
    }

    @Test
    @Order(15)
    void testEditReportDetail() throws Exception {
        TestPlanReportDetailEditRequest request = new TestPlanReportDetailEditRequest();
        request.setId(GEN_REPORT_ID);
        request.setSummary("This is a summary for report detail");
        this.requestPostWithOk(EDIT_PLAN_REPORT, request);
    }

    @Resource
    private TestPlanReportSummaryMapper testPlanReportSummaryMapper;
    @Resource
    private TestPlanReportBugMapper testPlanReportBugMapper;
    @Resource
    private TestPlanReportFunctionCaseMapper testPlanReportFunctionCaseMapper;

    @Test
    @Order(99)
    void cleanReport() {
        TestPlanReportExample reportExample = new TestPlanReportExample();
        reportExample.createCriteria().andProjectIdEqualTo("100001100001");
        List<TestPlanReport> reports = testPlanReportMapper.selectByExample(reportExample);
        List<String> testPlanReportIdList = reports.stream().map(TestPlanReport::getId).toList();

        //测试清除方法
        CleanupTestPlanReportServiceImpl cleanupTestPlanReportService = CommonBeanFactory.getBean(CleanupTestPlanReportServiceImpl.class);
        cleanupTestPlanReportService.cleanReport(new HashMap<>() {{
            this.put(ProjectApplicationType.TEST_PLAN.TEST_PLAN_CLEAN_REPORT.name(), "3M");
        }}, DEFAULT_PROJECT_ID);
        //清除所有数据
        testPlanReportService.cleanAndDeleteReport(testPlanReportIdList);

        TestPlanReportSummaryExample summaryExample = new TestPlanReportSummaryExample();
        summaryExample.createCriteria().andTestPlanReportIdIn(testPlanReportIdList);

        TestPlanReportFunctionCaseExample testPlanReportFunctionCaseExample = new TestPlanReportFunctionCaseExample();
        testPlanReportFunctionCaseExample.createCriteria().andTestPlanReportIdIn(testPlanReportIdList);

        TestPlanReportBugExample testPlanReportBugExample = new TestPlanReportBugExample();
        testPlanReportBugExample.createCriteria().andTestPlanReportIdIn(testPlanReportIdList);

        Assertions.assertEquals(testPlanReportSummaryMapper.countByExample(summaryExample), 0);
        Assertions.assertEquals(testPlanReportFunctionCaseMapper.countByExample(testPlanReportFunctionCaseExample), 0);
        Assertions.assertEquals(testPlanReportBugMapper.countByExample(testPlanReportBugExample), 0);
    }
}
