package io.metersphere.functional.controller;

import io.metersphere.functional.constants.FunctionalCaseReviewStatus;
import io.metersphere.functional.domain.CaseReviewHistory;
import io.metersphere.functional.dto.CaseReviewHistoryDTO;
import io.metersphere.functional.dto.FunctionalCaseReviewDTO;
import io.metersphere.functional.mapper.CaseReviewHistoryMapper;
import io.metersphere.functional.request.FunctionalCaseReviewListRequest;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class FunctionalCaseReviewControllerTests extends BaseTest {

    private static final String URL_CASE_REVIEW_PAGE = "/functional/case/review/page";

    private static final String URL_CASE_REVIEW_COMMENT = "/functional/case/review/comment/";

    @Resource
    private CaseReviewHistoryMapper caseReviewHistoryMapper;

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

    @Test
    @Order(2)
    public void getCaseReviewHistory() throws Exception {
        CaseReviewHistory caseReviewHistory = new CaseReviewHistory();
        caseReviewHistory.setReviewId("用例关系名称1");
        caseReviewHistory.setCaseId("gyqReviewCaseTest");
        caseReviewHistory.setCreateUser("system");
        caseReviewHistory.setStatus(FunctionalCaseReviewStatus.RE_REVIEWED.toString());
        caseReviewHistory.setId("test");
        String content = "你好评论";
        caseReviewHistory.setContent(content.getBytes());
        caseReviewHistory.setCreateTime(System.currentTimeMillis());
        caseReviewHistoryMapper.insertSelective(caseReviewHistory);
        List<CaseReviewHistoryDTO> gyqReviewCaseTest = getCaseReviewHistoryList("gyqReviewCaseTest");
        Assertions.assertTrue(StringUtils.isNotBlank(gyqReviewCaseTest.getFirst().getContentText()));
        caseReviewHistory = new CaseReviewHistory();
        caseReviewHistory.setReviewId("用例关系名称2");
        caseReviewHistory.setCaseId("gyqReviewCaseTest");
        caseReviewHistory.setCreateUser("admin");
        caseReviewHistory.setStatus(FunctionalCaseReviewStatus.RE_REVIEWED.toString());
        caseReviewHistory.setId("testNoContent");
        caseReviewHistory.setCreateTime(System.currentTimeMillis());
        caseReviewHistoryMapper.insertSelective(caseReviewHistory);
        gyqReviewCaseTest = getCaseReviewHistoryList("gyqReviewCaseTest");
        Assertions.assertTrue(gyqReviewCaseTest.size()>1);
    }

    public List<CaseReviewHistoryDTO> getCaseReviewHistoryList(String caseId) throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(URL_CASE_REVIEW_COMMENT + caseId).header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .header(SessionConstants.CURRENT_PROJECT, DEFAULT_PROJECT_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        return JSON.parseArray(JSON.toJSONString(resultHolder.getData()), CaseReviewHistoryDTO.class);

    }
}
