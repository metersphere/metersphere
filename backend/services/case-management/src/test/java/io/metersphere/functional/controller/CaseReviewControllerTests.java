package io.metersphere.functional.controller;

import io.metersphere.functional.constants.CaseReviewPassRule;
import io.metersphere.functional.domain.*;
import io.metersphere.functional.mapper.CaseReviewFollowerMapper;
import io.metersphere.functional.mapper.CaseReviewFunctionalCaseMapper;
import io.metersphere.functional.mapper.CaseReviewMapper;
import io.metersphere.functional.mapper.CaseReviewUserMapper;
import io.metersphere.functional.request.CaseReviewAddRequest;
import io.metersphere.functional.request.CaseReviewFollowerRequest;
import io.metersphere.system.base.BaseTest;
import jakarta.annotation.Resource;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;

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
    private static final String FOLLOW_CASE_REVIEW = "/case/review/edit/follower";

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
        CaseReviewAddRequest caseReviewAddRequest = getCaseReviewAddRequest("创建评审1", CaseReviewPassRule.SINGLE.toString(), "CASE_REVIEW_TEST_GYQ_ID", false, true);
        this.requestPostWithOk(ADD_CASE_REVIEW,caseReviewAddRequest);
        CaseReviewExample caseReviewExample = new CaseReviewExample();
        caseReviewExample.createCriteria().andNameEqualTo("创建评审1");
        List<CaseReview> caseReviews = caseReviewMapper.selectByExample(caseReviewExample);
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

    @NotNull
    private static CaseReviewAddRequest getCaseReviewAddRequest(String name, String reviewPassRule, String caseId, boolean tag, boolean reviewer) {
        CaseReviewAddRequest caseReviewAddRequest = new CaseReviewAddRequest();
        caseReviewAddRequest.setProjectId(projectId);
        caseReviewAddRequest.setName(name);
        caseReviewAddRequest.setModuleId("CASE_REVIEW_REAL_MODULE_ID");
        caseReviewAddRequest.setReviewPassRule(reviewPassRule);
        List<String> reviewers = new ArrayList<>();
        reviewers.add("admin");
        if (reviewer) {
            caseReviewAddRequest.setReviewers(reviewers);
        }
        if (StringUtils.isNotBlank(caseId)) {
            List<String> caseIds = new ArrayList<>();
            caseIds.add(caseId);
            caseReviewAddRequest.setCaseIds(caseIds);
        }
        if (tag) {
            List<String> tags = new ArrayList<>();
            tags.add("11");
            caseReviewAddRequest.setTags(tags);
        }
        return caseReviewAddRequest;
    }

    @Test
    @Order(2)
    public void addCaseReviewWidthOutCaseIdsSuccess() throws Exception {
        CaseReviewAddRequest caseReviewAddRequest = getCaseReviewAddRequest("创建评审2", CaseReviewPassRule.SINGLE.toString(), null, false, true);
        this.requestPostWithOk(ADD_CASE_REVIEW,caseReviewAddRequest);
        CaseReviewExample caseReviewExample = new CaseReviewExample();
        caseReviewExample.createCriteria().andNameEqualTo("创建评审2");
        List<CaseReview> caseReviews = caseReviewMapper.selectByExample(caseReviewExample);
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
        CaseReviewAddRequest caseReviewAddRequest = getCaseReviewAddRequest("创建评审3", CaseReviewPassRule.SINGLE.toString(), null, true, true);
        this.requestPostWithOk(ADD_CASE_REVIEW,caseReviewAddRequest);
        CaseReviewExample caseReviewExample = new CaseReviewExample();
        caseReviewExample.createCriteria().andNameEqualTo("创建评审3");
        List<CaseReview> caseReviews = caseReviewMapper.selectByExample(caseReviewExample);
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
        CaseReviewAddRequest caseReviewAddRequestNoReviewer= getCaseReviewAddRequest("创建评审4", CaseReviewPassRule.SINGLE.toString(), null, true, false);
        this.requestPost(ADD_CASE_REVIEW, caseReviewAddRequestNoReviewer).andExpect(status().is4xxClientError());
        CaseReviewAddRequest caseReviewAddRequestNoName = getCaseReviewAddRequest(null, CaseReviewPassRule.SINGLE.toString(), null, true, true);
        this.requestPost(ADD_CASE_REVIEW, caseReviewAddRequestNoName).andExpect(status().is4xxClientError());
    }

    @Test
    @Order(5)
    public void followCaseReview() throws Exception {
        CaseReviewExample caseReviewExample = new CaseReviewExample();
        caseReviewExample.createCriteria().andNameEqualTo("创建评审1");
        List<CaseReview> caseReviews = caseReviewMapper.selectByExample(caseReviewExample);
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


}
