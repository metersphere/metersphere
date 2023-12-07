package io.metersphere.functional.controller;

import io.metersphere.functional.request.FunctionalCasePageRequest;
import io.metersphere.functional.request.ReviewFunctionalCasePageRequest;
import io.metersphere.system.base.BaseTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class CaseReviewFunctionalCaseControllerTests extends BaseTest {

    public static final String GET_CASE_IDS = "/case/review/detail/get-ids/";
    public static final String FUNCTIONAL_CASE_LIST_URL = "/functional/case/page";

    public static final String REVIEW_CASE_PAGE = "/case/review/detail/page";

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

}
