package io.metersphere.bug.controller;

import io.metersphere.system.base.BaseTest;
import io.metersphere.system.dto.request.OperationHistoryRequest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BugHistoryControllerTests extends BaseTest {

	public static final String BUG_PAGE = "/bug/history/page";

	@Test
	@Order(1)
	@Sql(scripts = {"/dml/init_bug_history.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
	void testBugHistoryPageSuccess() throws Exception {
		OperationHistoryRequest request = new OperationHistoryRequest();
		request.setSourceId("bug-history-id");
		request.setProjectId("100001100001");
		request.setCurrent(1);
		request.setPageSize(10);
		this.requestPost(BUG_PAGE, request).andExpect(status().isOk());
		request.setSort(Map.of("createTime", "asc"));
		this.requestPost(BUG_PAGE, request).andExpect(status().isOk());
	}
}
