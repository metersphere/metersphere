package io.metersphere.api.service;

import io.metersphere.api.provider.AssociateApiProvider;
import io.metersphere.api.provider.AssociateScenarioProvider;
import io.metersphere.request.AssociateOtherCaseRequest;
import io.metersphere.request.TestCasePageProviderRequest;
import io.metersphere.system.base.BaseTest;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AssociateCaseProviderTests extends BaseTest {

	@Resource
	AssociateApiProvider apiProvider;
	@Resource
	AssociateScenarioProvider scenarioProvider;

	@Test
	@Order(1)
	@Sql(scripts = {"/dml/init_associate_provider_test.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
	void coverApiProvider() {
		TestCasePageProviderRequest request = new TestCasePageProviderRequest();
		request.setProjectId("test-associate-pro");
		request.setSourceId("test-source-id");
		request.setSourceType("test-source-type");
		apiProvider.listUnRelatedTestCaseList(request);
		request.setKeyword("api-case-associate-2");
		apiProvider.listUnRelatedTestCaseList(request);
		AssociateOtherCaseRequest associateRequest = new AssociateOtherCaseRequest();
		associateRequest.setSelectAll(true);
		associateRequest.setProjectId("test-associate-pro");
		associateRequest.setSourceId("test-source-id");
		associateRequest.setSourceType("test-source-type");
		apiProvider.getRelatedIdsByParam(associateRequest, false);
		associateRequest.setExcludeIds(List.of("api-case-associate-1"));
		apiProvider.getRelatedIdsByParam(associateRequest, false);
		associateRequest.setSelectAll(false);
		associateRequest.setSelectIds(List.of("api-case-associate-1"));
		apiProvider.getRelatedIdsByParam(associateRequest, false);
	}

	@Test
	@Order(2)
	void coverScenarioProvider() {
		TestCasePageProviderRequest request = new TestCasePageProviderRequest();
		request.setProjectId("test-associate-pro");
		request.setSourceId("test-source-id");
		request.setSourceType("test-source-type");
		scenarioProvider.listUnRelatedTestCaseList(request);
		request.setKeyword("api-scenario-associate-2");
		scenarioProvider.listUnRelatedTestCaseList(request);
		AssociateOtherCaseRequest associateRequest = new AssociateOtherCaseRequest();
		associateRequest.setSelectAll(true);
		associateRequest.setProjectId("test-associate-pro");
		associateRequest.setSourceId("test-source-id");
		associateRequest.setSourceType("test-source-type");
		scenarioProvider.getRelatedIdsByParam(associateRequest, false);
		associateRequest.setExcludeIds(List.of("api-case-scenario-1"));
		scenarioProvider.getRelatedIdsByParam(associateRequest, false);
		associateRequest.setSelectAll(false);
		associateRequest.setSelectIds(List.of("api-case-scenario-1"));
		scenarioProvider.getRelatedIdsByParam(associateRequest, false);
	}
}
