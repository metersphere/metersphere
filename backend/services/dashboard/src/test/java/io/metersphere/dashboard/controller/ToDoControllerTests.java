package io.metersphere.dashboard.controller;

import io.metersphere.bug.dto.request.BugPageRequest;
import io.metersphere.functional.request.CaseReviewPageRequest;
import io.metersphere.plan.dto.request.TestPlanTableRequest;
import io.metersphere.system.base.BaseTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ToDoControllerTests extends BaseTest {

	private static final String PLAN_PAGE = "/plan/page";
	private static final String REVIEW_PAGE = "/review/page";
	private static final String BUG_PAGE = "/bug/page";

	@Override
	public String getBasePath() {
		return "/dashboard/todo";
	}

	@Test
	@Order(0)
	void plan() throws Exception{
		TestPlanTableRequest request = new TestPlanTableRequest();
		request.setProjectId(DEFAULT_PROJECT_ID);
		request.setCurrent(1);
		request.setPageSize(10);
		request.setType("ALL");
		this.requestPostWithOk(PLAN_PAGE, request);
		request.setSort(Map.of("id", "desc"));
		this.requestPostWithOk(PLAN_PAGE, request);
	}

	@Test
	@Order(1)
	void review() throws Exception{
		CaseReviewPageRequest request = new CaseReviewPageRequest();
		request.setProjectId(DEFAULT_PROJECT_ID);
		request.setCurrent(1);
		request.setPageSize(10);
		this.requestPostWithOk(REVIEW_PAGE, request);
		request.setSort(Map.of("id", "desc"));
		this.requestPostWithOk(REVIEW_PAGE, request);
	}

	@Test
	@Order(2)
	void bug() throws Exception{
		BugPageRequest request = new BugPageRequest();
		request.setProjectId(DEFAULT_PROJECT_ID);
		request.setCurrent(1);
		request.setPageSize(10);
		this.requestPostWithOk(BUG_PAGE, request);
		request.setSort(Map.of("id", "desc"));
		this.requestPostWithOk(BUG_PAGE, request);
	}
}
