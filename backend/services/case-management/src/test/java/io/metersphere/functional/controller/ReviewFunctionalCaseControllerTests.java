package io.metersphere.functional.controller;

import io.metersphere.functional.constants.CaseReviewPassRule;
import io.metersphere.functional.constants.CaseReviewStatus;
import io.metersphere.functional.constants.FunctionalCaseReviewStatus;
import io.metersphere.functional.domain.*;
import io.metersphere.functional.dto.CaseReviewHistoryDTO;
import io.metersphere.functional.mapper.CaseReviewFunctionalCaseMapper;
import io.metersphere.functional.mapper.CaseReviewHistoryMapper;
import io.metersphere.functional.mapper.CaseReviewMapper;
import io.metersphere.functional.request.BaseAssociateCaseRequest;
import io.metersphere.functional.request.CaseReviewRequest;
import io.metersphere.functional.request.ReviewFunctionalCaseRequest;
import io.metersphere.project.domain.Notification;
import io.metersphere.project.domain.NotificationExample;
import io.metersphere.project.mapper.NotificationMapper;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.notice.constants.NoticeConstants;
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
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ReviewFunctionalCaseControllerTests extends BaseTest {

    private static final String projectId = "project-review-case-test";

    private static final String SAVE_REVIEW = "/review/functional/case/save";
    private static final String ADD_CASE_REVIEW = "/case/review/add";
    private static final String REVIEW_LIST = "/review/functional/case/get/list/";

    @Resource
    private CaseReviewMapper caseReviewMapper;
    @Resource
    private CaseReviewHistoryMapper caseReviewHistoryMapper;
    @Resource
    private NotificationMapper notificationMapper;
    @Resource
    private CaseReviewFunctionalCaseMapper caseReviewFunctionalCaseMapper;


    @Test
    @Order(0)
    @Sql(scripts = {"/dml/init_review_case.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void saveReviewSuccess() throws Exception {
        //单人评审通过
        List<String> caseIds = new ArrayList<>();
        caseIds.add("gyqReviewCaseTest");
        caseIds.add("gyqReviewCaseTestOne");
        List<String> reviewers = new ArrayList<>();
        reviewers.add("admin");
        reviewers.add("default-project-member-user-gyq");
        List<CaseReview> caseReviews = addReview("创建用例评审1", caseIds, reviewers);
        String reviewId = caseReviews.get(0).getId();
        ReviewFunctionalCaseRequest reviewFunctionalCaseRequest = new ReviewFunctionalCaseRequest();
        reviewFunctionalCaseRequest.setReviewId(reviewId);
        reviewFunctionalCaseRequest.setCaseId("gyqReviewCaseTest");
        reviewFunctionalCaseRequest.setProjectId(projectId);
        reviewFunctionalCaseRequest.setStatus(FunctionalCaseReviewStatus.PASS.toString());
        reviewFunctionalCaseRequest.setContent("通过了");
        reviewFunctionalCaseRequest.setNotifier("default-project-member-user-gyq-2;");
        reviewFunctionalCaseRequest.setReviewPassRule(CaseReviewPassRule.SINGLE.toString());
        this.requestPostWithOk(SAVE_REVIEW, reviewFunctionalCaseRequest);
        CaseReviewHistoryExample caseReviewHistoryExample = new CaseReviewHistoryExample();
        caseReviewHistoryExample.createCriteria().andReviewIdEqualTo(reviewId).andCaseIdEqualTo("gyqReviewCaseTest");
        List<CaseReviewHistory> caseReviewHistories = caseReviewHistoryMapper.selectByExample(caseReviewHistoryExample);
        Assertions.assertEquals(1, caseReviewHistories.size());
        CaseReviewFunctionalCaseExample  caseReviewFunctionalCaseExample = new CaseReviewFunctionalCaseExample();
        caseReviewFunctionalCaseExample.createCriteria().andReviewIdEqualTo(reviewId).andCaseIdEqualTo("gyqReviewCaseTest");
        List<CaseReviewFunctionalCase> caseReviewFunctionalCases = caseReviewFunctionalCaseMapper.selectByExample(caseReviewFunctionalCaseExample);
        Assertions.assertTrue(StringUtils.equalsIgnoreCase(caseReviewFunctionalCases.get(0).getStatus(),FunctionalCaseReviewStatus.PASS.toString()));
        List<CaseReview> caseReviews1 = getCaseReviews("创建用例评审1");
        Assertions.assertTrue(StringUtils.equals(caseReviews1.get(0).getStatus(), CaseReviewStatus.UNDERWAY.toString()));
        //单人评审不通过
        reviewFunctionalCaseRequest = new ReviewFunctionalCaseRequest();
        reviewFunctionalCaseRequest.setReviewId(reviewId);
        reviewFunctionalCaseRequest.setCaseId("gyqReviewCaseTestOne");
        reviewFunctionalCaseRequest.setProjectId(projectId);
        reviewFunctionalCaseRequest.setStatus(FunctionalCaseReviewStatus.UN_PASS.toString());
        reviewFunctionalCaseRequest.setContent("不通过");
        reviewFunctionalCaseRequest.setNotifier("default-project-member-user-gyq-2;");
        reviewFunctionalCaseRequest.setReviewPassRule(CaseReviewPassRule.SINGLE.toString());
        this.requestPostWithOk(SAVE_REVIEW, reviewFunctionalCaseRequest);
        caseReviewHistoryExample = new CaseReviewHistoryExample();
        caseReviewHistoryExample.createCriteria().andReviewIdEqualTo(reviewId).andCaseIdEqualTo("gyqReviewCaseTestOne");
        caseReviewHistories = caseReviewHistoryMapper.selectByExample(caseReviewHistoryExample);
        Assertions.assertEquals(1, caseReviewHistories.size());
        caseReviewFunctionalCaseExample = new CaseReviewFunctionalCaseExample();
        caseReviewFunctionalCaseExample.createCriteria().andReviewIdEqualTo(reviewId).andCaseIdEqualTo("gyqReviewCaseTestOne");
        caseReviewFunctionalCases = caseReviewFunctionalCaseMapper.selectByExample(caseReviewFunctionalCaseExample);
        Assertions.assertTrue(StringUtils.equalsIgnoreCase(caseReviewFunctionalCases.get(0).getStatus(),FunctionalCaseReviewStatus.UN_PASS.toString()));
        caseReviews1 = getCaseReviews("创建用例评审1");
        Assertions.assertTrue(StringUtils.equals(caseReviews1.get(0).getStatus(), CaseReviewStatus.COMPLETED.toString()));
        NotificationExample notificationExample = new NotificationExample();
        notificationExample.createCriteria().andResourceTypeEqualTo(NoticeConstants.TaskType.CASE_REVIEW_TASK);
        List<Notification> notifications = notificationMapper.selectByExampleWithBLOBs(notificationExample);
        Assertions.assertFalse(notifications.isEmpty());
        notificationExample = new NotificationExample();
        notificationExample.createCriteria().andResourceTypeEqualTo(NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK);
        notifications = notificationMapper.selectByExampleWithBLOBs(notificationExample);
        Assertions.assertFalse(notifications.isEmpty());
    }

    @Test
    @Order(1)
    public void saveReviewMultipleSuccess() throws Exception {
        //多人评审部分通过
        List<String> caseIds = new ArrayList<>();
        caseIds.add("gyqReviewCaseTestTwo");
        List<String> reviewers = new ArrayList<>();
        reviewers.add("admin");
        reviewers.add("default-project-member-user-gyq");
        List<CaseReview> caseReviews = addReview("创建用例评审2", caseIds, reviewers);
        String reviewId = caseReviews.get(0).getId();
        ReviewFunctionalCaseRequest reviewFunctionalCaseRequest = new ReviewFunctionalCaseRequest();
        reviewFunctionalCaseRequest.setReviewId(reviewId);
        reviewFunctionalCaseRequest.setCaseId("gyqReviewCaseTestTwo");
        reviewFunctionalCaseRequest.setProjectId(projectId);
        reviewFunctionalCaseRequest.setStatus(FunctionalCaseReviewStatus.PASS.toString());
        reviewFunctionalCaseRequest.setContent("通过了");
        reviewFunctionalCaseRequest.setNotifier("default-project-member-user-gyq-2");
        reviewFunctionalCaseRequest.setReviewPassRule(CaseReviewPassRule.MULTIPLE.toString());
        this.requestPostWithOk(SAVE_REVIEW, reviewFunctionalCaseRequest);
        CaseReviewHistoryExample caseReviewHistoryExample = new CaseReviewHistoryExample();
        caseReviewHistoryExample.createCriteria().andReviewIdEqualTo(reviewId).andCaseIdEqualTo("gyqReviewCaseTestTwo");
        List<CaseReviewHistory> caseReviewHistories = caseReviewHistoryMapper.selectByExample(caseReviewHistoryExample);
        Assertions.assertEquals(1, caseReviewHistories.size());
        CaseReviewFunctionalCaseExample  caseReviewFunctionalCaseExample = new CaseReviewFunctionalCaseExample();
        caseReviewFunctionalCaseExample.createCriteria().andReviewIdEqualTo(reviewId).andCaseIdEqualTo("gyqReviewCaseTestTwo");
        List<CaseReviewFunctionalCase> caseReviewFunctionalCases = caseReviewFunctionalCaseMapper.selectByExample(caseReviewFunctionalCaseExample);
        Assertions.assertTrue(StringUtils.equalsIgnoreCase(caseReviewFunctionalCases.get(0).getStatus(),FunctionalCaseReviewStatus.UNDER_REVIEWED.toString()));
        List<CaseReview> caseReviews1 = getCaseReviews("创建用例评审2");
        Assertions.assertTrue(StringUtils.equals(caseReviews1.get(0).getStatus(), CaseReviewStatus.UNDERWAY.toString()));
    }

    @Test
    @Order(2)
    public void saveReviewMultipleAllSuccess() throws Exception {
        //多人评审部分通过
        List<String> caseIds = new ArrayList<>();
        caseIds.add("gyqReviewCaseTestThree");
        caseIds.add("gyqReviewCaseTestFour");
        List<String> reviewers = new ArrayList<>();
        reviewers.add("admin");
        List<CaseReview> caseReviews = addReview("创建用例评审3", caseIds, reviewers);
        String reviewId = caseReviews.get(0).getId();
        ReviewFunctionalCaseRequest reviewFunctionalCaseRequest = new ReviewFunctionalCaseRequest();
        reviewFunctionalCaseRequest.setReviewId(reviewId);
        reviewFunctionalCaseRequest.setCaseId("gyqReviewCaseTestThree");
        reviewFunctionalCaseRequest.setProjectId(projectId);
        reviewFunctionalCaseRequest.setStatus(FunctionalCaseReviewStatus.PASS.toString());
        reviewFunctionalCaseRequest.setContent("通过了");
        reviewFunctionalCaseRequest.setNotifier("default-project-member-user-gyq-2");
        reviewFunctionalCaseRequest.setReviewPassRule(CaseReviewPassRule.MULTIPLE.toString());
        this.requestPostWithOk(SAVE_REVIEW, reviewFunctionalCaseRequest);
        CaseReviewHistoryExample caseReviewHistoryExample = new CaseReviewHistoryExample();
        caseReviewHistoryExample.createCriteria().andReviewIdEqualTo(reviewId).andCaseIdEqualTo("gyqReviewCaseTestThree");
        List<CaseReviewHistory> caseReviewHistories = caseReviewHistoryMapper.selectByExample(caseReviewHistoryExample);
        Assertions.assertEquals(1, caseReviewHistories.size());
        CaseReviewFunctionalCaseExample  caseReviewFunctionalCaseExample = new CaseReviewFunctionalCaseExample();
        caseReviewFunctionalCaseExample.createCriteria().andReviewIdEqualTo(reviewId).andCaseIdEqualTo("gyqReviewCaseTestThree");
        List<CaseReviewFunctionalCase> caseReviewFunctionalCases = caseReviewFunctionalCaseMapper.selectByExample(caseReviewFunctionalCaseExample);
        Assertions.assertTrue(StringUtils.equalsIgnoreCase(caseReviewFunctionalCases.get(0).getStatus(),FunctionalCaseReviewStatus.PASS.toString()));
        List<CaseReview> caseReviews1 = getCaseReviews("创建用例评审3");
        Assertions.assertTrue(StringUtils.equals(caseReviews1.get(0).getStatus(), CaseReviewStatus.UNDERWAY.toString()));

        reviewFunctionalCaseRequest = new ReviewFunctionalCaseRequest();
        reviewFunctionalCaseRequest.setReviewId(reviewId);
        reviewFunctionalCaseRequest.setCaseId("gyqReviewCaseTestFour");
        reviewFunctionalCaseRequest.setProjectId(projectId);
        reviewFunctionalCaseRequest.setStatus(FunctionalCaseReviewStatus.UN_PASS.toString());
        reviewFunctionalCaseRequest.setContent("不通过");
        reviewFunctionalCaseRequest.setNotifier("default-project-member-user-gyq-2");
        reviewFunctionalCaseRequest.setReviewPassRule(CaseReviewPassRule.MULTIPLE.toString());
        this.requestPostWithOk(SAVE_REVIEW, reviewFunctionalCaseRequest);
        caseReviewHistoryExample = new CaseReviewHistoryExample();
        caseReviewHistoryExample.createCriteria().andReviewIdEqualTo(reviewId).andCaseIdEqualTo("gyqReviewCaseTestFour");
        caseReviewHistories = caseReviewHistoryMapper.selectByExample(caseReviewHistoryExample);
        Assertions.assertEquals(1, caseReviewHistories.size());
        caseReviewFunctionalCaseExample = new CaseReviewFunctionalCaseExample();
        caseReviewFunctionalCaseExample.createCriteria().andReviewIdEqualTo(reviewId).andCaseIdEqualTo("gyqReviewCaseTestFour");
        caseReviewFunctionalCases = caseReviewFunctionalCaseMapper.selectByExample(caseReviewFunctionalCaseExample);
        Assertions.assertTrue(StringUtils.equalsIgnoreCase(caseReviewFunctionalCases.get(0).getStatus(),FunctionalCaseReviewStatus.UN_PASS.toString()));
        caseReviews1 = getCaseReviews("创建用例评审3");
        Assertions.assertTrue(StringUtils.equals(caseReviews1.get(0).getStatus(), CaseReviewStatus.COMPLETED.toString()));
    }

    @Test
    @Order(3)
    public void saveReviewFalse() throws Exception {
        //多人评审部分通过
        List<String> caseIds = new ArrayList<>();
        caseIds.add("gyqReviewCaseTestThree");
        caseIds.add("gyqReviewCaseTestFour");
        List<String> reviewers = new ArrayList<>();
        reviewers.add("admin");
        List<CaseReview> caseReviews = addReview("创建用例评审4", caseIds, reviewers);
        String reviewId = caseReviews.get(0).getId();
        ReviewFunctionalCaseRequest reviewFunctionalCaseRequest = new ReviewFunctionalCaseRequest();
        reviewFunctionalCaseRequest.setReviewId(reviewId);
        reviewFunctionalCaseRequest.setCaseId("gyqReviewCaseTestFour");
        reviewFunctionalCaseRequest.setProjectId(projectId);
        reviewFunctionalCaseRequest.setStatus(FunctionalCaseReviewStatus.UN_PASS.toString());
        reviewFunctionalCaseRequest.setNotifier("default-project-member-user-gyq-2");
        reviewFunctionalCaseRequest.setReviewPassRule(CaseReviewPassRule.SINGLE.toString());
        this.requestPost(SAVE_REVIEW, reviewFunctionalCaseRequest).andExpect(status().is5xxServerError());

    }

    @Test
    @Order(4)
    public void getListSuccess() throws Exception {
        List<CaseReview> caseReviews = getCaseReviews("创建用例评审1");
        String reviewId = caseReviews.get(0).getId();
        CaseReviewHistory caseReviewHistory = new CaseReviewHistory();
        caseReviewHistory.setReviewId(reviewId);
        caseReviewHistory.setCaseId("gyqReviewCaseTest");
        caseReviewHistory.setCreateUser("system");
        caseReviewHistory.setStatus(FunctionalCaseReviewStatus.RE_REVIEWED.toString());
        caseReviewHistory.setId("test");
        caseReviewHistory.setCreateTime(System.currentTimeMillis());
        caseReviewHistoryMapper.insertSelective(caseReviewHistory);
        List<CaseReviewHistoryDTO> gyqReviewCaseTest = getCaseReviewHistoryList("gyqReviewCaseTest", reviewId);
        System.out.println(JSON.toJSONString(gyqReviewCaseTest));
    }


    public List<CaseReviewHistoryDTO> getCaseReviewHistoryList(String caseId,String reviewId) throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(REVIEW_LIST +"/"+reviewId +"/"+ caseId).header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .header(SessionConstants.CURRENT_PROJECT, projectId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        return JSON.parseArray(JSON.toJSONString(resultHolder.getData()), CaseReviewHistoryDTO.class);

    }

    private  List<CaseReview> addReview(String name, List<String> caseIds, List<String> reviewers) throws Exception {
        CaseReviewRequest caseReviewRequest = new CaseReviewRequest();
        caseReviewRequest.setProjectId(projectId);
        caseReviewRequest.setName(name);
        caseReviewRequest.setModuleId("CASE_REVIEW_REAL_MODULE_ID");
        caseReviewRequest.setReviewPassRule(CaseReviewPassRule.SINGLE.toString());
        caseReviewRequest.setReviewers(reviewers);
        BaseAssociateCaseRequest baseAssociateCaseRequest = new BaseAssociateCaseRequest();
        baseAssociateCaseRequest.setSelectAll(false);
        baseAssociateCaseRequest.setProjectId(projectId);
        baseAssociateCaseRequest.setSelectIds(caseIds);
        caseReviewRequest.setBaseAssociateCaseRequest(baseAssociateCaseRequest);
        List<String> tags = new ArrayList<>();
        tags.add("11");
        caseReviewRequest.setTags(tags);
        this.requestPostWithOk(ADD_CASE_REVIEW, caseReviewRequest);
        return getCaseReviews(name);
    }

    private List<CaseReview> getCaseReviews(String name) {
        CaseReviewExample caseReviewExample = new CaseReviewExample();
        caseReviewExample.createCriteria().andNameEqualTo(name);
        return caseReviewMapper.selectByExample(caseReviewExample);
    }


}
