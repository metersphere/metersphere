package io.metersphere.api.controller;

import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.provider.AssociateScenarioProvider;
import io.metersphere.dto.TestCaseProviderDTO;
import io.metersphere.request.AssociateOtherCaseRequest;
import io.metersphere.request.TestCasePageProviderRequest;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class AssociateScenarioProviderTest extends BaseTest {
    @Resource
    private AssociateScenarioProvider provider;

    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_functional_scenario_test.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void getScenarioCaseListSuccess() throws Exception {
        TestCasePageProviderRequest request = new TestCasePageProviderRequest();
        request.setSourceType("SCENARIO");
        request.setSourceId("gyq_associate_scenario_id_1");
        request.setProjectId("project-associate-scenario-test");
        request.setCurrent(1);
        request.setPageSize(10);
        request.setSort(new HashMap<>() {{
            put("createTime", "desc");
        }});
        List<TestCaseProviderDTO> apiScenarioList = provider.getScenarioCaseList("functional_case_test", "case_id", "source_id", request);
        String jsonString = JSON.toJSONString(apiScenarioList);
        System.out.println(jsonString);
    }

    @Test
    @Order(2)
    public void moduleCountSuccess() throws Exception {
        TestCasePageProviderRequest request = new TestCasePageProviderRequest();
        request.setSourceType("SCENARIO");
        request.setSourceId("gyq_associate_scenario_id_1");
        request.setProjectId("project-associate-scenario-test");
        request.setCurrent(1);
        request.setPageSize(10);
        request.setSort(new HashMap<>() {{
            put("createTime", "desc");
        }});

        Map<String, Long> stringLongMap = provider.moduleCount("functional_case_test", "case_id", "source_id", request, false);
        String jsonString = JSON.toJSONString(stringLongMap);
        System.out.println(jsonString);
    }

    @Test
    @Order(3)
    public void getSelectIdsSuccess() throws Exception {
        AssociateOtherCaseRequest request = new AssociateOtherCaseRequest();
        request.setSourceType("SCENARIO");
        request.setSourceId("gyq_associate_scenario_id_1");
        request.setSelectAll(true);
        request.setProjectId("project-associate-scenario-test");
        request.setExcludeIds(List.of("associate_gyq_api_scenario_two"));
        List<ApiScenario> scenarioCases = provider.getSelectScenarioCases(request, false);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(scenarioCases));
        request.setSelectAll(false);
        request.setProjectId("project-associate-scenario-test");
        request.setSelectIds(List.of("associate_gyq_api_scenario_one"));
        scenarioCases = provider.getSelectScenarioCases(request, false);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(scenarioCases));
        request.setSourceType("SCENARIO");
        request.setSourceId("gyq_associate_scenario_id_1");
        request.setSelectAll(true);
        request.setProjectId("project-associate-scenario-test");
        scenarioCases = provider.getSelectScenarioCases(request, false);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(scenarioCases));

    }


}
