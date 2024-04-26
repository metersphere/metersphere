package io.metersphere.functional.controller;

import io.metersphere.functional.provider.AssociateFunctionalProvider;
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
    AssociateFunctionalProvider functionalProvider;

    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_associate_case_provider_test.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    void coverCaseProvider() {
        TestCasePageProviderRequest request = new TestCasePageProviderRequest();
        request.setProjectId("test-pro");
        request.setVersionId("test-ver");
        request.setSourceId("test-source-id");
        request.setSourceType("test-source-type");
        functionalProvider.listUnRelatedTestCaseList(request);
        AssociateOtherCaseRequest associateRequest = new AssociateOtherCaseRequest();
        associateRequest.setSelectAll(true);
        associateRequest.setProjectId("select-case-pro");
        associateRequest.setVersionId("v1.0.0");
        associateRequest.setSourceId("test-source-id");
        associateRequest.setSourceType("test-source-type");
        functionalProvider.getRelatedIdsByParam(associateRequest, false);
        associateRequest.setExcludeIds(List.of("select-case"));
        functionalProvider.getRelatedIdsByParam(associateRequest, false);
        associateRequest.setSelectAll(false);
        associateRequest.setSelectIds(List.of("select-case"));
        functionalProvider.getRelatedIdsByParam(associateRequest, false);
    }
}
