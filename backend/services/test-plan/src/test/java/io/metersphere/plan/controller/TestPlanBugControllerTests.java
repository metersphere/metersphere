package io.metersphere.plan.controller;

import io.metersphere.plan.dto.request.TestPlanBugPageRequest;
import io.metersphere.plan.dto.response.TestPlanBugPageResponse;
import io.metersphere.plan.service.TestPlanBugService;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestPlanBugControllerTests extends BaseTest {

	@Resource
	private ProjectMapper projectMapper;
	@Resource
	private TestPlanBugService testPlanBugService;

	public static final String TEST_PLAN_BUG_PAGE = "/test-plan/bug/page";

	private static final String TEST_PLAN_DELETE = "/test-plan/delete";

	@Test
	@Order(1)
	@Sql(scripts = {"/dml/init_test_plan_bug.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
	void testBugPageSuccess() throws Exception {
		TestPlanBugPageRequest request = new TestPlanBugPageRequest();
		request.setPlanId("test-plan-id-for-bug");
		request.setProjectId("100001100001");
		request.setCurrent(1);
		request.setPageSize(10);
		request.setKeyword("oasis");
		MvcResult mvcResult = this.requestPostWithOkAndReturn(TEST_PLAN_BUG_PAGE, request);
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
		// 返回值中取出第一条数据, 并判断是否包含关键字default
		TestPlanBugPageResponse bug = JSON.parseArray(JSON.toJSONString(pageData.getList()), TestPlanBugPageResponse.class).getFirst();
		Assertions.assertTrue(StringUtils.contains(bug.getTitle(), request.getKeyword())
				|| StringUtils.contains(bug.getNum(), request.getKeyword()));
		// 数据为空
		request.setKeyword("oasis-1");
		request.setSort(Map.of("b.create_time", "asc"));
		this.requestPost(TEST_PLAN_BUG_PAGE, request);
	}

	@Test
	@Order(2)
	void testBugPageError() throws Exception {
		// 参数有误
		TestPlanBugPageRequest request = new TestPlanBugPageRequest();
		request.setCurrent(1);
		request.setPageSize(10);
		this.requestPost(TEST_PLAN_BUG_PAGE, request, status().isBadRequest());
		// 页码有误
		request.setProjectId("100001100001");
		request.setPlanId("test-plan-id");
		request.setCurrent(0);
		request.setPageSize(10);
		this.requestPost(TEST_PLAN_BUG_PAGE, request, status().isBadRequest());
		// 页数有误
		request.setCurrent(1);
		request.setPageSize(1);
		this.requestPost(TEST_PLAN_BUG_PAGE, request, status().isBadRequest());
	}

	@Test
	@Order(3)
	void coverDeletePlan() throws Exception {
		Project project = new Project();
		project.setId("100001100001");
		project.setModuleSetting(JSON.toJSONString(List.of("bugManagement","caseManagement", "apiTest", "testPlan")));
		projectMapper.updateByPrimaryKeySelective(project);
		this.requestGet(TEST_PLAN_DELETE + "/test-plan-id-for-bug");
		this.requestGet(TEST_PLAN_DELETE + "/test-plan-id-for-bug-1");
	}

	@Test
	@Order(4)
	void emptyFunctionTest() throws Exception {
		testPlanBugService.caseExecResultCount("testPlanId");
		testPlanBugService.selectDistinctExecResultByTestPlanIds(null);
		testPlanBugService.updatePos(null, 0);
		testPlanBugService.refreshPos(null);
	}
}
