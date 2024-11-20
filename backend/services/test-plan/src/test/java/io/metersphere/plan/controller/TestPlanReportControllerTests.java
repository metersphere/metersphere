package io.metersphere.plan.controller;

import io.metersphere.api.domain.ApiReportRelateTaskExample;
import io.metersphere.api.mapper.ApiReportRelateTaskMapper;
import io.metersphere.plan.constants.CollectionQueryType;
import io.metersphere.plan.domain.*;
import io.metersphere.plan.dto.TestPlanShareInfo;
import io.metersphere.plan.dto.request.*;
import io.metersphere.plan.dto.response.TestPlanReportPageResponse;
import io.metersphere.plan.enums.TestPlanReportAttachmentSourceType;
import io.metersphere.plan.mapper.*;
import io.metersphere.plan.service.CleanupTestPlanReportServiceImpl;
import io.metersphere.plan.service.TestPlanReportService;
import io.metersphere.project.domain.ProjectApplicationExample;
import io.metersphere.project.mapper.ProjectApplicationMapper;
import io.metersphere.sdk.constants.ProjectApplicationType;
import io.metersphere.sdk.constants.ShareInfoType;
import io.metersphere.sdk.constants.TaskTriggerMode;
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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class TestPlanReportControllerTests extends BaseTest {

    private static final String LIST_PLAN_REPORT = "/test-plan/report/page";
    private static final String RENAME_PLAN_REPORT = "/test-plan/report/rename";
    private static final String DELETE_PLAN_REPORT = "/test-plan/report/delete";
    private static final String BATCH_DELETE_PLAN_REPORT = "/test-plan/report/batch-delete";
    private static final String MANUAL_GEN_PLAN_REPORT = "/test-plan/report/manual-gen";
    private static final String GET_MANUAL_PLAN_REPORT_LAYOUT = "/test-plan/report/get-layout";
    private static final String AUTO_GEN_PLAN_REPORT = "/test-plan/report/auto-gen";
    private static final String GET_PLAN_REPORT = "/test-plan/report/get";
    private static final String GET_PLAN_TASK_RESULT = "/test-plan/report/get-task";
    private static final String GET_PLAN_RESULT = "/test-plan/report/get-result";
    private static final String EDIT_PLAN_REPORT_AND_UPLOAD_PIC = "/test-plan/report/upload/md/file";
    private static final String EDIT_PLAN_REPORT = "/test-plan/report/detail/edit";
    private static final String GET_PLAN_REPORT_DETAIL_BUG_PAGE = "/test-plan/report/detail/bug/page";
    private static final String GET_PLAN_REPORT_DETAIL_FUNCTIONAL_PAGE = "/test-plan/report/detail/functional/case/page";
    private static final String GET_PLAN_REPORT_DETAIL_FUNCTIONAL_RESULT = "/test-plan/report/detail/functional/case/step";
    private static final String GET_PLAN_REPORT_DETAIL_API_PAGE = "/test-plan/report/detail/api/case/page";
    private static final String GET_PLAN_REPORT_DETAIL_SCENARIO_PAGE = "/test-plan/report/detail/scenario/case/page";
    private static final String GET_PLAN_REPORT_DETAIL_PLAN_PAGE = "/test-plan/report/detail/plan/report/page";
    private static final String GET_PLAN_REPORT_DETAIL_COLLECTION_PAGE = "/test-plan/report/detail/{1}/collection/page";
    private static final String GEN_AND_SHARE = "/test-plan/report/share/gen";
    private static final String GET_SHARE_INFO = "/test-plan/report/share/get";
    private static final String GET_SHARE_REPORT_LAYOUT = "/test-plan/report/share/get-layout";
    private static final String GET_SHARE_TIME = "/test-plan/report/share/get-share-time";
    private static final String GET_SHARE_REPORT = "/test-plan/report/share/get/detail";
    private static final String GET_SHARE_REPORT_BUG_LIST = "/test-plan/report/share/detail/bug/page";
    private static final String GET_SHARE_REPORT_FUNCTIONAL_LIST = "/test-plan/report/share/detail/functional/case/page";
    private static final String GET_SHARE_REPORT_API_LIST = "/test-plan/report/share/detail/api/case/page";
    private static final String GET_SHARE_REPORT_SCENARIO_LIST = "/test-plan/report/share/detail/scenario/case/page";
    private static final String GET_SHARE_REPORT_PLAN_LIST = "/test-plan/report/share/detail/plan/report/page";
    private static final String GET_SHARE_REPORT_API_REPORT_LIST = "/test-plan/report/share/detail/api-report";
    private static final String GET_SHARE_REPORT_SCENARIO_REPORT_LIST = "/test-plan/report/share/detail/scenario-report";
    private static final String GET_SHARE_REPORT_DETAIL_FUNCTIONAL_RESULT = "/test-plan/report/share/detail/functional/case/step";
    private static final String EXPORT_REPORT = "/test-plan/report/export/{0}";
    private static final String BATCH_EXPORT_REPORT = "/test-plan/report/batch-export";

    @Autowired
    private TestPlanReportMapper testPlanReportMapper;
    @Resource
    private TestPlanReportService testPlanReportService;
    @Resource
    private ProjectApplicationMapper projectApplicationMapper;
    @Resource
    private TestPlanReportAttachmentMapper reportAttachmentMapper;

    private static String GEN_REPORT_ID;
    private static String GEN_SHARE_ID;
	@Autowired
	private ApiReportRelateTaskMapper apiReportRelateTaskMapper;

    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_test_plan_report.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    void tesPagePlanReportSuccess() throws Exception {
        TestPlanReportPageRequest request = new TestPlanReportPageRequest();
        request.setProjectId("100001100001");
        request.setCurrent(1);
        request.setPageSize(10);
        request.initKeyword("1");
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
        TestPlanReportPageResponse report = JSON.parseArray(JSON.toJSONString(pageData.getList()), TestPlanReportPageResponse.class).getFirst();
        Assertions.assertTrue(StringUtils.contains(report.getName(), request.getKeyword()));
        // 覆盖排序, 及数据为空
        request.setSort(Map.of("tpr.create_time", "asc"));
        request.initKeyword("oasis");
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
        GEN_SHARE_ID = shareInfo.getId();
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
    void testGetShareReportTableList() throws Exception {
        TestPlanShareReportDetailRequest request = new TestPlanShareReportDetailRequest();
        request.setCurrent(1);
        request.setPageSize(10);
        request.setReportId("test-plan-report-id-1");
        request.setShareId(GEN_SHARE_ID);
        // 获取分享的报告的列表明细
        this.requestPostWithOk(GET_SHARE_REPORT_BUG_LIST, request);
        this.requestPostWithOk(GET_SHARE_REPORT_FUNCTIONAL_LIST, request);
        this.requestPostWithOk(GET_SHARE_REPORT_API_LIST, request);
        this.requestPostWithOk(GET_SHARE_REPORT_SCENARIO_LIST, request);
        this.requestPostWithOk(GET_SHARE_REPORT_PLAN_LIST, request);
        request.setSort(Map.of("num", "asc"));
        this.requestPostWithOk(GET_SHARE_REPORT_BUG_LIST, request);
        this.requestPostWithOk(GET_SHARE_REPORT_FUNCTIONAL_LIST, request);
        this.requestPostWithOk(GET_SHARE_REPORT_API_LIST, request);
        this.requestPostWithOk(GET_SHARE_REPORT_SCENARIO_LIST, request);

        mockMvc.perform(getRequestBuilder(GET_SHARE_REPORT_API_REPORT_LIST + "/" + GEN_SHARE_ID + "/" + "test"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());

        mockMvc.perform(getRequestBuilder(GET_SHARE_REPORT_API_REPORT_LIST + "/get/" + GEN_SHARE_ID + "/" + "test" + "/111"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(getRequestBuilder(GET_SHARE_REPORT_SCENARIO_REPORT_LIST + "/" + GEN_SHARE_ID + "/" + "test"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());

        mockMvc.perform(getRequestBuilder(GET_SHARE_REPORT_SCENARIO_REPORT_LIST + "/get/" + GEN_SHARE_ID + "/" + "test" + "/111"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        this.requestGetWithOk(GET_SHARE_REPORT_DETAIL_FUNCTIONAL_RESULT + "/" + GEN_SHARE_ID + "/execute-his-1");
    }

    @Test
    @Order(9)
    void testGetShareReport() throws Exception {
        // 获取分享的报告
        this.requestGet(GET_SHARE_REPORT + "/" + GEN_SHARE_ID + "/test-plan-report-id-1");
        ProjectApplicationExample example = new ProjectApplicationExample();
        example.createCriteria().andProjectIdEqualTo("100001100001").andTypeEqualTo("TEST_PLAN_SHARE_REPORT");
        projectApplicationMapper.deleteByExample(example);
        this.requestGet(GET_SHARE_REPORT + "/" + GEN_SHARE_ID + "/test-plan-report-id-1");
        TestPlanReport report = new TestPlanReport();
        report.setId("test-plan-report-id-1");
        report.setDeleted(true);
        testPlanReportMapper.updateByPrimaryKeySelective(report);
        this.requestGet(GET_SHARE_REPORT + "/" + GEN_SHARE_ID + "/test-plan-report-id-1");
    }

    @Test
    @Order(10)
    void testDeletePlanReport() throws Exception {
        TestPlanReportDeleteRequest request = new TestPlanReportDeleteRequest();
        request.setId("test-plan-report-id-1");
        request.setProjectId("100001100001");
        this.requestGet(DELETE_PLAN_REPORT + "/test-plan-report-id-1");
    }

    @Test
    @Order(11)
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
    @Order(12)
    @Sql(scripts = {"/dml/init_test_plan_report_gen.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    void testGenReportError() throws Exception {
        TestPlanReportGenRequest genRequest = new TestPlanReportGenRequest();
        genRequest.setProjectId("100001100001");
        genRequest.setTestPlanId("plan_id_for_gen_report-x");
        this.requestPost(AUTO_GEN_PLAN_REPORT, genRequest, status().is5xxServerError());
    }

    @Test
    @Order(13)
    void testGenReportSuccess() throws Exception {
        TestPlanReportGenRequest genRequest = new TestPlanReportGenRequest();
        genRequest.setProjectId("100001100001");
        genRequest.setTestPlanId("plan_id_for_gen_report_1");
        genRequest.setTriggerMode(TaskTriggerMode.MANUAL.name());
        this.requestPost(AUTO_GEN_PLAN_REPORT, genRequest);
        genRequest.setTestPlanId("plan_id_for_gen_report");
        this.requestPost(AUTO_GEN_PLAN_REPORT, genRequest);
        GEN_REPORT_ID = getGenReportId();
    }

    @Test
    @Order(14)
    void testGetReportSuccess() throws Exception {
        this.requestGet(GET_PLAN_REPORT + "/" + GEN_REPORT_ID);
        this.requestGet(GET_PLAN_REPORT + "/" + "test-plan-report-id-5");
        this.requestGet(GET_PLAN_TASK_RESULT + "/" + "task-id-1");
        this.requestGet(GET_PLAN_TASK_RESULT + "/" + "task-id-2");
        this.requestGet(GET_PLAN_RESULT + "/" + "task-id-1");
        this.requestGet(GET_PLAN_RESULT + "/" + "task-id-2");
        // 为了不影响后续报告的清理
        cleanDefaultTaskReportRelate();
    }

    @Test
    @Order(15)
    void testPageReportDetailBugSuccess() throws Exception {
        TestPlanReportDetailPageRequest request = new TestPlanReportDetailPageRequest();
        request.setCurrent(1);
        request.setPageSize(10);
        request.setReportId(GEN_REPORT_ID);
        this.requestPostWithOk(GET_PLAN_REPORT_DETAIL_BUG_PAGE, request);
        request.setSort(Map.of("num", "asc"));
        this.requestPostWithOk(GET_PLAN_REPORT_DETAIL_BUG_PAGE, request);
    }

    @Test
    @Order(16)
    void testPageReportDetailFunctionalCaseSuccess() throws Exception {
        TestPlanReportDetailPageRequest request = new TestPlanReportDetailPageRequest();
        request.setCurrent(1);
        request.setPageSize(10);
        request.setReportId(GEN_REPORT_ID);
        this.requestPostWithOk(GET_PLAN_REPORT_DETAIL_FUNCTIONAL_PAGE, request);
        this.requestPostWithOk(GET_PLAN_REPORT_DETAIL_COLLECTION_PAGE, request, CollectionQueryType.FUNCTIONAL);
        this.requestPostWithOk(GET_PLAN_REPORT_DETAIL_API_PAGE, request);
        this.requestPostWithOk(GET_PLAN_REPORT_DETAIL_COLLECTION_PAGE, request, CollectionQueryType.API);
        this.requestPostWithOk(GET_PLAN_REPORT_DETAIL_SCENARIO_PAGE, request);
        this.requestPostWithOk(GET_PLAN_REPORT_DETAIL_COLLECTION_PAGE, request, CollectionQueryType.SCENARIO);
        this.requestPostWithOk(GET_PLAN_REPORT_DETAIL_PLAN_PAGE, request);
        request.setSort(Map.of("num", "asc"));
        request.setStartPager(false);
        this.requestPostWithOk(GET_PLAN_REPORT_DETAIL_FUNCTIONAL_PAGE, request);
        this.requestPostWithOk(GET_PLAN_REPORT_DETAIL_COLLECTION_PAGE, request, CollectionQueryType.FUNCTIONAL);
        this.requestPostWithOk(GET_PLAN_REPORT_DETAIL_API_PAGE, request);
        this.requestPostWithOk(GET_PLAN_REPORT_DETAIL_COLLECTION_PAGE, request, CollectionQueryType.API);
        this.requestPostWithOk(GET_PLAN_REPORT_DETAIL_SCENARIO_PAGE, request);
        this.requestPostWithOk(GET_PLAN_REPORT_DETAIL_COLLECTION_PAGE, request, CollectionQueryType.SCENARIO);
    }

    @Test
    @Order(17)
    void testEditReportDetail() throws Exception {
        TestPlanReportDetailEditRequest request = new TestPlanReportDetailEditRequest();
        request.setId(GEN_REPORT_ID);
        request.setComponentValue("This is a summary for report detail");
        this.requestPostWithOk(EDIT_PLAN_REPORT, request);
        request.setRichTextTmpFileIds(List.of("rich-text-file-id-for-report"));
        this.requestPost(EDIT_PLAN_REPORT, request, status().is5xxServerError());
        generateReportMdFile();
        this.requestPostWithOk(EDIT_PLAN_REPORT, request);
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.APPLICATION_OCTET_STREAM_VALUE, "aa".getBytes());
        MvcResult mvcResult = this.requestUploadFileWithOkAndReturn(EDIT_PLAN_REPORT_AND_UPLOAD_PIC, file);
        String sortData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(sortData, ResultHolder.class);
        String fileId = resultHolder.getData().toString();
        request.setRichTextTmpFileIds(List.of(fileId));
        this.requestPostWithOk(EDIT_PLAN_REPORT, request);
    }

    @Test
    @Order(18)
    void testSummaryGroupReport() {
        testPlanReportService.summaryGroupReport("test-plan-report-id-5");
        testPlanReportService.summaryGroupReport("test-plan-report-id-7");
        testPlanReportService.summaryGroupReport("test-plan-report-id-9");
    }

    @Test
    @Order(19)
    void testGetReportFunctionalExecResult() throws Exception {
        this.requestGet(GET_PLAN_REPORT_DETAIL_FUNCTIONAL_RESULT + "/execute-his-1");
        this.requestGet(GET_PLAN_REPORT_DETAIL_FUNCTIONAL_RESULT + "/execute-his-2");
    }

    @Test
    @Order(20)
    void testGenReportByManual() throws Exception {
        TestPlanReportComponentSaveRequest component = new TestPlanReportComponentSaveRequest();
        component.setName("component-for-test");
        component.setType("RICH_TEXT");
        component.setLabel("component-for-test");
        component.setValue("Val for test!");
        component.setPos(1L);
        TestPlanReportManualRequest genRequest = new TestPlanReportManualRequest();
        genRequest.setProjectId("100001100001");
        genRequest.setTestPlanId("plan_id_for_gen_report");
        genRequest.setTriggerMode(TaskTriggerMode.MANUAL.name());
        genRequest.setReportName("oasis");
        genRequest.setComponents(List.of(component));
        this.requestPost(MANUAL_GEN_PLAN_REPORT, genRequest);
        genRequest.setComponents(null);
        this.requestPost(MANUAL_GEN_PLAN_REPORT, genRequest);
    }

    @Test
    @Order(21)
    void testShareOrEditReportByManual() throws Exception {
        TestPlanReportShareRequest shareRequest = new TestPlanReportShareRequest();
        shareRequest.setReportId(getManualGenPlanReportId());
        shareRequest.setProjectId("100001100001");
        shareRequest.setShareType(ShareInfoType.TEST_PLAN_SHARE_REPORT.name());
        shareRequest.setLang(Locale.SIMPLIFIED_CHINESE.getLanguage());
        MvcResult mvcResult = this.requestPost(GEN_AND_SHARE, shareRequest).andReturn();
        String sortData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder sortHolder = JSON.parseObject(sortData, ResultHolder.class);
        TestPlanShareInfo shareInfo = JSON.parseObject(JSON.toJSONString(sortHolder.getData()), TestPlanShareInfo.class);
        Assertions.assertNotNull(shareInfo);
        this.requestGet(GET_SHARE_INFO + "/" + shareInfo.getId());
        this.requestGet(GET_SHARE_REPORT_LAYOUT + "/" + shareInfo.getId() + "/" + getManualGenPlanReportId());

        this.requestGet(GET_MANUAL_PLAN_REPORT_LAYOUT + "/" + getManualGenPlanReportId());
        // 编辑手动生成的报告
        TestPlanReportDetailEditRequest request = new TestPlanReportDetailEditRequest();
        request.setId(getManualGenPlanReportId());
        request.setComponentId("component-for-test");
        request.setComponentValue("This is a summary for report detail");
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
		assert cleanupTestPlanReportService != null;
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

    /**
     * 生成报告附件的测试数据
     */
    private void generateReportMdFile() {
        TestPlanReportAttachment attachment = new TestPlanReportAttachment();
        attachment.setId(UUID.randomUUID().toString());
        attachment.setTestPlanReportId(GEN_REPORT_ID);
        attachment.setFileId("rich-text-file-id-for-report");
        attachment.setFileName("test-file");
        attachment.setSize(111L);
        attachment.setSource(TestPlanReportAttachmentSourceType.RICH_TEXT.name());
        attachment.setCreateUser("admin");
        attachment.setCreateTime(System.currentTimeMillis());
        reportAttachmentMapper.insert(attachment);
    }

    /**
     * 获取生成的报告ID
     *
     * @return 报告ID
     */
    private String getGenReportId() {
        TestPlanReportExample example = new TestPlanReportExample();
        example.createCriteria().andTestPlanIdEqualTo("plan_id_for_gen_report");
        return testPlanReportMapper.selectByExample(example).getFirst().getId();
    }

    private String getManualGenPlanReportId() {
        TestPlanReportExample example = new TestPlanReportExample();
        example.createCriteria().andTestPlanIdEqualTo("plan_id_for_gen_report").andDefaultLayoutEqualTo(false);
        return testPlanReportMapper.selectByExample(example).getFirst().getId();
    }

    @Test
    @Order(25)
    void testExportReport() throws Exception {
        this.requestPost(EXPORT_REPORT,null,"test-plan-report-id-1");
        this.requestPost(EXPORT_REPORT,null,"test-plan-report-id-3");
    }

    @Test
    @Order(26)
    void testBatchExportReport() throws Exception {
        TestPlanReportBatchRequest request = new TestPlanReportBatchRequest();
        request.setProjectId("100001100001");
        request.setSelectAll(true);
        this.requestPost(BATCH_EXPORT_REPORT, request);
    }

    private void cleanDefaultTaskReportRelate() {
        ApiReportRelateTaskExample example = new ApiReportRelateTaskExample();
        example.createCriteria().andTaskResourceIdEqualTo("task-id-1");
        apiReportRelateTaskMapper.deleteByExample(example);
    }
}
