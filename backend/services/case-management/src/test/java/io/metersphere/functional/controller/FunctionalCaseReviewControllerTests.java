package io.metersphere.functional.controller;

import io.metersphere.functional.dto.FunctionalCaseReviewDTO;
import io.metersphere.functional.request.FunctionalCaseReviewListRequest;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.utils.Pager;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class FunctionalCaseReviewControllerTests extends BaseTest {

    private static final String URL_CASE_REVIEW_PAGE = "/functional/case/review/page";

    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_case_review_functional_case.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testGetPageList() throws Exception {
        FunctionalCaseReviewListRequest functionalCaseReviewListRequest = new FunctionalCaseReviewListRequest();
        functionalCaseReviewListRequest.setCaseId("associate_case_gyq_id");
        functionalCaseReviewListRequest.setCurrent(1);
        functionalCaseReviewListRequest.setPageSize(10);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(URL_CASE_REVIEW_PAGE, functionalCaseReviewListRequest);
        Pager<List<FunctionalCaseReviewDTO>> tableData = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                Pager.class);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(tableData.getCurrent(), functionalCaseReviewListRequest.getCurrent());

        //返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(JSON.parseArray(JSON.toJSONString(tableData.getList())).size() <= functionalCaseReviewListRequest.getPageSize());
        List<FunctionalCaseReviewDTO> fileList = JSON.parseArray(JSON.toJSONString(tableData.getList()), FunctionalCaseReviewDTO.class);
        String jsonString = JSON.toJSONString(fileList);
        System.out.println(jsonString);
    }


}
