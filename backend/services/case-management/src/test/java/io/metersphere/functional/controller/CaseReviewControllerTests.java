package io.metersphere.functional.controller;

import io.metersphere.functional.constants.CaseReviewPassRule;
import io.metersphere.functional.domain.*;
import io.metersphere.functional.mapper.CaseReviewFollowerMapper;
import io.metersphere.functional.mapper.CaseReviewFunctionalCaseMapper;
import io.metersphere.functional.mapper.CaseReviewMapper;
import io.metersphere.functional.mapper.CaseReviewUserMapper;
import io.metersphere.functional.request.CaseReviewFollowerRequest;
import io.metersphere.functional.request.CaseReviewRequest;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.User;
import jakarta.annotation.Resource;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CaseReviewControllerTests extends BaseTest {

    @Resource
    private MockMvc mockMvc;

    private static final String projectId = "project-gyq-case-review-test";

    private static final String ADD_CASE_REVIEW = "/case/review/add";
    private static final String EDIT_CASE_REVIEW = "/case/review/edit";
    private static final String FOLLOW_CASE_REVIEW = "/case/review/edit/follower";
    private static final String CASE_REVIEWER_LIST = "/case/review/user-option/";


    @Resource
    private CaseReviewMapper caseReviewMapper;
    @Resource
    private CaseReviewFollowerMapper caseReviewFollowerMapper;

    @Resource
    private CaseReviewUserMapper caseReviewUserMapper;
    @Resource
    private CaseReviewFunctionalCaseMapper caseReviewFunctionalCaseMapper;

    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_case_review.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void addCaseReviewSuccess() throws Exception {
        CaseReviewRequest caseReviewRequest = getCaseReviewAddRequest("创建评审1", CaseReviewPassRule.SINGLE.toString(), "CASE_REVIEW_TEST_GYQ_ID", false, true, null);
        this.requestPostWithOk(ADD_CASE_REVIEW, caseReviewRequest);
        List<CaseReview> caseReviews = getCaseReviews("创建评审1");
        Assertions.assertEquals(1, caseReviews.size());
        String caseReviewId = caseReviews.get(0).getId();
        CaseReviewUserExample caseReviewUserExample = new CaseReviewUserExample();
        caseReviewUserExample.createCriteria().andReviewIdEqualTo(caseReviewId);
        List<CaseReviewUser> caseReviewUsers = caseReviewUserMapper.selectByExample(caseReviewUserExample);
        Assertions.assertEquals(1, caseReviewUsers.size());
        CaseReviewFunctionalCaseExample caseReviewFunctionalCaseExample = new CaseReviewFunctionalCaseExample();
        caseReviewFunctionalCaseExample.createCriteria().andReviewIdEqualTo(caseReviewId);
        List<CaseReviewFunctionalCase> caseReviewFunctionalCases = caseReviewFunctionalCaseMapper.selectByExample(caseReviewFunctionalCaseExample);
        Assertions.assertEquals(1, caseReviewFunctionalCases.size());
    }

    private List<CaseReview> getCaseReviews(String name) {
        CaseReviewExample caseReviewExample = new CaseReviewExample();
        caseReviewExample.createCriteria().andNameEqualTo(name);
        return caseReviewMapper.selectByExample(caseReviewExample);
    }

    @NotNull
    private static CaseReviewRequest getCaseReviewAddRequest(String name, String reviewPassRule, String caseId, boolean tag, boolean reviewer, String id) {
        CaseReviewRequest caseReviewRequest = new CaseReviewRequest();
        if (StringUtils.isNotBlank(id)) {
            caseReviewRequest.setId(id);
        }
        caseReviewRequest.setProjectId(projectId);
        caseReviewRequest.setName(name);
        caseReviewRequest.setModuleId("CASE_REVIEW_REAL_MODULE_ID");
        caseReviewRequest.setReviewPassRule(reviewPassRule);
        List<String> reviewers = new ArrayList<>();
        reviewers.add("admin");
        if (reviewer) {
            caseReviewRequest.setReviewers(reviewers);
        }
        if (StringUtils.isNotBlank(caseId)) {
            List<String> caseIds = new ArrayList<>();
            caseIds.add(caseId);
            caseReviewRequest.setCaseIds(caseIds);
        }
        if (tag) {
            List<String> tags = new ArrayList<>();
            tags.add("11");
            caseReviewRequest.setTags(tags);
        }
        return caseReviewRequest;
    }

    @Test
    @Order(2)
    public void addCaseReviewWidthOutCaseIdsSuccess() throws Exception {
        CaseReviewRequest caseReviewRequest = getCaseReviewAddRequest("创建评审2", CaseReviewPassRule.SINGLE.toString(), null, false, true, null);
        this.requestPostWithOk(ADD_CASE_REVIEW, caseReviewRequest);
        List<CaseReview> caseReviews = getCaseReviews("创建评审2");
        Assertions.assertEquals(1, caseReviews.size());
        String caseReviewId = caseReviews.get(0).getId();
        CaseReviewUserExample caseReviewUserExample = new CaseReviewUserExample();
        caseReviewUserExample.createCriteria().andReviewIdEqualTo(caseReviewId);
        List<CaseReviewUser> caseReviewUsers = caseReviewUserMapper.selectByExample(caseReviewUserExample);
        Assertions.assertEquals(1, caseReviewUsers.size());
        CaseReviewFunctionalCaseExample caseReviewFunctionalCaseExample = new CaseReviewFunctionalCaseExample();
        caseReviewFunctionalCaseExample.createCriteria().andReviewIdEqualTo(caseReviewId);
        List<CaseReviewFunctionalCase> caseReviewFunctionalCases = caseReviewFunctionalCaseMapper.selectByExample(caseReviewFunctionalCaseExample);
        Assertions.assertEquals(0, caseReviewFunctionalCases.size());
    }


    @Test
    @Order(3)
    public void addCaseReviewWidthTagsSuccess() throws Exception {
        CaseReviewRequest caseReviewRequest = getCaseReviewAddRequest("创建评审3", CaseReviewPassRule.SINGLE.toString(), null, true, true, null);
        this.requestPostWithOk(ADD_CASE_REVIEW, caseReviewRequest);
        List<CaseReview> caseReviews = getCaseReviews("创建评审3");
        Assertions.assertEquals(1, caseReviews.size());
        String caseReviewId = caseReviews.get(0).getId();
        Assertions.assertNotNull(caseReviews.get(0).getTags());
        CaseReviewUserExample caseReviewUserExample = new CaseReviewUserExample();
        caseReviewUserExample.createCriteria().andReviewIdEqualTo(caseReviewId);
        List<CaseReviewUser> caseReviewUsers = caseReviewUserMapper.selectByExample(caseReviewUserExample);
        Assertions.assertEquals(1, caseReviewUsers.size());
        CaseReviewFunctionalCaseExample caseReviewFunctionalCaseExample = new CaseReviewFunctionalCaseExample();
        caseReviewFunctionalCaseExample.createCriteria().andReviewIdEqualTo(caseReviewId);
        List<CaseReviewFunctionalCase> caseReviewFunctionalCases = caseReviewFunctionalCaseMapper.selectByExample(caseReviewFunctionalCaseExample);
        Assertions.assertEquals(0, caseReviewFunctionalCases.size());
    }

    @Test
    @Order(4)
    public void addCaseReviewFalse() throws Exception {
        CaseReviewRequest caseReviewRequestNoReviewer = getCaseReviewAddRequest("创建评审4", CaseReviewPassRule.SINGLE.toString(), null, true, false, null);
        this.requestPost(ADD_CASE_REVIEW, caseReviewRequestNoReviewer).andExpect(status().is4xxClientError());
        CaseReviewRequest caseReviewRequestNoName = getCaseReviewAddRequest(null, CaseReviewPassRule.SINGLE.toString(), null, true, true, null);
        this.requestPost(ADD_CASE_REVIEW, caseReviewRequestNoName).andExpect(status().is4xxClientError());
    }

    @Test
    @Order(5)
    public void followCaseReview() throws Exception {
        List<CaseReview> caseReviews = getCaseReviews("创建评审1");
        CaseReview caseReview = caseReviews.get(0);
        CaseReviewFollowerRequest caseReviewFollowerRequest = new CaseReviewFollowerRequest();
        caseReviewFollowerRequest.setCaseReviewId(caseReview.getId());
        caseReviewFollowerRequest.setUserId("admin");
        this.requestPostWithOk(FOLLOW_CASE_REVIEW,caseReviewFollowerRequest);
        CaseReviewFollowerExample example = new CaseReviewFollowerExample();
        example.createCriteria().andReviewIdEqualTo(caseReview.getId()).andUserIdEqualTo("admin");
        Assertions.assertTrue(caseReviewFollowerMapper.countByExample(example) > 0);
        caseReviewFollowerRequest = new CaseReviewFollowerRequest();
        caseReviewFollowerRequest.setCaseReviewId(caseReview.getId());
        caseReviewFollowerRequest.setUserId("admin");
        this.requestPostWithOk(FOLLOW_CASE_REVIEW,caseReviewFollowerRequest);
        example = new CaseReviewFollowerExample();
        example.createCriteria().andReviewIdEqualTo(caseReview.getId()).andUserIdEqualTo("admin");
        Assertions.assertEquals(0, caseReviewFollowerMapper.countByExample(example));

    }

    @Test
    @Order(6)
    public void followCaseReviewFalse() throws Exception {
        CaseReviewFollowerRequest caseReviewFollowerRequest = new CaseReviewFollowerRequest();
        caseReviewFollowerRequest.setCaseReviewId("XXXXX");
        caseReviewFollowerRequest.setUserId("admin");
        this.requestPost(FOLLOW_CASE_REVIEW, caseReviewFollowerRequest).andExpect(status().is5xxServerError());
    }

    @Test
    @Order(7)
    public void editCaseReviewSuccess() throws Exception {
        List<CaseReview> caseReviews = getCaseReviews("创建评审1");
        CaseReview caseReview = caseReviews.get(0);
        CaseReviewRequest caseReviewRequest = getCaseReviewAddRequest("创建评审更新1", CaseReviewPassRule.SINGLE.toString(), null, true, true, caseReview.getId());
        this.requestPostWithOk(EDIT_CASE_REVIEW, caseReviewRequest);
        List<CaseReview> updateCaseReviews = getCaseReviews("创建评审更新1");
        Assertions.assertEquals(1, updateCaseReviews.size());

        List<CaseReview> caseReviews2 = getCaseReviews("创建评审2");
        CaseReview caseReview2 = caseReviews2.get(0);
        CaseReviewRequest caseReviewRequest2 = getCaseReviewAddRequest("创建评审更新2", CaseReviewPassRule.SINGLE.toString(), null, false, true, caseReview2.getId());
        this.requestPostWithOk(EDIT_CASE_REVIEW, caseReviewRequest2);
        List<CaseReview> updateCaseReviews2 = getCaseReviews("创建评审更新2");
        Assertions.assertEquals(1, updateCaseReviews2.size());

    }

    @Test
    @Order(8)
    public void editCaseReviewFalse() throws Exception {
        CaseReviewRequest caseReviewRequest = getCaseReviewAddRequest("创建评审更新1", CaseReviewPassRule.SINGLE.toString(), null, true, true, null);
        this.requestPost(EDIT_CASE_REVIEW, caseReviewRequest).andExpect(status().is4xxClientError());
        caseReviewRequest = getCaseReviewAddRequest("创建评审更新1", CaseReviewPassRule.SINGLE.toString(), null, true, true, "XXX");
        this.requestPost(EDIT_CASE_REVIEW, caseReviewRequest).andExpect(status().is5xxServerError());
    }

    @Test
    @Order(9)
    public void getUserList() throws Exception {
        MvcResult mvcResult = this.requestGetWithOkAndReturn(CASE_REVIEWER_LIST + projectId);
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        List<User> list = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), User.class);
        Assertions.assertFalse(list.isEmpty());
    }


}
