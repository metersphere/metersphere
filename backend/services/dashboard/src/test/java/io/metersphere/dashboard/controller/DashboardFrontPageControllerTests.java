package io.metersphere.dashboard.controller;

import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.dashboard.constants.DashboardUserLayoutKeys;
import io.metersphere.dashboard.dto.LayoutDTO;
import io.metersphere.dashboard.request.DashboardFrontPageRequest;
import io.metersphere.dashboard.response.OverViewCountDTO;
import io.metersphere.dashboard.service.DashboardService;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.mapper.UserLayoutMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DashboardFrontPageControllerTests extends BaseTest {

    @Resource
    private DashboardService dashboardService;

    @Resource
    private UserLayoutMapper userLayoutMapper;

    private static final String EDIT_LAYOUT = "/dashboard/layout/edit/";
    private static final String GET_LAYOUT = "/dashboard/layout/get/";


    private static final String CREATE_BY_ME = "/dashboard/CREATE_BY_ME";
    private static final String PROJECT_VIEW = "/dashboard/PROJECT_VIEW";


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
    }

    @Test
    @Order(2)
    public void testLayout() throws Exception {
        List<LayoutDTO> layoutDTO = new ArrayList<>();
        LayoutDTO layoutDTO1 = new LayoutDTO();
        layoutDTO1.setId(UUID.randomUUID().toString());
        layoutDTO1.setPos(1);
        layoutDTO1.setKey(DashboardUserLayoutKeys.CASE_COUNT.toString());
        layoutDTO1.setLabel("用例数量");
        layoutDTO1.setProjectIds(new ArrayList<>());
        layoutDTO1.setHandleUsers(new ArrayList<>());
        layoutDTO1.setFullScreen(false);
        layoutDTO.add(layoutDTO1);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(EDIT_LAYOUT+DEFAULT_ORGANIZATION_ID, layoutDTO);
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        List<LayoutDTO> layoutDTOS = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), LayoutDTO.class);
        Assertions.assertNotNull(layoutDTOS);

        MvcResult mvcResultGrt = this.requestGetWithOkAndReturn(GET_LAYOUT+DEFAULT_ORGANIZATION_ID);
        contentAsString = mvcResultGrt.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        layoutDTOS = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), LayoutDTO.class);
        Assertions.assertNotNull(layoutDTOS);
    }

}
