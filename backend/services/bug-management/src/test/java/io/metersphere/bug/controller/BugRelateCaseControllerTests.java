package io.metersphere.bug.controller;

import io.metersphere.bug.dto.BugRelateCaseDTO;
import io.metersphere.bug.dto.request.BugRelatedCasePageRequest;
import io.metersphere.sdk.constants.UserRoleType;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.utils.Pager;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BugRelateCaseControllerTests extends BaseTest {

    public static final String BUG_CASE_RELATE_PAGE = "/bug/relate/case/page";
    public static final String BUG_CASE_UN_RELATE = "/bug/relate/case/un-relate";
    public static final String BUG_CASE_RELATE_CHECK = "/bug/relate/case/check-permission";

    @Test
    @Order(0)
    @Sql(scripts = {"/dml/init_bug_case.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    void testBugRelatePageSuccess() throws Exception {
        BugRelatedCasePageRequest request = new BugRelatedCasePageRequest();
        request.setCurrent(1);
        request.setPageSize(10);
        request.setBugId("default-relate-bug-id");
        request.setKeyword("first");
        MvcResult mvcResult = this.requestPostWithOkAndReturn(BUG_CASE_RELATE_PAGE, request);
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
        BugRelateCaseDTO bugRelateCaseDTO = JSON.parseArray(JSON.toJSONString(pageData.getList()), BugRelateCaseDTO.class).get(0);
        Assertions.assertTrue(StringUtils.contains(bugRelateCaseDTO.getName(), request.getKeyword()));
    }

    @Test
    @Order(1)
    void testBugRelatePageEmpty() throws Exception {
        BugRelatedCasePageRequest request = new BugRelatedCasePageRequest();
        request.setCurrent(1);
        request.setPageSize(10);
        request.setBugId("default-relate-bug-id");
        request.setKeyword("keyword");
        MvcResult mvcResult = this.requestPostWithOkAndReturn(BUG_CASE_RELATE_PAGE, request);
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
        // 返回的数据量为0条
        Assertions.assertEquals(0, pageData.getTotal());
    }

    @Test
    @Order(2)
    void testBugRelatePageError() throws Exception {
        // 页码有误
        BugRelatedCasePageRequest request = new BugRelatedCasePageRequest();
        request.setCurrent(0);
        request.setPageSize(10);
        this.requestPost(BUG_CASE_RELATE_PAGE, request, status().isBadRequest());
        // 页数有误
        request = new BugRelatedCasePageRequest();
        request.setCurrent(1);
        request.setPageSize(1);
        this.requestPost(BUG_CASE_RELATE_PAGE, request, status().isBadRequest());
    }

    @Test
    @Order(3)
    void testBugUnRelateSuccess() throws Exception {
        this.requestGetWithOk(BUG_CASE_UN_RELATE + "/bug-relate-case-default-id");
        this.requestGetWithOk(BUG_CASE_UN_RELATE + "/bug-relate-case-default-id-1");
    }

    @Test
    @Order(4)
    void testBugUnRelateError() throws Exception {
        this.requestGet(BUG_CASE_UN_RELATE + "/bug-relate-case-default-id-x", status().is5xxServerError());
    }

    @Test
    @Order(5)
    void testBugRelateCheckPermissionError() throws Exception {
        // 非功能用例类型参数
        this.requestGet(BUG_CASE_RELATE_CHECK + "/100001100001/API", status().is5xxServerError());
    }

    @Test
    @Order(6)
    void testBugRelateCheckPermissionSuccess() throws Exception {
        // 默认项目ID且登录用户为admin
        this.requestGet(BUG_CASE_RELATE_CHECK + "/100001100001/FUNCTIONAL", status().isOk()).andReturn();
        // 切换登录用户为PROJECT, 权限校验失败
        this.requestGetWithNoAdmin(BUG_CASE_RELATE_CHECK + "/default-project-for-bug/FUNCTIONAL", UserRoleType.PROJECT.name(), status().is5xxServerError()).andReturn();
    }
}
