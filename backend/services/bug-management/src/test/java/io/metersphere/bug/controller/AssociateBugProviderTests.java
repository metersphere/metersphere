package io.metersphere.bug.controller;

import io.metersphere.bug.provider.AssociateBugProvider;
import io.metersphere.dto.BugProviderDTO;
import io.metersphere.request.AssociateBugPageRequest;
import io.metersphere.request.AssociateBugRequest;
import io.metersphere.request.BugPageProviderRequest;
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

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class AssociateBugProviderTests extends BaseTest {

    @Resource
    private AssociateBugProvider associateBugProvider;

    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_bug_relation_case.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void getBugList() {
        BugPageProviderRequest request = new BugPageProviderRequest();
        request.setSourceId("wx_associate_case_id_1");
        request.setProjectId("project_wx_associate_test");
        request.setCurrent(1);
        request.setPageSize(10);
        List<BugProviderDTO> bugList = associateBugProvider.getBugList("bug_relation_case", "case_id", "bug_id", request);
        String jsonString = JSON.toJSONString(bugList);
        System.out.println(jsonString);
    }

    @Test
    @Order(2)
    public void getSelectBugs() {
        AssociateBugRequest request = new AssociateBugRequest();
        request.setCaseId("wx_associate_case_id_1");
        request.setProjectId("project_wx_associate_test");
        request.setSelectAll(true);
        List<String> list = associateBugProvider.getSelectBugs(request, false);
        String jsonString = JSON.toJSONString(list);
        System.out.println(jsonString);

        request.setExcludeIds(List.of("bug_id_3"));
        List<String> list1 = associateBugProvider.getSelectBugs(request, false);
        String jsonString1 = JSON.toJSONString(list1);
        System.out.println(jsonString1);

        request.setSelectAll(false);
        request.setSelectIds(List.of("bug_id_1", "bug_id_2"));
        List<String> list2 = associateBugProvider.getSelectBugs(request, false);
        String jsonString2 = JSON.toJSONString(list2);
        System.out.println(jsonString2);

    }


    @Test
    @Order(3)
    public void testAssociateBug() {
        associateBugProvider.handleAssociateBug(List.of("bug_id_1", "bug_id_2"), "wx", "wx_associate_case_id_1");
    }

    @Test
    @Order(4)
    public void testDisassociateBug() {
        associateBugProvider.disassociateBug("wx_test_id_1");
    }

    @Test
    @Order(5)
    public void testAssociateBugPage(){
        AssociateBugPageRequest request = new AssociateBugPageRequest();
        request.setCurrent(1);
        request.setPageSize(10);
        request.setCaseId("123");
        request.setProjectId("project_wx_associate_test");
        associateBugProvider.hasAssociateBugPage(request);
        request.setCaseId("wx_2");
        associateBugProvider.hasAssociateBugPage(request);
        request.setCaseId("wx_3");
        associateBugProvider.hasAssociateBugPage(request);
    }
}
