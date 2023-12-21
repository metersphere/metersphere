package io.metersphere.functional.controller;

import io.metersphere.functional.constants.CaseReviewPassRule;
import io.metersphere.functional.constants.FunctionalCaseReviewStatus;
import io.metersphere.functional.domain.*;
import io.metersphere.functional.dto.CaseReviewDTO;
import io.metersphere.functional.mapper.*;
import io.metersphere.functional.request.*;
import io.metersphere.functional.result.CaseManagementResultCode;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.User;
import io.metersphere.system.dto.sdk.request.PosRequest;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CaseReviewControllerTests extends BaseTest {

    @Resource
    private MockMvc mockMvc;

    private static final String projectId = "project-gyq-case-review-test";

    private static final String ADD_CASE_REVIEW = "/case/review/add";
    private static final String COPY_CASE_REVIEW = "/case/review/copy";
    private static final String BATCH_MOVE_CASE_REVIEW = "/case/review/batch/move";
    private static final String EDIT_CASE_REVIEW = "/case/review/edit";
    private static final String PAGE_CASE_REVIEW = "/case/review/page";
    private static final String MODULE_COUNT_CASE_REVIEW = "/case/review/module/count";
    private static final String ASSOCIATE_CASE_REVIEW = "/case/review/associate";
    private static final String DISASSOCIATE_CASE_REVIEW = "/case/review/disassociate/";
    private static final String EDIT_POS_CASE_REVIEW_URL = "/case/review/edit/pos";
    private static final String FOLLOW_CASE_REVIEW = "/case/review/edit/follower";
    private static final String CASE_REVIEWER_LIST = "/case/review/user-option/";
    private static final String DETAIL_CASE_REVIEW = "/case/review/detail/";
    private static final String DELETE_CASE_REVIEW = "/case/review/delete/";



    @Resource
    private CaseReviewMapper caseReviewMapper;
    @Resource
    private CaseReviewFollowerMapper caseReviewFollowerMapper;

    @Resource
    private CaseReviewUserMapper caseReviewUserMapper;
    @Resource
    private CaseReviewFunctionalCaseMapper caseReviewFunctionalCaseMapper;
    @Resource
    private CaseReviewFunctionalCaseUserMapper caseReviewFunctionalCaseUserMapper;

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


        caseReviewRequest = getCaseReviewAddRequest("创建评审1", CaseReviewPassRule.SINGLE.toString(), "CASE_REVIEW_TEST_GYQ_ID", false, true, null);
        this.requestPostWithOk(COPY_CASE_REVIEW, caseReviewRequest);
        caseReviews = getCaseReviews("创建评审1");
        Assertions.assertEquals(2, caseReviews.size());
        List<String> list = caseReviews.stream().map(CaseReview::getId).distinct().toList();
        Assertions.assertEquals(2, list.size());

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
        BaseAssociateCaseRequest baseAssociateCaseRequest = new BaseAssociateCaseRequest();
        baseAssociateCaseRequest.setProjectId(projectId);
        baseAssociateCaseRequest.setSelectAll(false);
        List<String> reviewers = new ArrayList<>();
        reviewers.add("admin");
        if (reviewer) {
            caseReviewRequest.setReviewers(reviewers);
        }
        if (StringUtils.isNotBlank(caseId)) {
            List<String> caseIds = new ArrayList<>();
            caseIds.add(caseId);
            baseAssociateCaseRequest.setSelectIds(caseIds);
        }
        if (tag) {
            List<String> tags = new ArrayList<>();
            tags.add("11");
            caseReviewRequest.setTags(tags);
        }
        caseReviewRequest.setBaseAssociateCaseRequest(baseAssociateCaseRequest);
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
        this.requestPostWithOk(FOLLOW_CASE_REVIEW, caseReviewFollowerRequest);
        CaseReviewFollowerExample example = new CaseReviewFollowerExample();
        example.createCriteria().andReviewIdEqualTo(caseReview.getId()).andUserIdEqualTo("admin");
        Assertions.assertTrue(caseReviewFollowerMapper.countByExample(example) > 0);
        caseReviewFollowerRequest = new CaseReviewFollowerRequest();
        caseReviewFollowerRequest.setCaseReviewId(caseReview.getId());
        caseReviewFollowerRequest.setUserId("admin");
        this.requestPostWithOk(FOLLOW_CASE_REVIEW, caseReviewFollowerRequest);
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

    @Test
    @Order(10)
    public void associateCaseSuccess() throws Exception {
        List<CaseReview> caseReviews = getCaseReviews("创建评审更新1");
        Assertions.assertEquals(1, caseReviews.size());
        String caseReviewId = caseReviews.get(0).getId();
        CaseReviewAssociateRequest caseReviewAssociateRequest = new CaseReviewAssociateRequest();
        caseReviewAssociateRequest.setProjectId(projectId);
        caseReviewAssociateRequest.setReviewId(caseReviewId);
        BaseAssociateCaseRequest baseAssociateCaseRequest = new BaseAssociateCaseRequest();
        baseAssociateCaseRequest.setProjectId(projectId);
        baseAssociateCaseRequest.setSelectAll(true);
        baseAssociateCaseRequest.setExcludeIds(List.of("CASE_REVIEW_TEST_GYQ_ID"));
        caseReviewAssociateRequest.setBaseAssociateCaseRequest(baseAssociateCaseRequest);
        List<String> userIds = new ArrayList<>();
        userIds.add("gyq_review_test");
        userIds.add("gyq_review_test2");
        caseReviewAssociateRequest.setReviewers(userIds);
        this.requestPostWithOk(ASSOCIATE_CASE_REVIEW, caseReviewAssociateRequest);
        CaseReviewFunctionalCaseExample caseReviewFunctionalCaseExample = new CaseReviewFunctionalCaseExample();
        caseReviewFunctionalCaseExample.createCriteria().andReviewIdEqualTo(caseReviewId);
        List<CaseReviewFunctionalCase> caseReviewFunctionalCases = caseReviewFunctionalCaseMapper.selectByExample(caseReviewFunctionalCaseExample);
        List<String> castIds = caseReviewFunctionalCases.stream().map(CaseReviewFunctionalCase::getCaseId).toList();
        Assertions.assertTrue(CollectionUtils.isNotEmpty(castIds));
        CaseReviewFunctionalCaseUserExample caseReviewFunctionalCaseUserExample = new CaseReviewFunctionalCaseUserExample();
        caseReviewFunctionalCaseUserExample.createCriteria().andReviewIdEqualTo(caseReviewId);
        List<CaseReviewFunctionalCaseUser> caseReviewFunctionalCaseUsers = caseReviewFunctionalCaseUserMapper.selectByExample(caseReviewFunctionalCaseUserExample);
        List<String> userIdList = caseReviewFunctionalCaseUsers.stream().map(CaseReviewFunctionalCaseUser::getUserId).toList();
        Assertions.assertTrue(userIdList.contains("gyq_review_test"));
        Assertions.assertTrue(userIdList.contains("gyq_review_test2"));
        List<CaseReview> caseReviews2 = getCaseReviews("创建评审更新1");
        Assertions.assertTrue( caseReviews.get(0).getCaseCount()<caseReviews2.get(0).getCaseCount());
    }

    @Test
    @Order(11)
    public void associateCaseFalse() throws Exception {
        CaseReviewAssociateRequest caseReviewAssociateRequest = new CaseReviewAssociateRequest();
        caseReviewAssociateRequest.setProjectId(projectId);
        caseReviewAssociateRequest.setReviewId("caseReviewIdXXXX");
        BaseAssociateCaseRequest baseAssociateCaseRequest = new BaseAssociateCaseRequest();
        baseAssociateCaseRequest.setProjectId(projectId);
        baseAssociateCaseRequest.setSelectAll(true);
        caseReviewAssociateRequest.setBaseAssociateCaseRequest(baseAssociateCaseRequest);
        List<String> userIds = new ArrayList<>();
        userIds.add("gyq_review_test");
        userIds.add("gyq_review_test2");
        caseReviewAssociateRequest.setReviewers(userIds);
        this.requestPost(ASSOCIATE_CASE_REVIEW, caseReviewAssociateRequest).andExpect(status().is5xxServerError());
        List<CaseReview> caseReviews = getCaseReviews("创建评审更新1");
        caseReviewAssociateRequest = new CaseReviewAssociateRequest();
        caseReviewAssociateRequest.setProjectId(projectId);
        caseReviewAssociateRequest.setReviewId(caseReviews.get(0).getId());
        baseAssociateCaseRequest = new BaseAssociateCaseRequest();
        baseAssociateCaseRequest.setProjectId(projectId);
        baseAssociateCaseRequest.setSelectAll(false);
        baseAssociateCaseRequest.setSelectIds(List.of("CASE_REVIEW_TEST_GYQ_XX"));
        caseReviewAssociateRequest.setBaseAssociateCaseRequest(baseAssociateCaseRequest);
        userIds = new ArrayList<>();
        userIds.add("gyq_review_test");
        userIds.add("gyq_review_test2");
        caseReviewAssociateRequest.setReviewers(userIds);
        this.requestPostWithOk(ASSOCIATE_CASE_REVIEW, caseReviewAssociateRequest);

        caseReviewAssociateRequest = new CaseReviewAssociateRequest();
        caseReviewAssociateRequest.setProjectId(projectId);
        caseReviewAssociateRequest.setReviewId(caseReviews.get(0).getId());
        baseAssociateCaseRequest = new BaseAssociateCaseRequest();
        baseAssociateCaseRequest.setProjectId("project-gyq-case-review-testYY");
        baseAssociateCaseRequest.setSelectAll(true);
        baseAssociateCaseRequest.setSelectIds(List.of("CASE_REVIEW_TEST_GYQ_XX"));
        caseReviewAssociateRequest.setBaseAssociateCaseRequest(baseAssociateCaseRequest);
        userIds = new ArrayList<>();
        userIds.add("gyq_review_test");
        userIds.add("gyq_review_test2");
        caseReviewAssociateRequest.setReviewers(userIds);
        this.requestPostWithOk(ASSOCIATE_CASE_REVIEW, caseReviewAssociateRequest);
    }

    @Test
    @Order(12)
    public void getPageSuccess() throws Exception {
        List<CaseReview> caseReviews = getCaseReviews("创建评审更新1");
        Assertions.assertEquals(1, caseReviews.size());
        Map<String, Object> caseReviewCombine = buildRequestCombine();
        CaseReviewPageRequest request = new CaseReviewPageRequest();
        Map<String, List<String>> filters = new HashMap<>();
        filters.put("status", Arrays.asList("PREPARED", "UNDERWAY", "COMPLETED", "ARCHIVED"));
        request.setFilter(filters);
        request.setCombine(caseReviewCombine);
        request.setProjectId(projectId);
        request.setKeyword("评审更新");
        request.setReviewByMe("admin");
        request.setCurrent(1);
        request.setPageSize(10);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(PAGE_CASE_REVIEW, request);
        Pager<List<CaseReviewDTO>> tableData = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                Pager.class);

        MvcResult moduleCountMvcResult = this.requestPostWithOkAndReturn(MODULE_COUNT_CASE_REVIEW, request);
        Map<String, Integer> moduleCount = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(moduleCountMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                Map.class);

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

        //如果没有数据，则返回的模块节点也不应该有数据
        boolean moduleHaveResource = false;
        for (int countByModuleId : moduleCount.values()) {
            if (countByModuleId > 0) {
                moduleHaveResource = true;
                break;
            }
        }
        Assertions.assertEquals(request.getPageSize(), tableData.getPageSize());
        if (tableData.getTotal() > 0) {
            Assertions.assertTrue(moduleHaveResource);
        }

        Assertions.assertTrue(moduleCount.containsKey("all"));

        CaseReviewFunctionalCaseExample caseReviewFunctionalCaseExample = new CaseReviewFunctionalCaseExample();
        caseReviewFunctionalCaseExample.createCriteria().andReviewIdEqualTo(caseReviews.get(0).getId());
        List<CaseReviewFunctionalCase> caseReviewFunctionalCases = caseReviewFunctionalCaseMapper.selectByExample(caseReviewFunctionalCaseExample);
        Map<String, CaseReviewFunctionalCase> caseReviewFunctionalCaseMap = caseReviewFunctionalCases.stream().collect(Collectors.toMap(CaseReviewFunctionalCase::getCaseId, t -> t));
        caseReviewFunctionalCaseMap.forEach((k, v) -> {
            switch (k) {
                case "CASE_REVIEW_TEST_GYQ_ID2" -> v.setStatus(FunctionalCaseReviewStatus.RE_REVIEWED.toString());
                case "CASE_REVIEW_TEST_GYQ_ID3" -> v.setStatus(FunctionalCaseReviewStatus.UNDER_REVIEWED.toString());
                case "CASE_REVIEW_TEST_GYQ_ID4" -> v.setStatus(FunctionalCaseReviewStatus.PASS.toString());
                case "CASE_REVIEW_TEST_GYQ_ID5" -> v.setStatus(FunctionalCaseReviewStatus.UN_PASS.toString());
                default -> v.setStatus(FunctionalCaseReviewStatus.UN_REVIEWED.toString());
            }
            caseReviewFunctionalCaseMapper.updateByPrimaryKeySelective(v);
        });

        request = new CaseReviewPageRequest();
        filters = new HashMap<>();
        filters.put("status", Arrays.asList("PREPARED", "UNDERWAY", "COMPLETED", "ARCHIVED"));
        request.setFilter(filters);
        request.setCombine(caseReviewCombine);
        request.setProjectId(projectId);
        request.setKeyword("评审更新");
        request.setCurrent(1);
        request.setPageSize(10);
        mvcResult = this.requestPostWithOkAndReturn(PAGE_CASE_REVIEW, request);
        // 获取返回值
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        pageData = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
        // 返回值不为空
        Assertions.assertNotNull(pageData);
        // 返回值的页码和当前页码相同
        Assertions.assertEquals(pageData.getCurrent(), request.getCurrent());
        // 返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(JSON.parseArray(JSON.toJSONString(pageData.getList())).size() <= request.getPageSize());
        List<CaseReviewDTO> caseReviewDTOS = JSON.parseArray(JSON.toJSONString(pageData.getList()), CaseReviewDTO.class);
        List<CaseReviewDTO> caseReviewOne = caseReviewDTOS.stream().filter(t -> StringUtils.equals(t.getName(), "创建评审更新1")).toList();
        Assertions.assertTrue(caseReviewOne.get(0).getPassCount() > 0);
        Assertions.assertTrue(caseReviewOne.get(0).getUnPassCount() > 0);
        Assertions.assertTrue(caseReviewOne.get(0).getUnderReviewedCount() > 0);
        Assertions.assertTrue(caseReviewOne.get(0).getReReviewedCount() > 0);
        Assertions.assertTrue(caseReviewOne.get(0).getReviewedCount() > 0);

        request = new CaseReviewPageRequest();
        filters = new HashMap<>();
        filters.put("status", Arrays.asList("UNDERWAY", "COMPLETED", "ARCHIVED"));
        request.setFilter(filters);
        request.setCombine(caseReviewCombine);
        request.setProjectId(projectId);
        request.setKeyword("评审更新");
        request.setCurrent(1);
        request.setPageSize(10);
        mvcResult = this.requestPostWithOkAndReturn(PAGE_CASE_REVIEW, request);
        // 获取返回值
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        pageData = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
        // 返回值不为空
        Assertions.assertNotNull(pageData);
        // 返回值的页码和当前页码相同
        Assertions.assertEquals(pageData.getCurrent(), request.getCurrent());
        // 返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(JSON.parseArray(JSON.toJSONString(pageData.getList())).size() <= request.getPageSize());
        caseReviewDTOS = JSON.parseArray(JSON.toJSONString(pageData.getList()), CaseReviewDTO.class);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(caseReviewDTOS));

    }

    @Test
    @Order(13)
    public void testPos() throws Exception {
        List<CaseReview> caseReviews = getCaseReviews("创建评审更新1");
        List<CaseReview> caseReviews2 = getCaseReviews("创建评审更新2");
        Long pos = caseReviews.get(0).getPos();
        Long pos2 = caseReviews2.get(0).getPos();
        PosRequest posRequest = new PosRequest();
        posRequest.setProjectId(projectId);
        posRequest.setTargetId(caseReviews.get(0).getId());
        posRequest.setMoveId(caseReviews2.get(0).getId());
        posRequest.setMoveMode("AFTER");
        this.requestPostWithOkAndReturn(EDIT_POS_CASE_REVIEW_URL, posRequest);
        caseReviews = getCaseReviews("创建评审更新1");
        caseReviews2 = getCaseReviews("创建评审更新2");
        Long pos3 = caseReviews.get(0).getPos();
        Long pos4 = caseReviews2.get(0).getPos();
        Assertions.assertTrue(Objects.equals(pos, pos3));
        Assertions.assertTrue(pos2 > pos4);
        posRequest.setMoveMode("BEFORE");
        this.requestPostWithOkAndReturn(EDIT_POS_CASE_REVIEW_URL, posRequest);
        caseReviews = getCaseReviews("创建评审更新1");
        caseReviews2 = getCaseReviews("创建评审更新2");
        Long pos5 = caseReviews.get(0).getPos();
        Long pos6 = caseReviews2.get(0).getPos();
        Assertions.assertTrue(Objects.equals(pos5, pos3));
        Assertions.assertTrue(pos6 > pos4);
    }

    @Test
    @Order(14)
    public void testFunctionalCaseDetail() throws Exception {
        List<CaseReview> caseReviews = getCaseReviews("创建评审更新1");
        String id = caseReviews.get(0).getId();
        assertErrorCode(this.requestGet(DETAIL_CASE_REVIEW + "ERROR_TEST_FUNCTIONAL_CASE_ID"), CaseManagementResultCode.CASE_REVIEW_NOT_FOUND);
        MvcResult mvcResult = this.requestGetWithOkAndReturn(DETAIL_CASE_REVIEW + id);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
    }

    @Test
    @Order(15)
    public void testBatchMove() throws Exception {
        List<CaseReview> caseReviews = getCaseReviews("创建评审更新1");
        String moduleId = caseReviews.get(0).getModuleId();
        CaseReviewBatchRequest request = new CaseReviewBatchRequest();
        request.setProjectId(projectId);
        request.setMoveModuleId("CASE_REVIEW_REAL_MODULE_ID2");
        request.setSelectAll(false);
        this.requestPostWithOkAndReturn(BATCH_MOVE_CASE_REVIEW, request);
        request.setSelectAll(true);
        request.setExcludeIds(List.of(caseReviews.get(0).getId()));
        this.requestPostWithOkAndReturn(BATCH_MOVE_CASE_REVIEW, request);
        caseReviews = getCaseReviews("创建评审更新1");
        String moduleIdNew = caseReviews.get(0).getModuleId();
        Assertions.assertTrue(StringUtils.equals(moduleId, moduleIdNew));
        request = new CaseReviewBatchRequest();
        request.setProjectId(projectId);
        request.setMoveModuleId("CASE_REVIEW_REAL_MODULE_ID2");
        request.setSelectAll(false);
        this.requestPostWithOkAndReturn(BATCH_MOVE_CASE_REVIEW, request);
        caseReviews = getCaseReviews("创建评审更新1");
        String moduleIdNewOne = caseReviews.get(0).getModuleId();
        Assertions.assertTrue(StringUtils.equals(moduleIdNewOne, moduleIdNew));
    }

    @Test
    @Order(16)
    public void testDelete() throws Exception {
        List<CaseReview> caseReviews = getCaseReviews("创建评审更新2");
        delCaseReview(caseReviews.get(0).getId());

        delCaseReview("caseReviewIdX");
    }


    @Test
    @Order(17)
    public void testDisassociate() throws Exception {
        List<CaseReview> caseReviews = getCaseReviews("创建评审更新1");
        Assertions.assertEquals(1, caseReviews.size());
        String caseReviewId = caseReviews.get(0).getId();
        CaseReviewFunctionalCaseExample caseReviewFunctionalCaseExample = new CaseReviewFunctionalCaseExample();
        caseReviewFunctionalCaseExample.createCriteria().andReviewIdEqualTo(caseReviewId).andCaseIdEqualTo("CASE_REVIEW_TEST_GYQ_ID6");
        List<CaseReviewFunctionalCase> caseReviewFunctionalCases = caseReviewFunctionalCaseMapper.selectByExample(caseReviewFunctionalCaseExample);
        Assertions.assertEquals(1, caseReviewFunctionalCases.size());
        mockMvc.perform(MockMvcRequestBuilders.get(DISASSOCIATE_CASE_REVIEW+caseReviewId+"/CASE_REVIEW_TEST_GYQ_ID6").header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .header(SessionConstants.CURRENT_PROJECT, projectId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        caseReviewFunctionalCaseExample = new CaseReviewFunctionalCaseExample();
        caseReviewFunctionalCaseExample.createCriteria().andReviewIdEqualTo(caseReviewId).andCaseIdEqualTo("CASE_REVIEW_TEST_GYQ_ID6");
        caseReviewFunctionalCases = caseReviewFunctionalCaseMapper.selectByExample(caseReviewFunctionalCaseExample);
        Assertions.assertEquals(0, caseReviewFunctionalCases.size());

        caseReviews = getCaseReviews("创建评审3");
        Assertions.assertEquals(1, caseReviews.size());
        caseReviewId = caseReviews.get(0).getId();
        CaseReviewAssociateRequest caseReviewAssociateRequest = new CaseReviewAssociateRequest();
        caseReviewAssociateRequest.setProjectId(projectId);
        caseReviewAssociateRequest.setReviewId(caseReviewId);
        BaseAssociateCaseRequest baseAssociateCaseRequest = new BaseAssociateCaseRequest();
        baseAssociateCaseRequest.setProjectId(projectId);
        baseAssociateCaseRequest.setSelectAll(false);
        baseAssociateCaseRequest.setSelectIds(List.of("CASE_REVIEW_TEST_GYQ_ID2"));
        caseReviewAssociateRequest.setBaseAssociateCaseRequest(baseAssociateCaseRequest);
        List<String> userIds = new ArrayList<>();
        userIds.add("gyq_review_test");
        userIds.add("gyq_review_test2");
        caseReviewAssociateRequest.setReviewers(userIds);
        this.requestPostWithOk(ASSOCIATE_CASE_REVIEW, caseReviewAssociateRequest);

        mockMvc.perform(MockMvcRequestBuilders.get(DISASSOCIATE_CASE_REVIEW+caseReviewId+"/CASE_REVIEW_TEST_GYQ_ID2").header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .header(SessionConstants.CURRENT_PROJECT, projectId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        CaseReview caseReview = caseReviewMapper.selectByPrimaryKey(caseReviewId);
        Assertions.assertEquals(0, caseReview.getPassRate().compareTo(BigDecimal.ZERO));

    }

    @Test
    @Order(18)
    public void testDisassociateFalse() throws Exception {
        List<CaseReview> caseReviews = getCaseReviews("创建评审更新1");
        mockMvc.perform(MockMvcRequestBuilders.get(DISASSOCIATE_CASE_REVIEW+"caseReviewIdX"+"/CASE_REVIEW_TEST_GYQ_ID6").header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .header(SessionConstants.CURRENT_PROJECT, projectId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();

        String caseReviewId = caseReviews.get(0).getId();
        mockMvc.perform(MockMvcRequestBuilders.get(DISASSOCIATE_CASE_REVIEW+caseReviewId+"/CASE_REVIEW_TEST_GYQ_IDXX").header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .header(SessionConstants.CURRENT_PROJECT, projectId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();

    }

    /**
     * 生成高级搜索参数
     *
     * @return combine param
     */
    private Map<String, Object> buildRequestCombine() {
        Map<String, Object> map = new HashMap<>();
        map.put("reviewers", Map.of("operator", "in", "value", List.of("admin")));
        return map;
    }

    private void delCaseReview(String reviewId) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(DELETE_CASE_REVIEW+reviewId+"/"+projectId).header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .header(SessionConstants.CURRENT_PROJECT, projectId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
    }
}
