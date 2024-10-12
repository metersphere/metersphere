package io.metersphere.functional.controller;

import io.metersphere.functional.constants.CaseReviewPassRule;
import io.metersphere.functional.constants.FunctionalCaseReviewStatus;
import io.metersphere.functional.domain.*;
import io.metersphere.functional.dto.ReviewFunctionalCaseDTO;
import io.metersphere.functional.dto.ReviewerAndStatusDTO;
import io.metersphere.functional.mapper.CaseReviewFunctionalCaseMapper;
import io.metersphere.functional.mapper.CaseReviewHistoryMapper;
import io.metersphere.functional.request.*;
import io.metersphere.functional.service.CaseReviewFunctionalCaseService;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.dto.CombineCondition;
import io.metersphere.sdk.dto.CombineSearch;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.sdk.dto.BaseCondition;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class CaseReviewFunctionalCaseControllerTests extends BaseTest {

    public static final String GET_CASE_IDS = "/case/review/detail/get-ids/";

    public static final String FUNCTIONAL_CASE_LIST_URL = "/functional/case/page";

    public static final String REVIEW_CASE_PAGE = "/case/review/detail/page";
    public static final String BATCH_DELETE_URL = "/case/review/detail/batch/disassociate";
    public static final String FUNCTIONAL_CASE_ADD_URL = "/functional/case/add";

    public static final String REVIEW_FUNCTIONAL_CASE_POS = "/case/review/detail/edit/pos";
    public static final String BATCH_EDIT_REVIEWERS = "/case/review/detail/batch/edit/reviewers";

    public static final String REVIEW_FUNCTIONAL_CASE_BATCH_REVIEW = "/case/review/detail/batch/review";

    public static final String REVIEW_FUNCTIONAL_CASE_MIND_REVIEW = "/case/review/detail/mind/multiple/review";


    public static final String URL_MODULE_TREE = "/case/review/detail/tree/";

    public static final String REVIEW_FUNCTIONAL_CASE_MODULE_COUNT = "/case/review/detail/module/count";

    public static final String REVIEW_FUNCTIONAL_CASE_REVIEWER_STATUS = "/case/review/detail/reviewer/status/";


    public static final String GET_CASE_REVIEWER_LIST = "/case/review/detail/reviewer/list";

    public static final String GET_CASE_REVIEWER_AND_STATUS = "/case/review/detail/reviewer/status/total/";


    @Resource
    private CaseReviewFunctionalCaseMapper caseReviewFunctionalCaseMapper;
    @Resource
    private CaseReviewFunctionalCaseService caseReviewFunctionalCaseService;
    @Resource
    private CaseReviewHistoryMapper caseReviewHistoryMapper;


    @Test
    @Order(1)
    public void testGetCaseIds() throws Exception {
        this.requestGet(GET_CASE_IDS + "test_review_id");
    }


    @Test
    @Order(2)
    public void testPage() throws Exception {
        FunctionalCasePageRequest request = new FunctionalCasePageRequest();
        request.setProjectId("100001100001");
        request.setCurrent(1);
        request.setPageSize(10);
        request.setReviewId("wx_review_id_1");
        request.setCombineSearch(getCustomCombineSearch());
        this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_LIST_URL, request);
    }

    private CombineSearch getCustomCombineSearch() {
        CombineSearch combineSearch = new CombineSearch();
        CombineCondition condition = new CombineCondition();
        condition.setCustomField(true);
        condition.setName("TEST_FIELD_ID");
        condition.setOperator(CombineCondition.CombineConditionOperator.IN.name());
        condition.setValue(List.of("222"));
        combineSearch.setConditions(List.of(condition));
        return combineSearch;
    }


    @Test
    @Order(3)
    @Sql(scripts = {"/dml/init_review_functional_case_test.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testReviewCasePage() throws Exception {
        ReviewFunctionalCasePageRequest request = new ReviewFunctionalCasePageRequest();
        request.setCurrent(1);
        request.setPageSize(10);
        request.setProjectId("wx_test_project");

        request.setReviewId("wx_review_id_5");
        request.setViewStatusFlag(true);
        this.requestPostWithOkAndReturn(REVIEW_CASE_PAGE, request);
        request.setReviewId("wx_review_id_1");
        this.requestPostWithOkAndReturn(REVIEW_CASE_PAGE, request);

        request.setReviewId("wx_review_id_1");
        request.setCombineSearch(getCustomCombineSearch());
        request.setViewStatusFlag(true);
        this.requestPostWithOkAndReturn(REVIEW_CASE_PAGE, request);
        this.requestPostWithOkAndReturn(REVIEW_FUNCTIONAL_CASE_MODULE_COUNT, request);
        request.setViewStatusFlag(false);
        this.requestPostWithOkAndReturn(REVIEW_CASE_PAGE, request);

        request.setSort(new HashMap<>() {{
            put("createTime", "desc");
        }});
        MvcResult mvcResultPage = this.requestPostWithOkAndReturn(REVIEW_CASE_PAGE, request);
        Pager<List<ReviewFunctionalCaseDTO>> tableData = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(mvcResultPage.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                Pager.class);
        MvcResult moduleCountMvcResult = this.requestPostWithOkAndReturn(REVIEW_FUNCTIONAL_CASE_MODULE_COUNT, request);
        Map<String, Integer> moduleCount = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(moduleCountMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                Map.class);

        //如果没有数据，则返回的模块节点也不应该有数据
        boolean moduleHaveResource = false;
        for (int countByModuleId : moduleCount.values()) {
            if (countByModuleId > 0) {
                moduleHaveResource = true;
                break;
            }
        }
        if (tableData.getTotal() > 0) {
            Assertions.assertTrue(moduleHaveResource);
        }
        Assertions.assertTrue(moduleCount.containsKey("all"));
    }

    @Test
    @Order(4)
    public void emptyDataTest() throws Exception {
        List<BaseTreeNode> treeNodeDefaults = this.getCaseReviewModuleTreeNode("wx_test_project", "wx_review_id_5");
        String jsonStringD = JSON.toJSONString(treeNodeDefaults);
        System.out.println(jsonStringD);
        List<BaseTreeNode> treeNodes = this.getCaseReviewModuleTreeNode("wx_test_project", "wx_review_id_2");
        String jsonString = JSON.toJSONString(treeNodes);
        System.out.println(jsonString);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(treeNodes));
        ReviewFunctionalCasePageRequest request = new ReviewFunctionalCasePageRequest();
        request.setReviewId("wx_review_id_2");
        request.setProjectId("wx_test_project");
        request.setCurrent(1);
        request.setPageSize(10);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(REVIEW_FUNCTIONAL_CASE_MODULE_COUNT, request);
        Map<String, Integer> moduleCountResult = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                Map.class);
        System.out.println(JSON.toJSONString(moduleCountResult));

        MvcResult mvcResultPage = this.requestPostWithOkAndReturn(REVIEW_CASE_PAGE, request);
        Pager<List<ReviewFunctionalCaseDTO>> tableData = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(mvcResultPage.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                Pager.class);
        System.out.println(JSON.toJSONString(tableData.getList()));
    }


    @Test
    @Order(5)
    public void testBatchDisassociate() throws Exception {
        BaseReviewCaseBatchRequest request = new BaseReviewCaseBatchRequest();
        request.setReviewId("wx_review_id_1");
        request.setSelectAll(false);
        this.requestPostWithOkAndReturn(BATCH_DELETE_URL, request);
        request.setSelectIds(Arrays.asList("wx_test_2"));
        this.requestPostWithOkAndReturn(BATCH_DELETE_URL, request);
        request.setSelectIds(new ArrayList<>());
        request.setSelectAll(true);
        request.setExcludeIds(Arrays.asList("TEST_FUNCTIONAL_CASE_ID_1"));
        BaseCondition baseCondition = new BaseCondition();
        baseCondition.setCombineSearch(getCustomCombineSearch());
        request.setCondition(baseCondition);
        this.requestPostWithOkAndReturn(BATCH_DELETE_URL, request);

    }


    @Test
    @Order(6)
    public void testCaseReviewAddCase() throws Exception {
        //新增
        FunctionalCaseAddRequest request = creatFunctionalCase();
        LinkedMultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        List<MockMultipartFile> files = new ArrayList<>();
        paramMap.add("request", JSON.toJSONString(request));
        paramMap.add("files", files);
        MvcResult mvcResult = this.requestMultipartWithOkAndReturn(FUNCTIONAL_CASE_ADD_URL, paramMap);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);

        request.setReviewId("wx_review_id_4");
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        paramMap.add("files", files);
        this.requestMultipartWithOkAndReturn(FUNCTIONAL_CASE_ADD_URL, paramMap);
    }

    @Test
    @Order(7)
    public void testPos() throws Exception {
        List<CaseReviewFunctionalCase> caseReviewList = getCaseReviewFunctionalCase("wx_review_id_1");
        CaseReviewFunctionalCase caseReviews = caseReviewList.getFirst();
        CaseReviewFunctionalCase caseReviews2 = caseReviewList.get(1);
        Long pos = caseReviews.getPos();
        Long pos2 = caseReviews2.getPos();
        CaseReviewFunctionalCasePosRequest posRequest = new CaseReviewFunctionalCasePosRequest();
        posRequest.setProjectId("wx_test_project");
        posRequest.setTargetId(caseReviews.getId());
        posRequest.setMoveId(caseReviews2.getId());
        posRequest.setMoveMode("AFTER");
        posRequest.setReviewId("wx_review_id_1");
        this.requestPostWithOkAndReturn(REVIEW_FUNCTIONAL_CASE_POS, posRequest);
        caseReviewList = getCaseReviewFunctionalCase("wx_review_id_1");
        caseReviews = caseReviewList.get(1);
        caseReviews2 = caseReviewList.getFirst();
        Long pos3 = caseReviews.getPos();
        Long pos4 = caseReviews2.getPos();
        Assertions.assertTrue(Objects.equals(pos, pos3));
        Assertions.assertTrue(pos2 > pos4);
        posRequest.setMoveMode("BEFORE");
        this.requestPostWithOkAndReturn(REVIEW_FUNCTIONAL_CASE_POS, posRequest);
        caseReviewList = getCaseReviewFunctionalCase("wx_review_id_1");
        caseReviews = caseReviewList.getFirst();
        caseReviews2 = caseReviewList.get(1);
        Long pos5 = caseReviews.getPos();
        Long pos6 = caseReviews2.getPos();
        Assertions.assertTrue(Objects.equals(pos5, pos3));
        Assertions.assertTrue(pos6 > pos4);
    }


    @Test
    @Order(8)
    public void testBatchReview() throws Exception {

        BatchReviewFunctionalCaseRequest request = new BatchReviewFunctionalCaseRequest();
        request.setReviewId("wx_review_id_3");
        request.setReviewPassRule(CaseReviewPassRule.MULTIPLE.toString());
        request.setStatus(FunctionalCaseReviewStatus.PASS.toString());
        request.setSelectAll(true);
        request.setContent("测试批量评审通过");
        this.requestPostWithOk(REVIEW_FUNCTIONAL_CASE_BATCH_REVIEW, request);

        request = new BatchReviewFunctionalCaseRequest();
        request.setReviewId("wx_review_id_2");
        request.setReviewPassRule(CaseReviewPassRule.MULTIPLE.toString());
        request.setStatus(FunctionalCaseReviewStatus.UN_PASS.toString());
        request.setSelectAll(true);
        request.setContent("测试批量评审失败");
        this.requestPostWithOk(REVIEW_FUNCTIONAL_CASE_BATCH_REVIEW, request);

        request = new BatchReviewFunctionalCaseRequest();
        request.setReviewId("wx_review_id_1");
        request.setReviewPassRule(CaseReviewPassRule.SINGLE.toString());
        request.setStatus(FunctionalCaseReviewStatus.UN_PASS.toString());
        request.setSelectAll(true);
        request.setContent("测试批量评审重新评审");
        this.requestPostWithOk(REVIEW_FUNCTIONAL_CASE_BATCH_REVIEW, request);

        request = new BatchReviewFunctionalCaseRequest();
        request.setReviewId("wx_review_id_1");
        request.setReviewPassRule(CaseReviewPassRule.SINGLE.toString());
        request.setStatus(FunctionalCaseReviewStatus.RE_REVIEWED.toString());
        request.setSelectAll(true);
        request.setContent("测试批量评审重新评审");
        this.requestPostWithOk(REVIEW_FUNCTIONAL_CASE_BATCH_REVIEW, request);
        try {
            caseReviewFunctionalCaseService.batchReview(request, "GGG");
        } catch (Exception e) {
            Assertions.assertNotNull(e);
        }

        request = new BatchReviewFunctionalCaseRequest();
        request.setReviewId("wx_review_id_4");
        request.setReviewPassRule(CaseReviewPassRule.MULTIPLE.toString());
        request.setStatus(FunctionalCaseReviewStatus.UNDER_REVIEWED.toString());
        request.setSelectAll(true);
        request.setContent("测试批量评审人");
        caseReviewFunctionalCaseService.batchReview(request, "666");

        request = new BatchReviewFunctionalCaseRequest();
        request.setReviewId("wx_review_id_4");
        request.setReviewPassRule(CaseReviewPassRule.MULTIPLE.toString());
        request.setStatus(FunctionalCaseReviewStatus.UNDER_REVIEWED.toString());
        request.setSelectAll(true);
        request.setContent("测试批量评审人");
        caseReviewFunctionalCaseService.batchReview(request, "GGG");

        request = new BatchReviewFunctionalCaseRequest();
        request.setReviewId("wx_review_id_4");
        request.setReviewPassRule(CaseReviewPassRule.MULTIPLE.toString());
        request.setStatus(FunctionalCaseReviewStatus.UN_PASS.toString());
        request.setSelectAll(true);
        request.setContent("测试批量评审人");
        caseReviewFunctionalCaseService.batchReview(request, "123");

        request = new BatchReviewFunctionalCaseRequest();
        request.setReviewId("wx_review_id_1");
        request.setReviewPassRule(CaseReviewPassRule.MULTIPLE.toString());
        request.setStatus(FunctionalCaseReviewStatus.UN_PASS.toString());
        request.setSelectAll(true);
        List<String> excludeIds = new ArrayList<>();
        excludeIds.add("gyq_test_4");
        request.setExcludeIds(excludeIds);
        request.setContent("测试批量评审不通过");
        this.requestPostWithOk(REVIEW_FUNCTIONAL_CASE_BATCH_REVIEW, request);

        request = new BatchReviewFunctionalCaseRequest();
        request.setReviewId("wx_review_id_1");
        request.setReviewPassRule(CaseReviewPassRule.SINGLE.toString());
        request.setStatus(FunctionalCaseReviewStatus.PASS.toString());
        request.setSelectAll(false);
        List<String> ids = new ArrayList<>();
        ids.add("gyq_test_3");
        request.setSelectIds(ids);
        this.requestPostWithOk(REVIEW_FUNCTIONAL_CASE_BATCH_REVIEW, request);


        request = new BatchReviewFunctionalCaseRequest();
        request.setReviewId("wx_review_id_1");
        request.setReviewPassRule(CaseReviewPassRule.SINGLE.toString());
        request.setStatus(FunctionalCaseReviewStatus.UN_PASS.toString());
        request.setContent("hhh");
        request.setSelectAll(false);
        ids = new ArrayList<>();
        ids.add("gyq_test_3");
        request.setSelectIds(ids);
        caseReviewFunctionalCaseService.batchReview(request, "multiple_review_admin");


        request = new BatchReviewFunctionalCaseRequest();
        request.setReviewId("wx_review_id_1");
        request.setReviewPassRule(CaseReviewPassRule.SINGLE.toString());
        request.setStatus(FunctionalCaseReviewStatus.UNDER_REVIEWED.toString());
        request.setSelectAll(false);
        ids = new ArrayList<>();
        ids.add("gyq_test_3");
        request.setSelectIds(ids);
        caseReviewFunctionalCaseService.batchReview(request, "admin");


        request = new BatchReviewFunctionalCaseRequest();
        request.setReviewId("wx_review_id_1");
        request.setReviewPassRule(CaseReviewPassRule.MULTIPLE.toString());
        request.setStatus(FunctionalCaseReviewStatus.PASS.toString());
        request.setSelectAll(true);
        request.setNotifier("gyq;admin");
        request.setContent("测试批量评审通过");
        this.requestPostWithOk(REVIEW_FUNCTIONAL_CASE_BATCH_REVIEW, request);


        request = new BatchReviewFunctionalCaseRequest();
        request.setReviewId("wx_review_id_1");
        request.setReviewPassRule(CaseReviewPassRule.MULTIPLE.toString());
        request.setStatus(FunctionalCaseReviewStatus.PASS.toString());
        request.setSelectAll(false);
        ids = new ArrayList<>();
        ids.add(" gyq_test_5");
        request.setSelectIds(ids);
        request.setContent("测试批量评审通过");
        this.requestPostWithOk(REVIEW_FUNCTIONAL_CASE_BATCH_REVIEW, request);
        CaseReviewFunctionalCase caseReviewFunctionalCase = caseReviewFunctionalCaseMapper.selectByPrimaryKey("gyq_test_5");

        request = new BatchReviewFunctionalCaseRequest();
        request.setReviewId("wx_review_id_1");
        request.setReviewPassRule(CaseReviewPassRule.MULTIPLE.toString());
        request.setStatus(FunctionalCaseReviewStatus.UN_PASS.toString());
        request.setContent("hhh");
        request.setSelectAll(false);
        ids = new ArrayList<>();
        ids.add(" gyq_test_5");
        request.setSelectIds(ids);
        request.setContent("测试批量评审通过");
        caseReviewFunctionalCaseService.batchReview(request, "multiple_review_admin");
        CaseReviewFunctionalCase caseReviewFunctionalCase1 = caseReviewFunctionalCaseMapper.selectByPrimaryKey("gyq_test_5");
        Assertions.assertTrue(StringUtils.equalsIgnoreCase(caseReviewFunctionalCase.getStatus(), caseReviewFunctionalCase1.getStatus()));


        request = new BatchReviewFunctionalCaseRequest();
        request.setReviewId("wx_review_id_4");
        request.setReviewPassRule(CaseReviewPassRule.MULTIPLE.toString());
        request.setStatus(FunctionalCaseReviewStatus.PASS.toString());
        request.setSelectAll(false);
        request.setSelectIds(List.of("wx_test_10"));
        caseReviewFunctionalCaseService.batchReview(request, "admin");
        CaseReviewHistoryExample caseReviewHistoryExample = new CaseReviewHistoryExample();
        caseReviewHistoryExample.createCriteria().andCaseIdEqualTo("wx_case_id_2").andReviewIdEqualTo("wx_review_id_4").andAbandonedEqualTo(false).andDeletedEqualTo(false);
        List<CaseReviewHistory> caseReviewHistories = caseReviewHistoryMapper.selectByExample(caseReviewHistoryExample);
        Assertions.assertEquals(3,caseReviewHistories.size());

        request = new BatchReviewFunctionalCaseRequest();
        request.setReviewId("wx_review_id_4");
        request.setReviewPassRule(CaseReviewPassRule.MULTIPLE.toString());
        request.setStatus(FunctionalCaseReviewStatus.PASS.toString());
        request.setSelectAll(false);
        request.setSelectIds(List.of("wx_test_10"));
        caseReviewFunctionalCaseService.batchReview(request, "123");
        caseReviewHistories = caseReviewHistoryMapper.selectByExample(caseReviewHistoryExample);
        Assertions.assertEquals(4,caseReviewHistories.size());

        request = new BatchReviewFunctionalCaseRequest();
        request.setReviewId("wx_review_id_4");
        request.setReviewPassRule(CaseReviewPassRule.MULTIPLE.toString());
        request.setStatus(FunctionalCaseReviewStatus.PASS.toString());
        request.setSelectAll(false);
        request.setSelectIds(List.of("wx_test_10"));
        caseReviewFunctionalCaseService.batchReview(request, "123");
        caseReviewHistories = caseReviewHistoryMapper.selectByExample(caseReviewHistoryExample);
        Assertions.assertEquals(5,caseReviewHistories.size());

    }

    @Test
    @Order(9)
    public void testMindReview() throws Exception {

        MindReviewFunctionalCaseRequest request = new MindReviewFunctionalCaseRequest();
        request.setReviewId("wx_review_id_3");
        request.setCaseId("wx_test_5");
        request.setStatus(FunctionalCaseReviewStatus.PASS.toString());
        request.setContent("测试批量评审通过");
        this.requestPost(REVIEW_FUNCTIONAL_CASE_MIND_REVIEW, request).andExpect(status().is5xxServerError());

        request = new MindReviewFunctionalCaseRequest();
        request.setReviewId("wx_review_id_2");
        request.setCaseId("gyq_case_id_9");
        request.setStatus(FunctionalCaseReviewStatus.UN_PASS.toString());
        request.setContent("测试批量评审失败");
        this.requestPostWithOk(REVIEW_FUNCTIONAL_CASE_MIND_REVIEW, request);


        request = new MindReviewFunctionalCaseRequest();
        request.setReviewId("wx_review_id_4");
        request.setCaseId("wx_case_id_4");
        request.setStatus(FunctionalCaseReviewStatus.UNDER_REVIEWED.toString());
        request.setContent("测试批量评审人");
        try {
            caseReviewFunctionalCaseService.mindReview(request, "666");
        } catch (Exception e){
            LogUtils.error(e.getMessage());
        }

        request = new MindReviewFunctionalCaseRequest();
        request.setReviewId("wx_review_id_4");
        request.setCaseId("wx_case_id_2");
        request.setStatus(FunctionalCaseReviewStatus.UNDER_REVIEWED.toString());
        request.setContent("测试批量评审人");
        caseReviewFunctionalCaseService.mindReview(request, "123");

        request = new MindReviewFunctionalCaseRequest();
        request.setReviewId("wx_review_id_4");
        request.setCaseId("wx_case_id_2");
        request.setStatus(FunctionalCaseReviewStatus.UN_PASS.toString());
        request.setContent("测试批量评审人");
        caseReviewFunctionalCaseService.mindReview(request, "admin");


        request = new MindReviewFunctionalCaseRequest();
        request.setReviewId("wx_review_id_1");
        request.setCaseId("gyq_case_id_5");
        request.setStatus(FunctionalCaseReviewStatus.PASS.toString());
        request.setNotifier("gyq;admin");
        request.setContent("测试批量评审通过");
        this.requestPostWithOk(REVIEW_FUNCTIONAL_CASE_MIND_REVIEW, request);
        CaseReviewFunctionalCase caseReviewFunctionalCase = caseReviewFunctionalCaseMapper.selectByPrimaryKey("gyq_test_5");


        request = new MindReviewFunctionalCaseRequest();
        request.setReviewId("wx_review_id_1");
        request.setStatus(FunctionalCaseReviewStatus.UN_PASS.toString());
        request.setContent("hhh");
        request.setCaseId("gyq_case_id_5");
        request.setContent("测试批量评审通过");
        caseReviewFunctionalCaseService.mindReview(request, "multiple_review_admin");
        CaseReviewFunctionalCase caseReviewFunctionalCase1 = caseReviewFunctionalCaseMapper.selectByPrimaryKey("gyq_test_5");
        Assertions.assertFalse(StringUtils.equalsIgnoreCase(caseReviewFunctionalCase.getStatus(), caseReviewFunctionalCase1.getStatus()));


        request = new MindReviewFunctionalCaseRequest();
        request.setReviewId("wx_review_id_4");
        request.setStatus(FunctionalCaseReviewStatus.PASS.toString());
        request.setCaseId("wx_case_id_2");
        caseReviewFunctionalCaseService.mindReview(request, "admin");
        CaseReviewHistoryExample caseReviewHistoryExample = new CaseReviewHistoryExample();
        caseReviewHistoryExample.createCriteria().andCaseIdEqualTo("wx_case_id_2").andReviewIdEqualTo("wx_review_id_4").andAbandonedEqualTo(false).andDeletedEqualTo(false);
        List<CaseReviewHistory> caseReviewHistories = caseReviewHistoryMapper.selectByExample(caseReviewHistoryExample);
        Assertions.assertEquals(8,caseReviewHistories.size());

        request = new MindReviewFunctionalCaseRequest();
        request.setReviewId("wx_review_id_4");
        request.setStatus(FunctionalCaseReviewStatus.PASS.toString());
        request.setCaseId("wx_case_id_2");
        caseReviewFunctionalCaseService.mindReview(request, "123");
        caseReviewHistories = caseReviewHistoryMapper.selectByExample(caseReviewHistoryExample);
        Assertions.assertEquals(9,caseReviewHistories.size());

        request = new MindReviewFunctionalCaseRequest();
        request.setReviewId("wx_review_id_4");
        request.setStatus(FunctionalCaseReviewStatus.PASS.toString());
        request.setCaseId("wx_case_id_2");
        caseReviewFunctionalCaseService.mindReview(request, "123");
        caseReviewHistories = caseReviewHistoryMapper.selectByExample(caseReviewHistoryExample);
        Assertions.assertEquals(10,caseReviewHistories.size());

        request = new MindReviewFunctionalCaseRequest();
        request.setReviewId("wx_review_id_4");
        request.setStatus(FunctionalCaseReviewStatus.PASS.toString());
        request.setCaseId("wx_case_id_2");
        request.setUserId("123");
        caseReviewFunctionalCaseService.mindReview(request, "123");
        caseReviewHistories = caseReviewHistoryMapper.selectByExample(caseReviewHistoryExample);
        Assertions.assertEquals(11,caseReviewHistories.size());

    }

    @Test
    @Order(10)
    public void testBatchReviewFalse() throws Exception {
        BatchReviewFunctionalCaseRequest request = new BatchReviewFunctionalCaseRequest();
        request.setReviewId("wx_review_id_1");
        request.setReviewPassRule(CaseReviewPassRule.SINGLE.toString());
        request.setStatus(FunctionalCaseReviewStatus.UN_PASS.toString());
        request.setSelectAll(true);
        this.requestPost(REVIEW_FUNCTIONAL_CASE_BATCH_REVIEW, request).andExpect(status().is5xxServerError());
    }


    private List<CaseReviewFunctionalCase> getCaseReviewFunctionalCase(String reviewId) {
        CaseReviewFunctionalCaseExample caseReviewFunctionalCaseExample = new CaseReviewFunctionalCaseExample();
        caseReviewFunctionalCaseExample.createCriteria().andReviewIdEqualTo(reviewId);
        caseReviewFunctionalCaseExample.setOrderByClause("pos asc");
        return caseReviewFunctionalCaseMapper.selectByExample(caseReviewFunctionalCaseExample);
    }


    private FunctionalCaseAddRequest creatFunctionalCase() {
        FunctionalCaseAddRequest functionalCaseAddRequest = new FunctionalCaseAddRequest();
        functionalCaseAddRequest.setProjectId(DEFAULT_PROJECT_ID);
        functionalCaseAddRequest.setTemplateId("default_template_id");
        functionalCaseAddRequest.setName("测试评审详情创建用例");
        functionalCaseAddRequest.setCaseEditType("STEP");
        functionalCaseAddRequest.setModuleId("TEST_MODULE_ID");
        functionalCaseAddRequest.setReviewId("wx_review_id_1");
        return functionalCaseAddRequest;
    }


    @Test
    @Order(11)
    public void testBatchEditReviewers() throws Exception {
        BatchEditReviewerRequest request = new BatchEditReviewerRequest();
        //更新评审人
        request.setAppend(false);
        request.setReviewerId(List.of("wx1"));
        request.setReviewId("wx_review_id_1");
        //增加覆盖率
        this.requestPost(BATCH_EDIT_REVIEWERS, request);

        //测试选中id进行更新  单人评审情况下
        request.setSelectIds(List.of("wx_test_3", "wx_test_4"));
        this.requestPostWithOkAndReturn(BATCH_EDIT_REVIEWERS, request);
        //多人评审情况下
        request.setReviewId("wx_review_id_3");
        request.setSelectIds(List.of("wx_test_5", "wx_test_6"));
        this.requestPostWithOkAndReturn(BATCH_EDIT_REVIEWERS, request);
        //多人 更新不变
        request.setReviewId("wx_review_id_4");
        request.setReviewerId(List.of("admin"));
        request.setSelectIds(List.of("wx_test_8", "wx_test_9", "wx_test_10"));
        this.requestPostWithOkAndReturn(BATCH_EDIT_REVIEWERS, request);

        request.setReviewId("wx_review_id_6");
        request.setReviewerId(List.of("wx11"));
        request.setSelectIds(List.of("wx_test_12"));
        this.requestPostWithOkAndReturn(BATCH_EDIT_REVIEWERS, request);

        request.setReviewId("wx_review_id_8");
        request.setReviewerId(List.of("admin"));
        request.setSelectIds(List.of("wx_test_13"));
        this.requestPostWithOkAndReturn(BATCH_EDIT_REVIEWERS, request);

        request.setReviewId("wx_review_id_9");
        request.setReviewerId(List.of("admin"));
        request.setSelectIds(List.of("wx_test_14"));
        this.requestPostWithOkAndReturn(BATCH_EDIT_REVIEWERS, request);

        //追加评审人
        request.setAppend(true);
        request.setReviewId("wx_review_id_4");
        request.setReviewerId(List.of("wx11"));
        request.setSelectIds(List.of("wx_test_10"));
        this.requestPostWithOkAndReturn(BATCH_EDIT_REVIEWERS, request);
        //覆盖率
        request.setReviewId("wx_review_id_4");
        request.setReviewerId(List.of("wx11"));
        request.setSelectIds(List.of("wx_test_10"));
        this.requestPostWithOkAndReturn(BATCH_EDIT_REVIEWERS, request);
    }

    @Test
    @Order(12)
    public void getUserStatus() throws Exception {
        List<OptionDTO> optionDTOS = getOptionDTOS("wx_review_id_1", "gyq_case_id_5");
        Assertions.assertTrue(CollectionUtils.isNotEmpty(optionDTOS));
        optionDTOS = getOptionDTOS("wx_review_id_1_NONE", "gyq_case_id_5");
        Assertions.assertTrue(CollectionUtils.isEmpty(optionDTOS));
    }

    @Test
    @Order(13)
    public void getModuleCount() throws Exception {
        ReviewFunctionalCasePageRequest request = new ReviewFunctionalCasePageRequest();
        request.setReviewId("wx_review_id_1");
        request.setCurrent(1);
        request.setPageSize(10);
        request.setProjectId("wx_test_project");
        MvcResult moduleCountMvcResult = this.requestPostWithOkAndReturn(REVIEW_FUNCTIONAL_CASE_MODULE_COUNT, request);
        Map<String, Integer> moduleCount = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(moduleCountMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                Map.class);

        Assertions.assertTrue(moduleCount.containsKey("TEST_MODULE_ID_COUNT_three"));

    }

    @Test
    @Order(14)
    public void getReviewerList() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(GET_CASE_REVIEWER_LIST + "/wx_review_id_1/gyq_case_id_5").header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String returnData = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        List<CaseReviewFunctionalCaseUser> optionDTOS = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), CaseReviewFunctionalCaseUser.class);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(optionDTOS));
    }

    @Test
    @Order(15)
    public void getReviewerWidthTotalList() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(GET_CASE_REVIEWER_AND_STATUS + "/wx_review_id_1/gyq_case_id_5").header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String returnData = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        ReviewerAndStatusDTO reviewerAndStatusDTO = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), ReviewerAndStatusDTO.class);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(reviewerAndStatusDTO.getReviewerStatus()));

        BatchReviewFunctionalCaseRequest request = new BatchReviewFunctionalCaseRequest();
        request.setReviewId("wx_review_id_1");
        request.setReviewPassRule(CaseReviewPassRule.MULTIPLE.toString());
        request.setStatus(FunctionalCaseReviewStatus.RE_REVIEWED.toString());
        request.setSelectAll(false);
        request.setSelectIds(List.of("gyq_test_5"));
        request.setContent("测试批量评审通过");
        this.requestPostWithOk(REVIEW_FUNCTIONAL_CASE_BATCH_REVIEW, request);
        result = mockMvc.perform(MockMvcRequestBuilders.get(GET_CASE_REVIEWER_AND_STATUS + "/wx_review_id_1/gyq_case_id_5").header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        returnData = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        reviewerAndStatusDTO = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), ReviewerAndStatusDTO.class);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(reviewerAndStatusDTO.getReviewerStatus()));
        result = mockMvc.perform(MockMvcRequestBuilders.get(GET_CASE_REVIEWER_AND_STATUS + "/wx_review_id_5/gyq_case_id_d").header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        returnData = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        reviewerAndStatusDTO = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), ReviewerAndStatusDTO.class);
    }



    private List<OptionDTO> getOptionDTOS(String reviewId, String caseId) throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(REVIEW_FUNCTIONAL_CASE_REVIEWER_STATUS + "/" + reviewId + "/" + caseId).header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String returnData = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        List<OptionDTO> optionDTOS = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), OptionDTO.class);
        return optionDTOS;
    }


    private List<BaseTreeNode> getCaseReviewModuleTreeNode(String projectId, String reviewId) throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(URL_MODULE_TREE + "/" + reviewId).header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .header(SessionConstants.CURRENT_PROJECT, projectId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String returnData = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        return JSON.parseArray(JSON.toJSONString(resultHolder.getData()), BaseTreeNode.class);
    }
}
