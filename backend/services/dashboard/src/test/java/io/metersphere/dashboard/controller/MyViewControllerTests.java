package io.metersphere.dashboard.controller;

import io.metersphere.dashboard.request.DashboardViewApiCaseTableRequest;
import io.metersphere.dashboard.request.DashboardViewPlanTableRequest;
import io.metersphere.dashboard.request.DashboardViewTableRequest;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.base.BaseTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MyViewControllerTests extends BaseTest {

	private static final String PLAN_PAGE = "/plan/page";
	private static final String PLAN_STATISTICS = "/plan/statistics";
	private static final String FUNCTIONAL_PAGE = "/functional/page";
	private static final String REVIEW_PAGE = "/review/page";
	private static final String API_PAGE = "/api/page";
	private static final String SCENARIO_PAGE = "/scenario/page";
	private static final String BUG_PAGE = "/bug/page";

	@Override
	public String getBasePath() {
		return "/dashboard/my";
	}

	@Test
	@Order(0)
	void plan() throws Exception{
		DashboardViewTableRequest viewRequest = buildCorrectParam();
		DashboardViewPlanTableRequest planRequest = new DashboardViewPlanTableRequest();
		BeanUtils.copyBean(planRequest, viewRequest);
		planRequest.setType("ALL");
		this.requestPostWithOk(PLAN_PAGE, planRequest);
		this.requestPost(PLAN_STATISTICS, List.of("no-exist-id"));
	}

	@Test
	@Order(1)
	void functional() throws Exception{
		DashboardViewTableRequest viewRequest = buildCorrectParam();
		this.requestPostWithOk(FUNCTIONAL_PAGE, viewRequest);
		viewRequest.setSort(Map.of("id", "desc"));
		this.requestPostWithOk(FUNCTIONAL_PAGE, viewRequest);
	}

	@Test
	@Order(2)
	void review() throws Exception{
		DashboardViewTableRequest viewRequest = buildCorrectParam();
		this.requestPostWithOk(REVIEW_PAGE, viewRequest);
		viewRequest.setSort(Map.of("id", "desc"));
		this.requestPostWithOk(REVIEW_PAGE, viewRequest);
	}

	@Test
	@Order(3)
	void api() throws Exception{
		DashboardViewTableRequest viewRequest = buildCorrectParam();
		DashboardViewApiCaseTableRequest apiRequest = new DashboardViewApiCaseTableRequest();
		BeanUtils.copyBean(apiRequest, viewRequest);
		apiRequest.setProtocols(List.of("HTTP"));
		this.requestPostWithOk(API_PAGE, apiRequest);
		apiRequest.setSort(Map.of("id", "desc"));
		this.requestPostWithOk(API_PAGE, apiRequest);
	}

	@Test
	@Order(4)
	void scenario() throws Exception{
		DashboardViewTableRequest viewRequest = buildCorrectParam();
		this.requestPostWithOk(SCENARIO_PAGE, viewRequest);
		viewRequest.setSort(Map.of("id", "desc"));
		this.requestPostWithOk(SCENARIO_PAGE, viewRequest);
	}

	@Test
	@Order(5)
	void bug() throws Exception{
		DashboardViewTableRequest viewRequest = buildCorrectParam();
		this.requestPostWithOk(BUG_PAGE, viewRequest);
		viewRequest.setSort(Map.of("id", "desc"));
		this.requestPostWithOk(BUG_PAGE, viewRequest);
	}

	private DashboardViewTableRequest buildCorrectParam() {
		DashboardViewTableRequest correctParam = new DashboardViewTableRequest();
		correctParam.setProjectId(DEFAULT_PROJECT_ID);
		correctParam.setCurrent(1);
		correctParam.setPageSize(10);
		return correctParam;
	}
}
