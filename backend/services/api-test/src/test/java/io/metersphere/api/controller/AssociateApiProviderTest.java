package io.metersphere.api.controller;

import io.metersphere.api.provider.AssociateApiProvider;
import io.metersphere.dto.ApiTestCaseProviderDTO;
import io.metersphere.request.ApiTestCasePageProviderRequest;
import io.metersphere.sdk.util.JSON;
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

import java.util.HashMap;
import java.util.List;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class AssociateApiProviderTest extends BaseTest {
    @Resource
    private AssociateApiProvider provider;

    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_functional_case_test.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void getApiTestCaseListSuccess() throws Exception {
        ApiTestCasePageProviderRequest request = new ApiTestCasePageProviderRequest();
        request.setSourceId("gyq_associate_case_id_1");
        request.setProjectId("project-associate-case-test");
        request.setCurrent(1);
        request.setPageSize(10);
        request.setSort(new HashMap<>() {{
            put("createTime", "desc");
        }});
        List<ApiTestCaseProviderDTO> apiTestCaseList = provider.getApiTestCaseList("functional_case_test", "case_id", "source_id", request);
        String jsonString = JSON.toJSONString(apiTestCaseList);
        System.out.println(jsonString);
    }


}
