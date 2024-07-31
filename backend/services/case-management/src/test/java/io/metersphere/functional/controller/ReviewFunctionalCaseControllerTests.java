package io.metersphere.functional.controller;

import io.metersphere.functional.constants.CaseFileSourceType;
import io.metersphere.functional.constants.CaseReviewPassRule;
import io.metersphere.functional.constants.FunctionalCaseReviewStatus;
import io.metersphere.functional.domain.*;
import io.metersphere.functional.dto.CaseReviewHistoryDTO;
import io.metersphere.functional.mapper.CaseReviewFunctionalCaseMapper;
import io.metersphere.functional.mapper.CaseReviewHistoryMapper;
import io.metersphere.functional.mapper.CaseReviewMapper;
import io.metersphere.functional.mapper.FunctionalCaseAttachmentMapper;
import io.metersphere.functional.request.BaseAssociateCaseRequest;
import io.metersphere.functional.request.CaseReviewRequest;
import io.metersphere.functional.request.FunctionalCaseFileRequest;
import io.metersphere.functional.request.ReviewFunctionalCaseRequest;
import io.metersphere.functional.service.FunctionalCaseAttachmentService;
import io.metersphere.functional.service.ReviewFunctionalCaseService;
import io.metersphere.functional.utils.FileBaseUtils;
import io.metersphere.project.dto.filemanagement.request.FileUploadRequest;
import io.metersphere.project.service.FileMetadataService;
import io.metersphere.system.service.FileService;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.constants.StorageType;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
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

import java.nio.charset.StandardCharsets;
import java.util.*;

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

    public static final String ATTACHMENT_DOWNLOAD_URL = "/review/functional/case/download";
    public static final String UPLOAD_TEMP = "/review/functional/case/upload/temp/file";
    public static final String ATTACHMENT_PREVIEW_URL = "/review/functional/case/preview";
    public static final String DOWNLOAD_FILE = "/review/functional/case/download/file/%s/%s/%s";


    @Resource
    private CaseReviewMapper caseReviewMapper;
    @Resource
    private CaseReviewHistoryMapper caseReviewHistoryMapper;
    @Resource
    private CaseReviewFunctionalCaseMapper caseReviewFunctionalCaseMapper;
    @Resource
    private FileService fileService;
    @Resource
    private FileMetadataService fileMetadataService;
    @Resource
    private FunctionalCaseAttachmentService functionalCaseAttachmentService;
    @Resource
    private FunctionalCaseAttachmentMapper functionalCaseAttachmentMapper;
    @Resource
    private ReviewFunctionalCaseService reviewFunctionalCaseService;

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
        List<CaseReview> caseReviews = addReview("创建用例评审1", caseIds, reviewers, CaseReviewPassRule.SINGLE.toString());
        String reviewId = caseReviews.getFirst().getId();
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
        caseReviewHistoryExample.createCriteria().andReviewIdEqualTo(reviewId).andCaseIdEqualTo("gyqReviewCaseTest").andAbandonedEqualTo(false);
        List<CaseReviewHistory> caseReviewHistories = caseReviewHistoryMapper.selectByExample(caseReviewHistoryExample);
        Assertions.assertEquals(1, caseReviewHistories.size());
        CaseReviewFunctionalCaseExample caseReviewFunctionalCaseExample = new CaseReviewFunctionalCaseExample();
        caseReviewFunctionalCaseExample.createCriteria().andReviewIdEqualTo(reviewId).andCaseIdEqualTo("gyqReviewCaseTest");
        List<CaseReviewFunctionalCase> caseReviewFunctionalCases = caseReviewFunctionalCaseMapper.selectByExample(caseReviewFunctionalCaseExample);
        Assertions.assertTrue(StringUtils.equalsIgnoreCase(caseReviewFunctionalCases.getFirst().getStatus(), FunctionalCaseReviewStatus.PASS.toString()));
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
        caseReviewHistoryExample.createCriteria().andReviewIdEqualTo(reviewId).andCaseIdEqualTo("gyqReviewCaseTestOne").andAbandonedEqualTo(false);
        caseReviewHistories = caseReviewHistoryMapper.selectByExample(caseReviewHistoryExample);
        Assertions.assertEquals(1, caseReviewHistories.size());
        caseReviewFunctionalCaseExample = new CaseReviewFunctionalCaseExample();
        caseReviewFunctionalCaseExample.createCriteria().andReviewIdEqualTo(reviewId).andCaseIdEqualTo("gyqReviewCaseTestOne");
        caseReviewFunctionalCases = caseReviewFunctionalCaseMapper.selectByExample(caseReviewFunctionalCaseExample);
        Assertions.assertTrue(StringUtils.equalsIgnoreCase(caseReviewFunctionalCases.getFirst().getStatus(), FunctionalCaseReviewStatus.UN_PASS.toString()));


        reviewFunctionalCaseRequest = new ReviewFunctionalCaseRequest();
        reviewFunctionalCaseRequest.setReviewId(reviewId);
        reviewFunctionalCaseRequest.setCaseId("gyqReviewCaseTestOne");
        reviewFunctionalCaseRequest.setProjectId(projectId);
        reviewFunctionalCaseRequest.setStatus(FunctionalCaseReviewStatus.RE_REVIEWED.toString());
        reviewFunctionalCaseRequest.setContent("不通过");
        reviewFunctionalCaseRequest.setNotifier("default-project-member-user-gyq-2;");
        reviewFunctionalCaseRequest.setReviewPassRule(CaseReviewPassRule.SINGLE.toString());
        this.requestPostWithOk(SAVE_REVIEW, reviewFunctionalCaseRequest);
        caseReviewHistoryExample = new CaseReviewHistoryExample();
        caseReviewHistoryExample.createCriteria().andReviewIdEqualTo(reviewId).andCaseIdEqualTo("gyqReviewCaseTestOne").andAbandonedEqualTo(false);
        caseReviewHistories = caseReviewHistoryMapper.selectByExample(caseReviewHistoryExample);
        Assertions.assertEquals(2, caseReviewHistories.size());
        caseReviewFunctionalCaseExample = new CaseReviewFunctionalCaseExample();
        caseReviewFunctionalCaseExample.createCriteria().andReviewIdEqualTo(reviewId).andCaseIdEqualTo("gyqReviewCaseTestOne");
        caseReviewFunctionalCases = caseReviewFunctionalCaseMapper.selectByExample(caseReviewFunctionalCaseExample);
        Assertions.assertTrue(StringUtils.equalsIgnoreCase(caseReviewFunctionalCases.getFirst().getStatus(), FunctionalCaseReviewStatus.RE_REVIEWED.toString()));

        reviewFunctionalCaseRequest = new ReviewFunctionalCaseRequest();
        reviewFunctionalCaseRequest.setReviewId(reviewId);
        reviewFunctionalCaseRequest.setCaseId("gyqReviewCaseTestOne");
        reviewFunctionalCaseRequest.setProjectId(projectId);
        reviewFunctionalCaseRequest.setStatus(FunctionalCaseReviewStatus.PASS.toString());
        reviewFunctionalCaseRequest.setContent("不通过");
        reviewFunctionalCaseRequest.setNotifier("default-project-member-user-gyq-2;");
        reviewFunctionalCaseRequest.setReviewPassRule(CaseReviewPassRule.SINGLE.toString());
        reviewFunctionalCaseService.saveReview(reviewFunctionalCaseRequest, "default-project-member-user-gyq-4");
        caseReviewFunctionalCaseExample.createCriteria().andReviewIdEqualTo(reviewId).andCaseIdEqualTo("gyqReviewCaseTestOne");
        caseReviewFunctionalCases = caseReviewFunctionalCaseMapper.selectByExample(caseReviewFunctionalCaseExample);
        Assertions.assertTrue(StringUtils.equalsIgnoreCase(caseReviewFunctionalCases.getFirst().getStatus(), FunctionalCaseReviewStatus.RE_REVIEWED.toString()));

        reviewFunctionalCaseRequest = new ReviewFunctionalCaseRequest();
        reviewFunctionalCaseRequest.setReviewId(reviewId);
        reviewFunctionalCaseRequest.setCaseId("gyqReviewCaseTestOne");
        reviewFunctionalCaseRequest.setProjectId(projectId);
        reviewFunctionalCaseRequest.setStatus(FunctionalCaseReviewStatus.UNDER_REVIEWED.toString());
        reviewFunctionalCaseRequest.setContent("不通过");
        reviewFunctionalCaseRequest.setNotifier("default-project-member-user-gyq-2;");
        reviewFunctionalCaseRequest.setReviewPassRule(CaseReviewPassRule.SINGLE.toString());
        reviewFunctionalCaseService.saveReview(reviewFunctionalCaseRequest, "admin");
        caseReviewFunctionalCaseExample.createCriteria().andReviewIdEqualTo(reviewId).andCaseIdEqualTo("gyqReviewCaseTestOne");
        caseReviewFunctionalCases = caseReviewFunctionalCaseMapper.selectByExample(caseReviewFunctionalCaseExample);
        Assertions.assertTrue(StringUtils.equalsIgnoreCase(caseReviewFunctionalCases.getFirst().getStatus(), FunctionalCaseReviewStatus.RE_REVIEWED.toString()));
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
        List<CaseReview> caseReviews = addReview("创建用例评审2", caseIds, reviewers, CaseReviewPassRule.MULTIPLE.toString());
        String reviewId = caseReviews.getFirst().getId();
        CaseReviewHistory caseReviewHistory = new CaseReviewHistory();
        caseReviewHistory.setReviewId(reviewId);
        caseReviewHistory.setCaseId("gyqReviewCaseTestTwo");
        caseReviewHistory.setCreateUser("system");
        caseReviewHistory.setStatus("RE_REVIEWED");
        caseReviewHistory.setId(IDGenerator.nextStr());
        caseReviewHistory.setCreateTime(System.currentTimeMillis());
        caseReviewHistoryMapper.insertSelective(caseReviewHistory);
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
        caseReviewHistoryExample.createCriteria().andReviewIdEqualTo(reviewId).andCaseIdEqualTo("gyqReviewCaseTestTwo").andAbandonedEqualTo(false);
        List<CaseReviewHistory> caseReviewHistories = caseReviewHistoryMapper.selectByExample(caseReviewHistoryExample);
        Assertions.assertEquals(2, caseReviewHistories.size());
        CaseReviewFunctionalCaseExample caseReviewFunctionalCaseExample = new CaseReviewFunctionalCaseExample();
        caseReviewFunctionalCaseExample.createCriteria().andReviewIdEqualTo(reviewId).andCaseIdEqualTo("gyqReviewCaseTestTwo");
        List<CaseReviewFunctionalCase> caseReviewFunctionalCases = caseReviewFunctionalCaseMapper.selectByExample(caseReviewFunctionalCaseExample);
        Assertions.assertTrue(StringUtils.equalsIgnoreCase(caseReviewFunctionalCases.getFirst().getStatus(), FunctionalCaseReviewStatus.UNDER_REVIEWED.toString()));
        List<CaseReview> caseReviews1 = getCaseReviews("创建用例评审2");
        System.out.println(caseReviews1.getFirst().getStatus());


        reviewFunctionalCaseRequest = new ReviewFunctionalCaseRequest();
        reviewFunctionalCaseRequest.setReviewId(reviewId);
        reviewFunctionalCaseRequest.setCaseId("gyqReviewCaseTestTwo");
        reviewFunctionalCaseRequest.setProjectId(projectId);
        reviewFunctionalCaseRequest.setStatus(FunctionalCaseReviewStatus.UN_PASS.toString());
        reviewFunctionalCaseRequest.setContent("通过了");
        reviewFunctionalCaseRequest.setNotifier("default-project-member-user-gyq-2");
        reviewFunctionalCaseRequest.setReviewPassRule(CaseReviewPassRule.MULTIPLE.toString());
        reviewFunctionalCaseService.saveReview(reviewFunctionalCaseRequest, "default-project-member-user-gyq-4");
        try {
            reviewFunctionalCaseService.saveReview(reviewFunctionalCaseRequest, "default-project-member-user-gyq-s");

        } catch (Exception e) {
            Assertions.assertTrue(StringUtils.equalsIgnoreCase(e.getMessage(), Translator.get("case_review_user")));
        }


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
        List<CaseReview> caseReviews = addReview("创建用例评审3", caseIds, reviewers, CaseReviewPassRule.MULTIPLE.toString());
        String reviewId = caseReviews.getFirst().getId();
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
        caseReviewHistoryExample.createCriteria().andReviewIdEqualTo(reviewId).andCaseIdEqualTo("gyqReviewCaseTestThree").andAbandonedEqualTo(false);
        List<CaseReviewHistory> caseReviewHistories = caseReviewHistoryMapper.selectByExample(caseReviewHistoryExample);
        Assertions.assertEquals(1, caseReviewHistories.size());
        CaseReviewFunctionalCaseExample caseReviewFunctionalCaseExample = new CaseReviewFunctionalCaseExample();
        caseReviewFunctionalCaseExample.createCriteria().andReviewIdEqualTo(reviewId).andCaseIdEqualTo("gyqReviewCaseTestThree");
        List<CaseReviewFunctionalCase> caseReviewFunctionalCases = caseReviewFunctionalCaseMapper.selectByExample(caseReviewFunctionalCaseExample);
        Assertions.assertTrue(StringUtils.equalsIgnoreCase(caseReviewFunctionalCases.getFirst().getStatus(), FunctionalCaseReviewStatus.PASS.toString()));

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
        caseReviewHistoryExample.createCriteria().andReviewIdEqualTo(reviewId).andCaseIdEqualTo("gyqReviewCaseTestFour").andAbandonedEqualTo(false);
        caseReviewHistories = caseReviewHistoryMapper.selectByExample(caseReviewHistoryExample);
        Assertions.assertEquals(1, caseReviewHistories.size());
        caseReviewFunctionalCaseExample = new CaseReviewFunctionalCaseExample();
        caseReviewFunctionalCaseExample.createCriteria().andReviewIdEqualTo(reviewId).andCaseIdEqualTo("gyqReviewCaseTestFour");
        caseReviewFunctionalCases = caseReviewFunctionalCaseMapper.selectByExample(caseReviewFunctionalCaseExample);
        Assertions.assertTrue(StringUtils.equalsIgnoreCase(caseReviewFunctionalCases.getFirst().getStatus(), FunctionalCaseReviewStatus.UN_PASS.toString()));


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
        List<CaseReview> caseReviews = addReview("创建用例评审4", caseIds, reviewers, CaseReviewPassRule.SINGLE.toString());
        String reviewId = caseReviews.getFirst().getId();
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
        String reviewId = caseReviews.getFirst().getId();
        CaseReviewHistory caseReviewHistory = new CaseReviewHistory();
        caseReviewHistory.setReviewId(reviewId);
        caseReviewHistory.setCaseId("gyqReviewCaseTest");
        caseReviewHistory.setCreateUser("system");
        caseReviewHistory.setStatus(FunctionalCaseReviewStatus.RE_REVIEWED.toString());
        caseReviewHistory.setId("testList");
        caseReviewHistory.setCreateTime(System.currentTimeMillis());
        caseReviewHistoryMapper.insertSelective(caseReviewHistory);
        List<CaseReviewHistoryDTO> gyqReviewCaseTest = getCaseReviewHistoryList("gyqReviewCaseTest", reviewId);
        System.out.println(JSON.toJSONString(gyqReviewCaseTest));
    }

    @Test
    @Order(5)
    public void testAttachmentPreview() throws Exception {
        FunctionalCaseFileRequest request = new FunctionalCaseFileRequest();
        request.setProjectId("project-review-case-test");
        request.setLocal(true);
        request.setFileId("TEST_REVIEW_COMMENT_FILE_ID");
        request.setCaseId("gyqReviewCaseTest");
        uploadLocalFile();
        this.downloadFile(ATTACHMENT_PREVIEW_URL, request);

        //覆盖controller
        request.setLocal(false);
        String fileId = uploadFile();
        request.setFileId(fileId);
        request.setProjectId(DEFAULT_PROJECT_ID);
        this.downloadFile(ATTACHMENT_PREVIEW_URL, request);

        //增加覆盖率
        request.setLocal(true);
        request.setProjectId("123213");
        request.setFileId("123123");
        request.setCaseId("123123");
        this.downloadFile(ATTACHMENT_PREVIEW_URL, request);
    }

    @Test
    @Order(6)
    public void testUploadTemp() throws Exception {
        //覆盖controller方法
        MockMultipartFile file = getMockMultipartFile();
        String fileId = doUploadTempFile(file);
        Assertions.assertTrue(StringUtils.isNotBlank(fileId));
        file = getNoNameMockMultipartFile();
        doUploadTempFileFalse(file);
        functionalCaseAttachmentService.uploadMinioFile("gyqReviewCaseTest", "project-review-case-test", List.of(fileId), "admin", CaseFileSourceType.REVIEW_COMMENT.toString());
        FunctionalCaseAttachmentExample functionalCaseAttachmentExample = new FunctionalCaseAttachmentExample();
        functionalCaseAttachmentExample.createCriteria().andCaseIdEqualTo("gyqReviewCaseTest").andFileIdEqualTo(fileId).andFileSourceEqualTo(CaseFileSourceType.REVIEW_COMMENT.toString());
        List<FunctionalCaseAttachment> functionalCaseAttachments = functionalCaseAttachmentMapper.selectByExample(functionalCaseAttachmentExample);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(functionalCaseAttachments));
        functionalCaseAttachmentService.uploadMinioFile("gyqReviewCaseTest", "project-review-case-test", new ArrayList<>(), "admin", CaseFileSourceType.REVIEW_COMMENT.toString());
        String functionalCaseDir = DefaultRepositoryDir.getFunctionalCaseDir("project-review-case-test", "gyqReviewCaseTest");
        functionalCaseAttachmentService.uploadFileResource(functionalCaseDir, new HashMap<>(), "project-review-case-test", "gyqReviewCaseTest");
        Map<String, String> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put(fileId, null);
        functionalCaseAttachmentService.uploadFileResource(functionalCaseDir, objectObjectHashMap, "project-review-case-test", "gyqReviewCaseTest");

    }

    @Test
    @Order(7)
    public void testAttachmentDownload() throws Exception {
        //覆盖controller
        FunctionalCaseFileRequest request = new FunctionalCaseFileRequest();
        request.setProjectId("project-review-case-test");
        request.setFileId("TEST_REVIEW_COMMENT_FILE_ID");
        request.setCaseId("gyqReviewCaseTest");
        request.setLocal(true);
        this.downloadFile(ATTACHMENT_DOWNLOAD_URL, request);
    }

    @Test
    @Order(8)
    public void downTemp() throws Exception {
        MockMultipartFile file = getMockMultipartFile();
        String fileId = doUploadTempFile(file);
        Assertions.assertTrue(org.testcontainers.shaded.org.apache.commons.lang3.StringUtils.isNotBlank(fileId));
        MvcResult compressedResult = this.downloadTempFile(String.format(DOWNLOAD_FILE, "project-review-case-test", fileId, false));
        Assertions.assertTrue(compressedResult.getResponse().getContentAsByteArray().length > 0);
        compressedResult = this.downloadTempFile(String.format(DOWNLOAD_FILE, "project-review-case-test", fileId, true));
        Assertions.assertTrue(compressedResult.getResponse().getContentAsByteArray().length > 0);
        functionalCaseAttachmentService.uploadMinioFile("gyqReviewCaseTest", "project-review-case-test", List.of(fileId), "admin", CaseFileSourceType.CASE_DETAIL.toString());
        compressedResult = this.downloadTempFile(String.format(DOWNLOAD_FILE, "project-review-case-test", fileId, false));
        Assertions.assertTrue(compressedResult.getResponse().getContentAsByteArray().length > 0);
        compressedResult = this.downloadTempFile(String.format(DOWNLOAD_FILE, "project-review-case-test", fileId, true));
        Assertions.assertTrue(compressedResult.getResponse().getContentAsByteArray().length > 0);
    }

    protected MvcResult downloadTempFile(String url, Object... uriVariables) throws Exception {
        return mockMvc.perform(getRequestBuilder(url, uriVariables))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(status().isOk()).andReturn();
    }


    public List<CaseReviewHistoryDTO> getCaseReviewHistoryList(String caseId, String reviewId) throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(REVIEW_LIST + "/" + reviewId + "/" + caseId).header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .header(SessionConstants.CURRENT_PROJECT, projectId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        return JSON.parseArray(JSON.toJSONString(resultHolder.getData()), CaseReviewHistoryDTO.class);

    }

    private List<CaseReview> addReview(String name, List<String> caseIds, List<String> reviewers, String reviewPassRule) throws Exception {
        CaseReviewRequest caseReviewRequest = new CaseReviewRequest();
        caseReviewRequest.setProjectId(projectId);
        caseReviewRequest.setName(name);
        caseReviewRequest.setModuleId("CASE_REVIEW_REAL_MODULE_ID");
        caseReviewRequest.setReviewPassRule(reviewPassRule);
        caseReviewRequest.setReviewers(reviewers);
        BaseAssociateCaseRequest baseAssociateCaseRequest = new BaseAssociateCaseRequest();
        baseAssociateCaseRequest.setSelectAll(false);
        baseAssociateCaseRequest.setProjectId(projectId);
        baseAssociateCaseRequest.setSelectIds(caseIds);
        baseAssociateCaseRequest.setReviewers(reviewers);
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

    protected MvcResult downloadFile(String url, Object param, Object... uriVariables) throws Exception {
        return mockMvc.perform(getPostRequestBuilder(url, param, uriVariables))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(status().isOk()).andReturn();
    }

    private void uploadLocalFile() {
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/test.JPG")).getPath();
        MockMultipartFile file = new MockMultipartFile("file", "file_re-upload.JPG", MediaType.APPLICATION_OCTET_STREAM_VALUE, FileBaseUtils.getFileBytes(filePath));
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFileName("测试评审");
        fileRequest.setFolder(DefaultRepositoryDir.getFunctionalCaseDir("project-review-case-test", "gyqReviewCaseTest") + "/" + "TEST_REVIEW_COMMENT_FILE_ID");
        fileRequest.setStorage(StorageType.MINIO.name());
        try {
            fileService.upload(file, fileRequest);
        } catch (Exception e) {
            throw new MSException("save file error");
        }
    }

    private String uploadFile() throws Exception {
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/test.JPG")).getPath();
        MockMultipartFile file = new MockMultipartFile("file", "file_re-upload.JPG", MediaType.APPLICATION_OCTET_STREAM_VALUE, FileBaseUtils.getFileBytes(filePath));
        FileUploadRequest fileUploadRequest = new FileUploadRequest();
        fileUploadRequest.setProjectId("project-review-case-test");
        return fileMetadataService.upload(fileUploadRequest, "admin", file);
    }

    private static MockMultipartFile getMockMultipartFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "file_upload.JPG",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "Hello, World!".getBytes()
        );
        return file;
    }

    private String doUploadTempFile(MockMultipartFile file) throws Exception {
        return JSON.parseObject(requestUploadFileWithOkAndReturn(UPLOAD_TEMP, file)
                        .getResponse()
                        .getContentAsString(), ResultHolder.class)
                .getData().toString();
    }

    private void doUploadTempFileFalse(MockMultipartFile file) throws Exception {
        this.requestUploadFile(UPLOAD_TEMP, file).andExpect(status().is5xxServerError());
    }

    private static MockMultipartFile getNoNameMockMultipartFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                null,
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "Hello, World!".getBytes()
        );
        return file;
    }
}
