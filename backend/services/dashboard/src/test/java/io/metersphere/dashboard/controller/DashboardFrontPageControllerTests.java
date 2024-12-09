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
import io.metersphere.dashboard.response.CascadeChildrenDTO;
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
import io.metersphere.system.dto.user.UserExtendDTO;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.jetbrains.annotations.NotNull;
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
    private static final String PROJECT_PLAN_VIEW = "/dashboard/plan_view";
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
    private static final String PLAN_LEGACY_BUG = "/dashboard/plan_legacy_bug";



    private static final String REVIEWING_BY_ME = "/dashboard/reviewing_by_me";
    private static final String API_CHANGE = "/dashboard/api_change";
    private static final String BUG_HANDLE_USER_LIST = "/dashboard/bug_handle_user/list/";

    private static final String PROJECT_MEMBER_USER_LIST = "/dashboard/member/get-project-member/option/";

    private static final String PROJECT_PLAN_LIST = "/dashboard/plan/option/";


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
        dashboardFrontPageRequest.setHandleUsers(List.of("admin"));
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
        dashboardFrontPageRequest.setProjectIds(new ArrayList<>());
        dashboardFrontPageRequest.setSelectAll(false);
        mvcResult = this.requestPostWithOkAndReturn(CREATE_BY_ME, dashboardFrontPageRequest);
        contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        moduleCount = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), OverViewCountDTO.class);
        Assertions.assertNotNull(moduleCount);
        mvcResultAll = this.requestPostWithOkAndReturn(PROJECT_VIEW, dashboardFrontPageRequest);
        moduleCountAll = ApiDataUtils.parseObject(JSON.toJSONString(parseResponse(mvcResultAll).get("data")), OverViewCountDTO.class);
        Assertions.assertNotNull(moduleCountAll);

        dashboardFrontPageRequest.setDayNumber(null);
        dashboardFrontPageRequest.setStartTime(1697971947000L);
        dashboardFrontPageRequest.setEndTime(1730181702699L);
        dashboardFrontPageRequest.setSelectAll(true);
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

        mvcResultAll = this.requestPostWithOkAndReturn(PROJECT_PLAN_VIEW, dashboardFrontPageRequest);
        moduleCountAll = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(mvcResultAll.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                OverViewCountDTO.class);
        Assertions.assertNotNull(moduleCountAll);

        dashboardFrontPageRequest.setPlanId("dashboard_test-plan-id");
        mvcResultAll = this.requestPostWithOkAndReturn(PROJECT_PLAN_VIEW, dashboardFrontPageRequest);
        moduleCountAll = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(mvcResultAll.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                OverViewCountDTO.class);
        Assertions.assertNotNull(moduleCountAll);

        dashboardFrontPageRequest.setPlanId("dashboard_group-plan");
        mvcResultAll = this.requestPostWithOkAndReturn(PROJECT_PLAN_VIEW, dashboardFrontPageRequest);
        moduleCountAll = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(mvcResultAll.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                OverViewCountDTO.class);
        Assertions.assertNotNull(moduleCountAll);

        dashboardFrontPageRequest.setPlanId("dashboard_test-plan-id2");
        mvcResultAll = this.requestPostWithOkAndReturn(PROJECT_PLAN_VIEW, dashboardFrontPageRequest);
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
        this.requestGet(GET_LAYOUT + "DEFAULT_ORGANIZATION_ID").andExpect(status().is5xxServerError());
        MvcResult defaultResult =  this.requestGetWithOkAndReturn(GET_LAYOUT + DEFAULT_ORGANIZATION_ID);
        String defaultString = defaultResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder defaultHolder = JSON.parseObject(defaultString, ResultHolder.class);
        List<LayoutDTO>defaultDTOS = JSON.parseArray(JSON.toJSONString(defaultHolder.getData()), LayoutDTO.class);
        Assertions.assertEquals(4, defaultDTOS.size());
        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andOrganizationIdEqualTo(DEFAULT_ORGANIZATION_ID);
        List<Project> projects = projectMapper.selectByExample(projectExample);

        ProjectMemberRequest projectMemberRequest = new ProjectMemberRequest();
        projectMemberRequest.setProjectId(DEFAULT_PROJECT_ID);
        projectMemberRequest.setCurrent(1);
        projectMemberRequest.setPageSize(5);
        List<ProjectUserDTO> projectUserDTOS = projectMemberService.listMember(projectMemberRequest);

        List<LayoutDTO> layoutDTO = new ArrayList<>();
        LayoutDTO layoutDTOa = getLayoutDTO(0, DashboardUserLayoutKeys.PROJECT_VIEW, "项目概览");
        layoutDTO.add(layoutDTOa);

        LayoutDTO layoutDTOb = getLayoutDTOWidthProject(1, DashboardUserLayoutKeys.CREATE_BY_ME, "我的创建", projects.stream().map(Project::getId).toList());
        layoutDTO.add(layoutDTOb);

        List<String> userIds = projectUserDTOS.stream().map(ProjectUserDTO::getId).toList();

        LayoutDTO layoutDTOc = getUserLayoutDTO(2,DashboardUserLayoutKeys.PROJECT_MEMBER_VIEW, "人员概览", userIds);
        layoutDTO.add(layoutDTOc);

        LayoutDTO layoutDTO1 = getLayoutDTO(3, DashboardUserLayoutKeys.CASE_COUNT, "用例数量");
        layoutDTO.add(layoutDTO1);
        LayoutDTO layoutDTO2 = getLayoutDTO(4, DashboardUserLayoutKeys.ASSOCIATE_CASE_COUNT, "关联用例统计");
        layoutDTO.add(layoutDTO2);
        LayoutDTO layoutDTO3 = getLayoutDTO(5, DashboardUserLayoutKeys.REVIEW_CASE_COUNT, "用例评审数量统计");
        layoutDTO.add(layoutDTO3);
        LayoutDTO layoutDTO4 = getLayoutDTO(6, DashboardUserLayoutKeys.REVIEWING_BY_ME, "待我评审");
        layoutDTO.add(layoutDTO4);
        LayoutDTO layoutDTO5 = getLayoutDTO(7, DashboardUserLayoutKeys.API_COUNT, "接口数量统计");
        layoutDTO.add(layoutDTO5);
        LayoutDTO layoutDTO6 = getLayoutDTO(8, DashboardUserLayoutKeys.API_CASE_COUNT, "接口用例数量统计");
        layoutDTO.add(layoutDTO6);
        LayoutDTO layoutDTO7 = getLayoutDTO(9, DashboardUserLayoutKeys.SCENARIO_COUNT, "场景用例数量统计");
        layoutDTO.add(layoutDTO7);
        LayoutDTO layoutDTO8 = getLayoutDTO(10, DashboardUserLayoutKeys.API_CHANGE, "接口变更统计");
        layoutDTO.add(layoutDTO8);
        LayoutDTO layoutDTO9 = getLayoutDTO(11, DashboardUserLayoutKeys.TEST_PLAN_COUNT, "测试计划数量统计");
        layoutDTO.add(layoutDTO9);
        LayoutDTO layoutDTOz = getLayoutDTO(12, DashboardUserLayoutKeys.PLAN_LEGACY_BUG, "计划遗留bug统计");
        layoutDTO.add(layoutDTOz);
        LayoutDTO layoutDTOg = getLayoutDTO(17, DashboardUserLayoutKeys.PROJECT_PLAN_VIEW, "测试计划概览");
        layoutDTO.add(layoutDTOg);
        LayoutDTO layoutDTOx = getLayoutDTO(13, DashboardUserLayoutKeys.BUG_COUNT, "缺陷数量统计");
        layoutDTO.add(layoutDTOx);
        LayoutDTO layoutDTOv = getLayoutDTO(14, DashboardUserLayoutKeys.CREATE_BUG_BY_ME, "我创建的缺陷");
        layoutDTO.add(layoutDTOv);
        LayoutDTO layoutDTOn = getLayoutDTO(15, DashboardUserLayoutKeys.HANDLE_BUG_BY_ME, "待我处理的缺陷");
        layoutDTO.add(layoutDTOn);
        LayoutDTO layoutDTOm = getLayoutDTO(16, DashboardUserLayoutKeys.BUG_HANDLE_USER, "缺陷处理人统计");
        layoutDTO.add(layoutDTOm);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(EDIT_LAYOUT + DEFAULT_ORGANIZATION_ID, layoutDTO);
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        List<LayoutDTO>layoutDTOS = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), LayoutDTO.class);
        Assertions.assertNotNull(layoutDTOS);

         MvcResult mvcResultGrt = this.requestGetWithOkAndReturn(GET_LAYOUT + DEFAULT_ORGANIZATION_ID);
        contentAsString = mvcResultGrt.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        layoutDTOS = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), LayoutDTO.class);
        Assertions.assertNotNull(layoutDTOS);

        getLayoutDTOWidthProject(2, DashboardUserLayoutKeys.ASSOCIATE_CASE_COUNT, "关联用例数量", List.of(DEFAULT_PROJECT_ID));
        layoutDTO.add(layoutDTO1);
        mvcResult = this.requestPostWithOkAndReturn(EDIT_LAYOUT + DEFAULT_ORGANIZATION_ID, layoutDTO);
        contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        layoutDTOS = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), LayoutDTO.class);
        Assertions.assertNotNull(layoutDTOS);
    }

    @NotNull
    private static LayoutDTO getUserLayoutDTO(int pos, DashboardUserLayoutKeys createByMe, String name, List<String> userIds) {
        LayoutDTO layoutDTOc = new LayoutDTO();
        layoutDTOc.setId(UUID.randomUUID().toString());
        layoutDTOc.setPos(pos);
        layoutDTOc.setKey(createByMe.toString());
        layoutDTOc.setLabel(name);
        layoutDTOc.setProjectIds(List.of(DEFAULT_PROJECT_ID));
        layoutDTOc.setHandleUsers(userIds);
        layoutDTOc.setFullScreen(false);
        return layoutDTOc;
    }

    @NotNull
    private static LayoutDTO getLayoutDTOWidthProject(int pos, DashboardUserLayoutKeys createByMe, String name, List<String> projects) {
        LayoutDTO layoutDTOb = new LayoutDTO();
        layoutDTOb.setId(UUID.randomUUID().toString());
        layoutDTOb.setPos(pos);
        layoutDTOb.setKey(createByMe.toString());
        layoutDTOb.setLabel(name);
        layoutDTOb.setProjectIds(projects);
        layoutDTOb.setHandleUsers(new ArrayList<>());
        layoutDTOb.setFullScreen(false);
        layoutDTOb.setPlanId("");
        return layoutDTOb;
    }

    @NotNull
    private static LayoutDTO getLayoutDTO(int pos, DashboardUserLayoutKeys associateCaseCount, String name) {
        LayoutDTO layoutDTO2 = getLayoutDTOWidthProject(pos, associateCaseCount, name, new ArrayList<>());
        return layoutDTO2;
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
        MvcResult planBugMvcResult = this.requestPostWithOkAndReturn(PLAN_LEGACY_BUG, dashboardFrontPageRequest);
        String planBugContentAsString = planBugMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder planBugResultHolder = JSON.parseObject(planBugContentAsString, ResultHolder.class);
        StatisticsDTO planBugCount = JSON.parseObject(JSON.toJSONString(planBugResultHolder.getData()), StatisticsDTO.class);
        Assertions.assertNotNull(planBugCount);

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

        planBugMvcResult = this.requestPostWithOkAndReturn(PLAN_LEGACY_BUG, dashboardFrontPageRequest);
        planBugContentAsString = planBugMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        planBugResultHolder = JSON.parseObject(planBugContentAsString, ResultHolder.class);
        planBugCount = JSON.parseObject(JSON.toJSONString(planBugResultHolder.getData()), StatisticsDTO.class);
        Assertions.assertNotNull(planBugCount);

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

        planBugMvcResult = this.requestPostWithOkAndReturn(PLAN_LEGACY_BUG, dashboardFrontPageRequest);
        planBugContentAsString = planBugMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        planBugResultHolder = JSON.parseObject(planBugContentAsString, ResultHolder.class);
        planBugCount = JSON.parseObject(JSON.toJSONString(planBugResultHolder.getData()), StatisticsDTO.class);
        Assertions.assertNotNull(planBugCount);

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

    @Test
    @Order(6)
    public void testProjectUserList() throws Exception {
        MvcResult mvcResult = this.requestGetWithOkAndReturn(PROJECT_MEMBER_USER_LIST + DEFAULT_PROJECT_ID);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        List<UserExtendDTO> list = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), UserExtendDTO.class);
        Assertions.assertNotNull(list);

        mvcResult = this.requestGetWithOkAndReturn(PROJECT_MEMBER_USER_LIST + "id");
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        list = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), UserExtendDTO.class);
        Assertions.assertNotNull(list);
    }

    @Test
    @Order(7)
    public void testProjectPlanList() throws Exception {
        MvcResult mvcResult = this.requestGetWithOkAndReturn(PROJECT_PLAN_LIST + DEFAULT_PROJECT_ID);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        List<CascadeChildrenDTO> list = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), CascadeChildrenDTO.class);
        Assertions.assertNotNull(list);

        mvcResult = this.requestGetWithOkAndReturn(PROJECT_MEMBER_USER_LIST + "id");
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        list = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), CascadeChildrenDTO.class);
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
