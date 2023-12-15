package io.metersphere.functional.controller;

import io.metersphere.functional.domain.CaseReviewFunctionalCase;
import io.metersphere.functional.domain.CaseReviewFunctionalCaseExample;
import io.metersphere.functional.mapper.CaseReviewFunctionalCaseMapper;
import io.metersphere.functional.request.*;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.dto.sdk.BaseCondition;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;

import java.nio.charset.StandardCharsets;
import java.util.*;

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

    @Resource
    private CaseReviewFunctionalCaseMapper caseReviewFunctionalCaseMapper;

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
        request.setExcludeIds(Arrays.asList("TEST_FUNCTIONAL_CASE_ID_1"));
        Map<String, Object> map = new HashMap<>();
        map.put("customs", Arrays.asList(new LinkedHashMap() {{
            put("id", "TEST_FIELD_ID");
            put("operator", "in");
            put("value", "222");
            put("type", "List");
        }}));
        request.setCombine(map);
        this.requestPost(FUNCTIONAL_CASE_LIST_URL, request);
    }


    @Test
    @Order(3)
    @Sql(scripts = {"/dml/init_review_functional_case_test.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testReviewCasePage() throws Exception {
        ReviewFunctionalCasePageRequest request = new ReviewFunctionalCasePageRequest();
        request.setReviewId("wx_review_id_1");
        request.setCurrent(1);
        request.setPageSize(10);
        Map<String, Object> map = new HashMap<>();
        map.put("customs", Arrays.asList(new LinkedHashMap() {{
            put("id", "TEST_FIELD_ID");
            put("operator", "in");
            put("value", "222");
            put("type", "List");
        }}));
        request.setCombine(map);
        request.setViewFlag(false);
        this.requestPostWithOkAndReturn(REVIEW_CASE_PAGE, request);

        request.setSort(new HashMap<>() {{
            put("createTime", "desc");
        }});
        this.requestPostWithOkAndReturn(REVIEW_CASE_PAGE, request);
    }


    @Test
    @Order(4)
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
        Map<String, Object> map = new HashMap<>();
        map.put("customs", Arrays.asList(new LinkedHashMap() {{
            put("id", "TEST_FIELD_ID");
            put("operator", "in");
            put("value", "222");
            put("type", "List");
        }}));
        BaseCondition baseCondition = new BaseCondition();
        baseCondition.setCombine(map);
        request.setCondition(baseCondition);
        this.requestPostWithOkAndReturn(BATCH_DELETE_URL, request);

    }


    @Test
    @Order(5)
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
    }

    @Test
    @Order(6)
    public void testPos() throws Exception {
        List<CaseReviewFunctionalCase> caseReviewList = getCaseReviewFunctionalCase("wx_review_id_1");
        CaseReviewFunctionalCase caseReviews = caseReviewList.get(0);
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
        caseReviews2 = caseReviewList.get(0);
        Long pos3 = caseReviews.getPos();
        Long pos4 = caseReviews2.getPos();
        Assertions.assertTrue(Objects.equals(pos, pos3));
        Assertions.assertTrue(pos2 > pos4);
        posRequest.setMoveMode("BEFORE");
        this.requestPostWithOkAndReturn(REVIEW_FUNCTIONAL_CASE_POS, posRequest);
        caseReviewList = getCaseReviewFunctionalCase("wx_review_id_1");
        caseReviews = caseReviewList.get(0);
        caseReviews2 = caseReviewList.get(1);
        Long pos5 = caseReviews.getPos();
        Long pos6 = caseReviews2.getPos();
        Assertions.assertTrue(Objects.equals(pos5, pos3));
        Assertions.assertTrue(pos6 > pos4);
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

}
