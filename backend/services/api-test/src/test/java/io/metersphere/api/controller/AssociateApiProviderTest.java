package io.metersphere.api.controller;

import io.metersphere.api.provider.AssociateApiProvider;
import io.metersphere.dto.TestCaseProviderDTO;
import io.metersphere.request.ApiModuleProviderRequest;
import io.metersphere.request.TestCasePageProviderRequest;
import io.metersphere.request.AssociateOtherCaseRequest;
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
public class AssociateApiProviderTest extends BaseTest {
    @Resource
    private AssociateApiProvider provider;

    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_functional_case_test.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void getApiTestCaseListSuccess() throws Exception {
        TestCasePageProviderRequest request = new TestCasePageProviderRequest();
        request.setSourceType("API");
        request.setSourceId("gyq_associate_case_id_1");
        request.setProjectId("project-associate-case-test");
        request.setCurrent(1);
        request.setPageSize(10);
        request.setSort(new HashMap<>() {{
            put("createTime", "desc");
        }});
        List<TestCaseProviderDTO> apiTestCaseList = provider.getApiTestCaseList("functional_case_test", "case_id", "source_id", request);
        String jsonString = JSON.toJSONString(apiTestCaseList);
        System.out.println(jsonString);
    }

    @Test
    @Order(2)
    public void moduleCountSuccess() throws Exception {
        ApiModuleProviderRequest request = new ApiModuleProviderRequest();
        request.setSourceId("gyq_associate_case_id_1");
        request.setProjectId("project-associate-case-test");
        request.setKeyword("测试查询模块用");

        Map<String, Long> stringLongMap = provider.moduleCount("functional_case_test", "case_id", "source_id", request, false);
        String jsonString = JSON.toJSONString(stringLongMap);
        System.out.println(jsonString);
    }

    @Test
    @Order(3)
    public void getSelectIdsSuccess() throws Exception {
        AssociateOtherCaseRequest request = new AssociateOtherCaseRequest();
        request.setSourceType("API");
        request.setSourceId("gyq_associate_case_id_1");
        request.setSelectAll(true);
        request.setProjectId("project-associate-case-test");
        request.setExcludeIds(List.of("gyq_associate_api_case_id_2"));
        List<String> selectIds = provider.getSelectIds(request, false);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(selectIds));
        request.setSelectAll(false);
        request.setProjectId("project-associate-case-test");
        request.setSelectIds(List.of("gyq_associate_api_case_id_1"));
        selectIds = provider.getSelectIds(request, false);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(selectIds));
    }


}
