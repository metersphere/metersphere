package io.metersphere.functional.controller;

import io.metersphere.functional.dto.FunctionalMinderTreeDTO;
import io.metersphere.functional.request.FunctionalCasePageRequest;
import io.metersphere.functional.request.MinderReviewFunctionalCasePageRequest;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class FunctionalCaseMinderControllerTest extends BaseTest {


    public static final String FUNCTIONAL_CASE_LIST_URL = "/functional/mind/case/list";
    public static final String REViEW_FUNCTIONAL_CASE_LIST_URL = "/functional/mind/case/review/list";



    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_file_minder_test.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testGetPageList() throws Exception {
        FunctionalCasePageRequest request = new FunctionalCasePageRequest();
        request.setProjectId("project-case-minder-test");
        request.setCurrent(1);
        request.setPageSize(10);
        request.setSort(new HashMap<>() {{
            put("createTime", "desc");
        }});
        MvcResult mvcResultPage = this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_LIST_URL, request);
        String contentAsString = mvcResultPage.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        FunctionalMinderTreeDTO baseTreeNodes = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), FunctionalMinderTreeDTO.class);
        Assertions.assertNotNull(baseTreeNodes);
       String jsonString = JSON.toJSONString(baseTreeNodes);
        System.out.println(jsonString);

    }

    @Test
    @Order(2)
    public void testGetReviewPageList() throws Exception {
       /* Map<String, Object> map = new HashMap<>();
        map.put("customs", Arrays.asList(new LinkedHashMap() {{
            put("id", "TEST_FIELD_ID");
            put("operator", "in");
            put("value", "222");
            put("type", "List");
        }}));
        request.setCombine(map);*/
        MinderReviewFunctionalCasePageRequest request = new MinderReviewFunctionalCasePageRequest();
        request.setProjectId("project-case-minder-test");
        request.setCurrent(1);
        request.setPageSize(10);
        request.setSort(new HashMap<>() {{
            put("createTime", "desc");
        }});
        request.setReviewId("TEST_MINDER_REVIEW_ID_GYQ");
        request.setViewFlag(true);
        request.setViewResult(true);

        MvcResult mvcResultPage = this.requestPostWithOkAndReturn(REViEW_FUNCTIONAL_CASE_LIST_URL, request);
        String contentAsString = mvcResultPage.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        FunctionalMinderTreeDTO baseTreeNodes = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), FunctionalMinderTreeDTO.class);
        Assertions.assertNotNull(baseTreeNodes);
        //String jsonString = JSON.toJSONString(baseTreeNodes);
        //System.out.println(jsonString);
        request.setReviewId("TEST_MINDER_REVIEW_ID_GYQ2");
        mvcResultPage = this.requestPostWithOkAndReturn(REViEW_FUNCTIONAL_CASE_LIST_URL, request);
        contentAsString = mvcResultPage.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        baseTreeNodes = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), FunctionalMinderTreeDTO.class);
        Assertions.assertNotNull(baseTreeNodes);
        //jsonString = JSON.toJSONString(baseTreeNodes);
        //System.out.println(jsonString);
    }
}
