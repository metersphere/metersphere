package io.metersphere.dashboard.controller;

import io.metersphere.api.dto.definition.ApiDefinitionUpdateDTO;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.bug.domain.Bug;
import io.metersphere.bug.domain.BugExample;
import io.metersphere.bug.mapper.BugMapper;
import io.metersphere.bug.service.BugStatusService;
import io.metersphere.dashboard.constants.DashboardUserLayoutKeys;
import io.metersphere.dashboard.dto.LayoutDTO;
import io.metersphere.dashboard.request.DashboardFrontPageRequest;
import io.metersphere.dashboard.response.OverViewCountDTO;
import io.metersphere.dashboard.response.StatisticsDTO;
import io.metersphere.dashboard.service.DashboardService;
import io.metersphere.functional.dto.CaseReviewDTO;
import io.metersphere.plugin.platform.dto.SelectOption;
import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectApplication;
import io.metersphere.project.domain.ProjectApplicationExample;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.dto.ProjectUserDTO;
import io.metersphere.project.mapper.ProjectApplicationMapper;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.request.ProjectMemberRequest;
import io.metersphere.project.service.ProjectMemberService;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BasePluginTestService;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.Header;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DashboardFrontPageControllerTests extends BaseTest {

    @Resource
    private DashboardService dashboardService;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private ProjectApplicationMapper projectApplicationMapper;

    @Resource
    private ProjectMemberService projectMemberService;
    @Resource
    private BugStatusService bugStatusService;
    @Resource
    private BugMapper bugMapper;
    @Resource
    private BasePluginTestService basePluginTestService;
    @Resource
    private MockServerClient mockServerClient;
    @Value("${embedded.mockserver.host}")
    private String mockServerHost;
    @Value("${embedded.mockserver.port}")
    private int mockServerHostPort;

    private static final String EDIT_LAYOUT = "/dashboard/layout/edit/";
    private static final String GET_LAYOUT = "/dashboard/layout/get/";


    private static final String CREATE_BY_ME = "/dashboard/create_by_me";
    private static final String PROJECT_VIEW = "/dashboard/project_view";
    private static final String PROJECT_MEMBER_VIEW = "/dashboard/project_member_view";
    private static final String CASE_COUNT = "/dashboard/case_count";
    private static final String ASSOCIATE_CASE_COUNT = "/dashboard/associate_case_count";
    private static final String REVIEW_CASE_COUNT = "/dashboard/review_case_count";
    private static final String BUG_HANDLE_USER = "/dashboard/bug_handle_user";
    private static final String API_COUNT = "/dashboard/api_count";
    private static final String API_CASE_COUNT = "/dashboard/api_case_count";
    private static final String SCENARIO_COUNT = "/dashboard/scenario_count";
    private static final String BUG_COUNT = "/dashboard/bug_count";
    private static final String CREATE_BUG_BY_ME = "/dashboard/create_bug_by_me";
    private static final String HANDLE_BUG_BY_ME = "/dashboard/handle_bug_by_me";



    private static final String REVIEWING_BY_ME = "/dashboard/reviewing_by_me";
    private static final String API_CHANGE = "/dashboard/api_change";
    private static final String BUG_HANDLE_USER_LIST = "/dashboard/bug_handle_user/list/";

    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_dashboard.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testSql() throws Exception {
        DashboardFrontPageRequest dashboardFrontPageRequest = new DashboardFrontPageRequest();
        dashboardFrontPageRequest.setOrganizationId(DEFAULT_ORGANIZATION_ID);
        dashboardFrontPageRequest.setDayNumber(3);
        dashboardFrontPageRequest.setCurrent(1);
        dashboardFrontPageRequest.setPageSize(5);
        dashboardFrontPageRequest.setProjectIds(List.of(DEFAULT_PROJECT_ID));
        MvcResult bugMvcResult = this.requestPostWithOkAndReturn(BUG_HANDLE_USER, dashboardFrontPageRequest);
        String bugContentAsString = bugMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder bugResultHolder = JSON.parseObject(bugContentAsString, ResultHolder.class);
        OverViewCountDTO bugCount = JSON.parseObject(JSON.toJSONString(bugResultHolder.getData()), OverViewCountDTO.class);
        Assertions.assertNotNull(bugCount);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(CREATE_BY_ME, dashboardFrontPageRequest);
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        OverViewCountDTO moduleCount = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), OverViewCountDTO.class);
        Assertions.assertNotNull(moduleCount);
        MvcResult mvcResultAll = this.requestPostWithOkAndReturn(PROJECT_VIEW, dashboardFrontPageRequest);
        OverViewCountDTO moduleCountAll = ApiDataUtils.parseObject(JSON.toJSONString(parseResponse(mvcResultAll).get("data")), OverViewCountDTO.class);
        Assertions.assertNotNull(moduleCountAll);
        dashboardFrontPageRequest.setDayNumber(null);
        dashboardFrontPageRequest.setStartTime(1716185577387L);
        dashboardFrontPageRequest.setEndTime(1730181702699L);
        dashboardFrontPageRequest.setProjectIds(new ArrayList<>());
        mvcResult = this.requestPostWithOkAndReturn(CREATE_BY_ME, dashboardFrontPageRequest);
        moduleCount = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                OverViewCountDTO.class);
        Assertions.assertNotNull(moduleCount);
        mvcResultAll = this.requestPostWithOkAndReturn(PROJECT_VIEW, dashboardFrontPageRequest);
        moduleCountAll = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(mvcResultAll.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                OverViewCountDTO.class);
        Assertions.assertNotNull(moduleCountAll);
        OverViewCountDTO gyq = dashboardService.createByMeCount(dashboardFrontPageRequest, "gyq");
        Assertions.assertNotNull(gyq);

        OverViewCountDTO gyq1 = dashboardService.projectViewCount(dashboardFrontPageRequest, "gyq");
        Assertions.assertNotNull(gyq1);

        OverViewCountDTO gyq2 = dashboardService.projectViewCount(dashboardFrontPageRequest, "default-dashboard-member-user-gyq");
        Assertions.assertNotNull(gyq2);

        OverViewCountDTO gyq3 = dashboardService.projectViewCount(dashboardFrontPageRequest, "default-dashboard-member-user-gyq");
        Assertions.assertNotNull(gyq3);

        dashboardFrontPageRequest.setProjectIds(List.of(DEFAULT_PROJECT_ID));
        mvcResultAll = this.requestPostWithOkAndReturn(PROJECT_MEMBER_VIEW, dashboardFrontPageRequest);
        moduleCountAll = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(mvcResultAll.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                OverViewCountDTO.class);
        Assertions.assertNotNull(moduleCountAll);
        List<SelectOption> headerStatusOption = bugStatusService.getHeaderStatusOption(DEFAULT_PROJECT_ID);
        buildBug(headerStatusOption);
        bugMvcResult = this.requestPostWithOkAndReturn(BUG_HANDLE_USER, dashboardFrontPageRequest);
        bugContentAsString = bugMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        bugResultHolder = JSON.parseObject(bugContentAsString, ResultHolder.class);
        bugCount = JSON.parseObject(JSON.toJSONString(bugResultHolder.getData()), OverViewCountDTO.class);
        Assertions.assertNotNull(bugCount);

        Project project = new Project();
        project.setModuleSetting("[]");
        project.setId(DEFAULT_PROJECT_ID);
        projectMapper.updateByPrimaryKeySelective(project);
        OverViewCountDTO gyq4 = dashboardService.projectViewCount(dashboardFrontPageRequest, "default-dashboard-member-user-gyq");
        Assertions.assertTrue(gyq4.getXAxis().isEmpty());
        OverViewCountDTO gyq5 = dashboardService.projectBugHandleUser(dashboardFrontPageRequest, "admin");
        Assertions.assertTrue(gyq5.getXAxis().isEmpty());
        project.setModuleSetting("[\"apiTest\",\"testPlan\",\"caseManagement\",\"bugManagement\"]");
        project.setId(DEFAULT_PROJECT_ID);
        projectMapper.updateByPrimaryKeySelective(project);
    }

    private void buildBug(List<SelectOption> headerStatusOption) {
        BugExample bugExample = new BugExample();
        bugExample.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID);
        List<Bug> bugs = bugMapper.selectByExample(bugExample);
        for (int i = 0; i < bugs.size(); i++) {
            Bug bug = new Bug();
            bug.setId(bugs.get(i).getId());
            bug.setStatus(headerStatusOption.get(i).getValue());
            bugMapper.updateByPrimaryKeySelective(bug);
        }
    }

    @Test
    @Order(2)
    public void testLayout() throws Exception {
        MvcResult mvcResultGrt = this.requestGetWithOkAndReturn(GET_LAYOUT + "DEFAULT_ORGANIZATION_ID");
        String contentAsString = mvcResultGrt.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        List<LayoutDTO> layoutDTOS = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), LayoutDTO.class);
        Assertions.assertEquals(3, layoutDTOS.size());

        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andOrganizationIdEqualTo(DEFAULT_ORGANIZATION_ID);
        List<Project> projects = projectMapper.selectByExample(projectExample);

        ProjectMemberRequest projectMemberRequest = new ProjectMemberRequest();
        projectMemberRequest.setProjectId(DEFAULT_PROJECT_ID);
        projectMemberRequest.setCurrent(1);
        projectMemberRequest.setPageSize(5);
        List<ProjectUserDTO> projectUserDTOS = projectMemberService.listMember(projectMemberRequest);

        List<LayoutDTO> layoutDTO = new ArrayList<>();
        LayoutDTO layoutDTOa = new LayoutDTO();
        layoutDTOa.setId(UUID.randomUUID().toString());
        layoutDTOa.setPos(3);
        layoutDTOa.setKey(DashboardUserLayoutKeys.PROJECT_VIEW.toString());
        layoutDTOa.setLabel("项目概览");
        layoutDTOa.setProjectIds(new ArrayList<>());
        layoutDTOa.setHandleUsers(new ArrayList<>());
        layoutDTOa.setFullScreen(false);
        layoutDTO.add(layoutDTOa);

        LayoutDTO layoutDTOb = new LayoutDTO();
        layoutDTOb.setId(UUID.randomUUID().toString());
        layoutDTOb.setPos(4);
        layoutDTOb.setKey(DashboardUserLayoutKeys.CREATE_BY_ME.toString());
        layoutDTOb.setLabel("我的创建");
        layoutDTOb.setProjectIds(projects.stream().map(Project::getId).toList());
        layoutDTOb.setHandleUsers(new ArrayList<>());
        layoutDTOb.setFullScreen(false);
        layoutDTO.add(layoutDTOb);

        List<String> userIds = projectUserDTOS.stream().map(ProjectUserDTO::getId).toList();

        LayoutDTO layoutDTOc = new LayoutDTO();
        layoutDTOc.setId(UUID.randomUUID().toString());
        layoutDTOc.setPos(4);
        layoutDTOc.setKey(DashboardUserLayoutKeys.PROJECT_MEMBER_VIEW.toString());
        layoutDTOc.setLabel("人员概览");
        layoutDTOc.setProjectIds(List.of(DEFAULT_PROJECT_ID));
        layoutDTOc.setHandleUsers(userIds);
        layoutDTOc.setFullScreen(false);
        layoutDTO.add(layoutDTOc);

        LayoutDTO layoutDTO1 = new LayoutDTO();
        layoutDTO1.setId(UUID.randomUUID().toString());
        layoutDTO1.setPos(1);
        layoutDTO1.setKey(DashboardUserLayoutKeys.CASE_COUNT.toString());
        layoutDTO1.setLabel("用例数量");
        layoutDTO1.setProjectIds(new ArrayList<>());
        layoutDTO1.setHandleUsers(new ArrayList<>());
        layoutDTO1.setFullScreen(false);
        layoutDTO.add(layoutDTO1);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(EDIT_LAYOUT + DEFAULT_ORGANIZATION_ID, layoutDTO);
        contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        layoutDTOS = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), LayoutDTO.class);
        Assertions.assertNotNull(layoutDTOS);

        mvcResultGrt = this.requestGetWithOkAndReturn(GET_LAYOUT + DEFAULT_ORGANIZATION_ID);
        contentAsString = mvcResultGrt.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        layoutDTOS = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), LayoutDTO.class);
        Assertions.assertNotNull(layoutDTOS);

        LayoutDTO layoutDTO2 = new LayoutDTO();
        layoutDTO2.setId(UUID.randomUUID().toString());
        layoutDTO2.setPos(2);
        layoutDTO2.setKey(DashboardUserLayoutKeys.ASSOCIATE_CASE_COUNT.toString());
        layoutDTO2.setLabel("关联用例数量");
        layoutDTO2.setProjectIds(List.of(DEFAULT_PROJECT_ID));
        layoutDTO2.setHandleUsers(new ArrayList<>());
        layoutDTO2.setFullScreen(false);
        layoutDTO.add(layoutDTO1);
        mvcResult = this.requestPostWithOkAndReturn(EDIT_LAYOUT + DEFAULT_ORGANIZATION_ID, layoutDTO);
        contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        layoutDTOS = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), LayoutDTO.class);
        Assertions.assertNotNull(layoutDTOS);
    }

    @Test
    @Order(3)
    public void testOther() throws Exception {
        basePluginTestService.addJiraPlugin();
        basePluginTestService.addServiceIntegration(DEFAULT_ORGANIZATION_ID);
        mockServerClient
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/rest/api/2/search"))
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeaders(
                                        new Header("Content-Type", "application/json; charset=utf-8"),
                                        new Header("Cache-Control", "public, max-age=86400"))
                                .withBody("{\"id\":\"123456\",\"name\":\"test\", \"issues\": [], \"total\": 1}")

                );
        DashboardFrontPageRequest dashboardFrontPageRequest = new DashboardFrontPageRequest();
        dashboardFrontPageRequest.setOrganizationId(DEFAULT_ORGANIZATION_ID);
        dashboardFrontPageRequest.setDayNumber(null);
        dashboardFrontPageRequest.setStartTime(1697971947000L);
        dashboardFrontPageRequest.setEndTime(1700650347000L);
        dashboardFrontPageRequest.setCurrent(1);
        dashboardFrontPageRequest.setPageSize(5);
        dashboardFrontPageRequest.setProjectIds(List.of(DEFAULT_PROJECT_ID));
        List<SelectOption> headerStatusOption = bugStatusService.getHeaderStatusOption(DEFAULT_PROJECT_ID);
        buildBug(headerStatusOption);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(CASE_COUNT, dashboardFrontPageRequest);
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        StatisticsDTO moduleCount = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), StatisticsDTO.class);
        Assertions.assertNotNull(moduleCount);
        MvcResult apiMvcResult = this.requestPostWithOkAndReturn(API_COUNT, dashboardFrontPageRequest);
        String apiContentAsString = apiMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder apiResultHolder = JSON.parseObject(apiContentAsString, ResultHolder.class);
        StatisticsDTO apiCount = JSON.parseObject(JSON.toJSONString(apiResultHolder.getData()), StatisticsDTO.class);
        Assertions.assertNotNull(apiCount);
        MvcResult apiCaseMvcResult = this.requestPostWithOkAndReturn(API_CASE_COUNT, dashboardFrontPageRequest);
        String apiCaseContentAsString = apiCaseMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder apiCaseResultHolder = JSON.parseObject(apiCaseContentAsString, ResultHolder.class);
        StatisticsDTO apiCaseCount = JSON.parseObject(JSON.toJSONString(apiCaseResultHolder.getData()), StatisticsDTO.class);
        Assertions.assertNotNull(apiCaseCount);
        MvcResult scenarioMvcResult = this.requestPostWithOkAndReturn(SCENARIO_COUNT, dashboardFrontPageRequest);
        String scenarioContentAsString = scenarioMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder scenarioResultHolder = JSON.parseObject(scenarioContentAsString, ResultHolder.class);
        StatisticsDTO scenarioCount = JSON.parseObject(JSON.toJSONString(scenarioResultHolder.getData()), StatisticsDTO.class);
        Assertions.assertNotNull(scenarioCount);
        MvcResult associateMvcResult = this.requestPostWithOkAndReturn(ASSOCIATE_CASE_COUNT, dashboardFrontPageRequest);
        String associateContent = associateMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder associateResultHolder = JSON.parseObject(associateContent, ResultHolder.class);
        StatisticsDTO associateCount = JSON.parseObject(JSON.toJSONString(associateResultHolder.getData()), StatisticsDTO.class);
        Assertions.assertNotNull(associateCount);
        MvcResult reviewMvcResult = this.requestPostWithOkAndReturn(REVIEW_CASE_COUNT, dashboardFrontPageRequest);
        String reviewContent = reviewMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder reviewResultHolder = JSON.parseObject(reviewContent, ResultHolder.class);
        StatisticsDTO reviewCount = JSON.parseObject(JSON.toJSONString(reviewResultHolder.getData()), StatisticsDTO.class);
        Assertions.assertNotNull(reviewCount);
        enableDefaultPlatformConfig();
        MvcResult bugMvcResult = this.requestPostWithOkAndReturn(BUG_COUNT, dashboardFrontPageRequest);
        String bugContentAsString = bugMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder bugResultHolder = JSON.parseObject(bugContentAsString, ResultHolder.class);
        StatisticsDTO bugCount = JSON.parseObject(JSON.toJSONString(bugResultHolder.getData()), StatisticsDTO.class);
        Assertions.assertNotNull(bugCount);
        MvcResult createBugMvcResult = this.requestPostWithOkAndReturn(CREATE_BUG_BY_ME, dashboardFrontPageRequest);
        String createBugContentAsString = createBugMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder createBugResultHolder = JSON.parseObject(createBugContentAsString, ResultHolder.class);
        StatisticsDTO createBugCount = JSON.parseObject(JSON.toJSONString(createBugResultHolder.getData()), StatisticsDTO.class);
        Assertions.assertNotNull(createBugCount);
        MvcResult handleBugMvcResult = this.requestPostWithOkAndReturn(HANDLE_BUG_BY_ME, dashboardFrontPageRequest);
        String handleBugContentAsString = handleBugMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder handleBugResultHolder = JSON.parseObject(handleBugContentAsString, ResultHolder.class);
        StatisticsDTO handleBugCount = JSON.parseObject(JSON.toJSONString(handleBugResultHolder.getData()), StatisticsDTO.class);
        Assertions.assertNotNull(handleBugCount);

        Project project = new Project();
        project.setModuleSetting("[]");
        project.setId(DEFAULT_PROJECT_ID);
        projectMapper.updateByPrimaryKeySelective(project);
        mvcResult = this.requestPostWithOkAndReturn(CASE_COUNT, dashboardFrontPageRequest);
        contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        moduleCount = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), StatisticsDTO.class);
        Assertions.assertNotNull(moduleCount);
        apiMvcResult = this.requestPostWithOkAndReturn(API_COUNT, dashboardFrontPageRequest);
        apiContentAsString = apiMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        apiResultHolder = JSON.parseObject(apiContentAsString, ResultHolder.class);
        apiCount = JSON.parseObject(JSON.toJSONString(apiResultHolder.getData()), StatisticsDTO.class);
        Assertions.assertNotNull(apiCount);
        apiCaseMvcResult = this.requestPostWithOkAndReturn(API_CASE_COUNT, dashboardFrontPageRequest);
        apiCaseContentAsString = apiCaseMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        apiCaseResultHolder = JSON.parseObject(apiCaseContentAsString, ResultHolder.class);
        apiCaseCount = JSON.parseObject(JSON.toJSONString(apiCaseResultHolder.getData()), StatisticsDTO.class);
        Assertions.assertNotNull(apiCaseCount);
        scenarioMvcResult = this.requestPostWithOkAndReturn(SCENARIO_COUNT, dashboardFrontPageRequest);
        scenarioContentAsString = scenarioMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        scenarioResultHolder = JSON.parseObject(scenarioContentAsString, ResultHolder.class);
        scenarioCount = JSON.parseObject(JSON.toJSONString(scenarioResultHolder.getData()), StatisticsDTO.class);
        Assertions.assertNotNull(scenarioCount);

        associateMvcResult = this.requestPostWithOkAndReturn(ASSOCIATE_CASE_COUNT, dashboardFrontPageRequest);
        associateContent = associateMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        associateResultHolder = JSON.parseObject(associateContent, ResultHolder.class);
        associateCount = JSON.parseObject(JSON.toJSONString(associateResultHolder.getData()), StatisticsDTO.class);
        Assertions.assertNotNull(associateCount);
        reviewMvcResult = this.requestPostWithOkAndReturn(REVIEW_CASE_COUNT, dashboardFrontPageRequest);
        reviewContent = reviewMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        reviewResultHolder = JSON.parseObject(reviewContent, ResultHolder.class);
        reviewCount = JSON.parseObject(JSON.toJSONString(reviewResultHolder.getData()), StatisticsDTO.class);
        Assertions.assertNotNull(reviewCount);
        bugMvcResult = this.requestPostWithOkAndReturn(BUG_COUNT, dashboardFrontPageRequest);
        bugContentAsString = bugMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        bugResultHolder = JSON.parseObject(bugContentAsString, ResultHolder.class);
        bugCount = JSON.parseObject(JSON.toJSONString(bugResultHolder.getData()), StatisticsDTO.class);
        Assertions.assertNotNull(bugCount);

        createBugMvcResult = this.requestPostWithOkAndReturn(CREATE_BUG_BY_ME, dashboardFrontPageRequest);
        createBugContentAsString = createBugMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        createBugResultHolder = JSON.parseObject(createBugContentAsString, ResultHolder.class);
        createBugCount = JSON.parseObject(JSON.toJSONString(createBugResultHolder.getData()), StatisticsDTO.class);
        Assertions.assertNotNull(createBugCount);

        handleBugMvcResult = this.requestPostWithOkAndReturn(HANDLE_BUG_BY_ME, dashboardFrontPageRequest);
        handleBugContentAsString = handleBugMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        handleBugResultHolder = JSON.parseObject(handleBugContentAsString, ResultHolder.class);
        handleBugCount = JSON.parseObject(JSON.toJSONString(handleBugResultHolder.getData()), StatisticsDTO.class);
        Assertions.assertNotNull(handleBugCount);


        project.setModuleSetting("[\"apiTest\",\"testPlan\",\"caseManagement\",\"bugManagement\"]");
        project.setId(DEFAULT_PROJECT_ID);
        projectMapper.updateByPrimaryKeySelective(project);
        dashboardFrontPageRequest.setDayNumber(3);
        dashboardFrontPageRequest.setStartTime(null);
        dashboardFrontPageRequest.setEndTime(null);
        mvcResult = this.requestPostWithOkAndReturn(CASE_COUNT, dashboardFrontPageRequest);
        contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        moduleCount = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), StatisticsDTO.class);
        Assertions.assertNotNull(moduleCount);
        apiMvcResult = this.requestPostWithOkAndReturn(API_COUNT, dashboardFrontPageRequest);
        apiContentAsString = apiMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        apiResultHolder = JSON.parseObject(apiContentAsString, ResultHolder.class);
        apiCount = JSON.parseObject(JSON.toJSONString(apiResultHolder.getData()), StatisticsDTO.class);
        Assertions.assertNotNull(apiCount);
        apiCaseMvcResult = this.requestPostWithOkAndReturn(API_CASE_COUNT, dashboardFrontPageRequest);
        apiCaseContentAsString = apiCaseMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        apiCaseResultHolder = JSON.parseObject(apiCaseContentAsString, ResultHolder.class);
        apiCaseCount = JSON.parseObject(JSON.toJSONString(apiCaseResultHolder.getData()), StatisticsDTO.class);
        Assertions.assertNotNull(apiCaseCount);
        scenarioMvcResult = this.requestPostWithOkAndReturn(SCENARIO_COUNT, dashboardFrontPageRequest);
        scenarioContentAsString = scenarioMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        scenarioResultHolder = JSON.parseObject(scenarioContentAsString, ResultHolder.class);
        scenarioCount = JSON.parseObject(JSON.toJSONString(scenarioResultHolder.getData()), StatisticsDTO.class);
        Assertions.assertNotNull(scenarioCount);

        associateMvcResult = this.requestPostWithOkAndReturn(ASSOCIATE_CASE_COUNT, dashboardFrontPageRequest);
        associateContent = associateMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        associateResultHolder = JSON.parseObject(associateContent, ResultHolder.class);
        associateCount = JSON.parseObject(JSON.toJSONString(associateResultHolder.getData()), StatisticsDTO.class);
        Assertions.assertNotNull(associateCount);
        reviewMvcResult = this.requestPostWithOkAndReturn(REVIEW_CASE_COUNT, dashboardFrontPageRequest);
        reviewContent = reviewMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        reviewResultHolder = JSON.parseObject(reviewContent, ResultHolder.class);
        reviewCount = JSON.parseObject(JSON.toJSONString(reviewResultHolder.getData()), StatisticsDTO.class);
        Assertions.assertNotNull(reviewCount);
        bugMvcResult = this.requestPostWithOkAndReturn(BUG_COUNT, dashboardFrontPageRequest);
        bugContentAsString = bugMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        bugResultHolder = JSON.parseObject(bugContentAsString, ResultHolder.class);
        bugCount = JSON.parseObject(JSON.toJSONString(bugResultHolder.getData()), StatisticsDTO.class);
        Assertions.assertNotNull(bugCount);

        createBugMvcResult = this.requestPostWithOkAndReturn(CREATE_BUG_BY_ME, dashboardFrontPageRequest);
        createBugContentAsString = createBugMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        createBugResultHolder = JSON.parseObject(createBugContentAsString, ResultHolder.class);
        createBugCount = JSON.parseObject(JSON.toJSONString(createBugResultHolder.getData()), StatisticsDTO.class);
        Assertions.assertNotNull(createBugCount);

        handleBugMvcResult = this.requestPostWithOkAndReturn(HANDLE_BUG_BY_ME, dashboardFrontPageRequest);
        handleBugContentAsString = handleBugMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        handleBugResultHolder = JSON.parseObject(handleBugContentAsString, ResultHolder.class);
        handleBugCount = JSON.parseObject(JSON.toJSONString(handleBugResultHolder.getData()), StatisticsDTO.class);
        Assertions.assertNotNull(handleBugCount);

    }

    @Test
    @Order(4)
    public void testList() throws Exception {
        DashboardFrontPageRequest dashboardFrontPageRequest = new DashboardFrontPageRequest();
        dashboardFrontPageRequest.setOrganizationId(DEFAULT_ORGANIZATION_ID);
        dashboardFrontPageRequest.setDayNumber(3);
        dashboardFrontPageRequest.setCurrent(1);
        dashboardFrontPageRequest.setPageSize(5);
        dashboardFrontPageRequest.setProjectIds(List.of(DEFAULT_PROJECT_ID));
        MvcResult mvcResult = this.requestPostWithOkAndReturn(REVIEWING_BY_ME, dashboardFrontPageRequest);
        Pager<List<CaseReviewDTO>> tableData = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                Pager.class);
        List<CaseReviewDTO> list = tableData.getList();
        Assertions.assertNotNull(list);
        MvcResult apiMvcResult = this.requestPostWithOkAndReturn(API_CHANGE, dashboardFrontPageRequest);
        Pager<List<ApiDefinitionUpdateDTO>> apiTableData = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(apiMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                Pager.class);
        List<ApiDefinitionUpdateDTO> apiList = apiTableData.getList();
        Assertions.assertNotNull(apiList);
        dashboardFrontPageRequest.setStartTime(1697971947000L);
        dashboardFrontPageRequest.setEndTime(1700650347000L);
        mvcResult = this.requestPostWithOkAndReturn(REVIEWING_BY_ME, dashboardFrontPageRequest);
        tableData = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                Pager.class);
        list = tableData.getList();
        Assertions.assertNotNull(list);
        apiMvcResult = this.requestPostWithOkAndReturn(API_CHANGE, dashboardFrontPageRequest);
        apiTableData = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(apiMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                Pager.class);
        apiList = apiTableData.getList();
        Assertions.assertNotNull(apiList);
        Project project = new Project();
        project.setModuleSetting("[]");
        project.setId(DEFAULT_PROJECT_ID);
        projectMapper.updateByPrimaryKeySelective(project);
        this.requestPost(REVIEWING_BY_ME, dashboardFrontPageRequest).andExpect(status().is5xxServerError());
        this.requestPost(API_CHANGE, dashboardFrontPageRequest).andExpect(status().is5xxServerError());
        project.setModuleSetting("[\"apiTest\",\"testPlan\",\"caseManagement\",\"bugManagement\"]");
        project.setId(DEFAULT_PROJECT_ID);
        projectMapper.updateByPrimaryKeySelective(project);
    }

    @Test
    @Order(5)
    public void testUserList() throws Exception {
        MvcResult mvcResult = this.requestGetWithOkAndReturn(BUG_HANDLE_USER_LIST + DEFAULT_PROJECT_ID);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        List<SelectOption> list = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), SelectOption.class);
        Assertions.assertNotNull(list);
    }

    private void enableDefaultPlatformConfig() {
        ProjectApplication record = new ProjectApplication();
        record.setTypeValue("true");
        ProjectApplicationExample example = new ProjectApplicationExample();
        example.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andTypeEqualTo("BUG_SYNC_SYNC_ENABLE");
        projectApplicationMapper.updateByExampleSelective(record, example);
    }
}
