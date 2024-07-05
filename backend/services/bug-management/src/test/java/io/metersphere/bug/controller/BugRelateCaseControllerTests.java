package io.metersphere.bug.controller;

import io.metersphere.bug.dto.BugCaseCheckResult;
import io.metersphere.bug.dto.request.BugRelatedCasePageRequest;
import io.metersphere.bug.dto.response.BugRelateCaseDTO;
import io.metersphere.bug.service.BugRelateCaseCommonService;
import io.metersphere.context.AssociateCaseFactory;
import io.metersphere.provider.BaseAssociateCaseProvider;
import io.metersphere.request.AssociateOtherCaseRequest;
import io.metersphere.request.TestCasePageProviderRequest;
import io.metersphere.sdk.constants.UserRoleType;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BugRelateCaseControllerTests extends BaseTest {

    @Resource
    BaseAssociateCaseProvider functionalCaseProvider;

    @Resource
    BugRelateCaseCommonService bugRelateCaseCommonService;

    public static final String BUG_CASE_UN_RELATE_PAGE = "/bug/case/un-relate/page";
    public static final String BUG_CASE_UN_RELATE_MODULE_TREE = "/bug/case/un-relate/module/tree";
    public static final String BUG_CASE_UN_RELATE_MODULE_COUNT = "/bug/case/un-relate/module/count";
    public static final String BUG_CASE_RELATE = "/bug/case/relate";
    public static final String BUG_CASE_PAGE = "/bug/case/page";
    public static final String BUG_CASE_UN_RELATE = "/bug/case/un-relate";
    public static final String BUG_CASE_CHECK = "/bug/case/check-permission";

    @Test
    @Order(0)
    @Sql(scripts = {"/dml/init_bug_case.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    void testBugUnRelateCasePage() throws Exception {
        TestCasePageProviderRequest request = new TestCasePageProviderRequest();
        request.setCurrent(1);
        request.setPageSize(10);
        request.setProjectId("default-project-for-bug");
        request.setVersionId("default_bug_version");
        request.setSourceId("default-relate-bug-id'");
        request.setSourceType("FUNCTIONAL");
        this.requestPost(BUG_CASE_UN_RELATE_PAGE, request, status().is5xxServerError());
    }

    @Test
    @Order(1)
    void testBugUnRelateCaseModule() throws Exception {
        TestCasePageProviderRequest request = new TestCasePageProviderRequest();
        request.setProjectId("default-project-for-bug");
        request.setVersionId("default_bug_version");
        request.setSourceId("default-relate-bug-id'");
        request.setSourceType("FUNCTIONAL");
        request.setCurrent(1);
        request.setPageSize(10);
        this.requestPostWithOk(BUG_CASE_UN_RELATE_MODULE_TREE, request);
        this.requestPostWithOk(BUG_CASE_UN_RELATE_MODULE_COUNT, request);
    }

    @Test
    @Order(2)
    void testBugRelateCase() throws Exception {
        AssociateOtherCaseRequest request = new AssociateOtherCaseRequest();
        request.setSelectAll(true);
        request.setExcludeIds(List.of("bug_relate_case-2", "bug_relate_case-3"));
        request.setProjectId("default-project-for-bug");
        request.setVersionId("default_bug_version");
        request.setSourceId("default-relate-bug-id");
        request.setSourceType("FUNCTIONAL");
        AssociateCaseFactory.PROVIDER_MAP.put("FUNCTIONAL", functionalCaseProvider);
        Mockito.when(functionalCaseProvider.getRelatedIdsByParam(request, false)).thenReturn(Collections.emptyList());
        this.requestPostWithOk(BUG_CASE_RELATE, request);
        request.setExcludeIds(null);
        Mockito.when(functionalCaseProvider.getRelatedIdsByParam(request, false)).thenReturn(List.of("bug_relate_case-2", "bug_relate_case-3"));
        this.requestPostWithOk(BUG_CASE_RELATE, request);
    }

    @Test
    @Order(3)
    void testBugRelatePageSuccess() throws Exception {
        BugRelatedCasePageRequest request = new BugRelatedCasePageRequest();
        request.setCurrent(1);
        request.setPageSize(10);
        request.setBugId("default-relate-bug-id");
        request.setKeyword("first");
        MvcResult mvcResult = this.requestPostWithOkAndReturn(BUG_CASE_PAGE, request);
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
        BugRelateCaseDTO bugRelateCaseDTO = JSON.parseArray(JSON.toJSONString(pageData.getList()), BugRelateCaseDTO.class).getFirst();
        Assertions.assertTrue(StringUtils.contains(bugRelateCaseDTO.getRelateCaseName(), request.getKeyword()));
    }

    @Test
    @Order(4)
    void testBugRelatePageEmpty() throws Exception {
        BugRelatedCasePageRequest request = new BugRelatedCasePageRequest();
        request.setCurrent(1);
        request.setPageSize(10);
        request.setBugId("default-relate-bug-id");
        request.setKeyword("keyword");
        MvcResult mvcResult = this.requestPostWithOkAndReturn(BUG_CASE_PAGE, request);
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
    @Order(5)
    void testBugRelatePageError() throws Exception {
        // 页码有误
        BugRelatedCasePageRequest request = new BugRelatedCasePageRequest();
        request.setCurrent(0);
        request.setPageSize(10);
        this.requestPost(BUG_CASE_PAGE, request, status().isBadRequest());
        // 页数有误
        request = new BugRelatedCasePageRequest();
        request.setCurrent(1);
        request.setPageSize(1);
        this.requestPost(BUG_CASE_PAGE, request, status().isBadRequest());
    }

    @Test
    @Order(6)
    void testBugUnRelateSuccess() throws Exception {
        this.requestGetWithOk(BUG_CASE_UN_RELATE + "/bug-relate-case-default-id");
        this.requestGetWithOk(BUG_CASE_UN_RELATE + "/bug-relate-case-default-id-1");
    }

    @Test
    @Order(7)
    void testBugUnRelateError() throws Exception {
        this.requestGet(BUG_CASE_UN_RELATE + "/bug-relate-case-default-id-x", status().is5xxServerError());
    }

    @Test
    @Order(8)
    void testBugRelateCheckPermissionError() throws Exception {
        // 非功能用例类型参数
        this.requestGet(BUG_CASE_CHECK + "/100001100001/UI", status().is5xxServerError());
    }

    @Test
    @Order(9)
    void testBugRelateCheckPermissionSuccess() throws Exception {
        // 默认项目ID且登录用户为admin
        MvcResult mvcResult = this.requestGetAndReturn(BUG_CASE_CHECK + "/100001100001/FUNCTIONAL");
        BugCaseCheckResult resultData = getResultData(mvcResult, BugCaseCheckResult.class);
        Assertions.assertTrue(resultData.getPass());
        // 切换登录用户为PROJECT, 权限校验失败
        MvcResult errResult = this.requestGetWithNoAdmin(BUG_CASE_CHECK + "/default-project-for-bug/FUNCTIONAL", UserRoleType.PROJECT.name()).andReturn();
        BugCaseCheckResult errData = getResultData(errResult, BugCaseCheckResult.class);
        Assertions.assertFalse(errData.getPass());
        Assertions.assertEquals(errData.getMsg(), Translator.get("bug_relate_case_permission_error"));
    }

    @Test
    @Order(10)
    void coverTest() {
        bugRelateCaseCommonService.refreshPos(null);
        bugRelateCaseCommonService.updatePos(null, 1L);
    }
}
