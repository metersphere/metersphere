package io.metersphere.bug.controller;

import io.metersphere.bug.dto.request.BugBatchRequest;
import io.metersphere.bug.dto.request.BugPageRequest;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.utils.Pager;
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
public class BugTrashControllerTests extends BaseTest {

    public static final String BUG_TRASH_PAGE = "/bug/trash/page";
    public static final String BUG_TRASH_RECOVER = "/bug/trash/recover";
    public static final String BUG_TRASH_DELETE = "/bug/trash/delete";
    public static final String BUG_TRASH_BATCH_RECOVER = "/bug/trash/batch-recover";
    public static final String BUG_TRASH_BATCH_DELETE = "/bug/trash/batch-delete";

    @Test
    @Order(0)
    @Sql(scripts = {"/dml/init_bug_trash.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    void testTrashPage() throws Exception {
        BugPageRequest bugRequest = new BugPageRequest();
        bugRequest.setCurrent(1);
        bugRequest.setPageSize(10);
        bugRequest.setProjectId("bug-trash-project");
        MvcResult mvcResult = this.requestPostWithOkAndReturn(BUG_TRASH_PAGE, bugRequest);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        Pager<?> pageData = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
        // 返回值不为空
        Assertions.assertNotNull(pageData);
        // 返回值的页码和当前页码相同
        Assertions.assertEquals(pageData.getCurrent(), bugRequest.getCurrent());
        // 返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(JSON.parseArray(JSON.toJSONString(pageData.getList())).size() <= bugRequest.getPageSize());
        // 排序
        bugRequest.setSort(Map.of("status", "asc"));
        this.requestPostWithOkAndReturn(BUG_TRASH_PAGE, bugRequest);
    }

    @Test
    @Order(1)
    void testRecoverOrDeleteError() throws Exception {
        // 恢复不存在的缺陷
        this.requestGet(BUG_TRASH_RECOVER + "/trash-bug-not-exist", status().is5xxServerError());
        // 恢复非Local缺陷
        this.requestGet(BUG_TRASH_RECOVER + "/trash-bug-5", status().is5xxServerError());
        // 删除非Local缺陷
        this.requestGet(BUG_TRASH_DELETE + "/trash-bug-5", status().is5xxServerError());
    }

    @Test
    @Order(1)
    void testRecoverOrDeleteSuccess() throws Exception {
        // 恢复Local缺陷
        this.requestGetWithOk(BUG_TRASH_RECOVER + "/trash-bug-1");
        // 删除Local缺陷
        this.requestGetWithOk(BUG_TRASH_DELETE + "/trash-bug-2");
    }

    @Test
    @Order(2)
    void testBatchError() throws Exception {
        // 全选, 恢复所有
        BugBatchRequest recoverRequest = new BugBatchRequest();
        recoverRequest.setProjectId("bug-trash-project-not-exist");
        recoverRequest.setSelectAll(true);
        // 批量恢复Local缺陷
        this.requestPost(BUG_TRASH_BATCH_RECOVER, recoverRequest, status().is5xxServerError());
        // 勾选部分, 恢复部分
        BugBatchRequest recoverRequest1 = new BugBatchRequest();
        recoverRequest1.setSelectAll(false);
        // 批量恢复Local缺陷
        this.requestPost(BUG_TRASH_BATCH_RECOVER, recoverRequest1, status().is5xxServerError());
    }

    @Test
    @Order(2)
    void testBatchSuccess() throws Exception {
        // 全选, 恢复所有
        BugBatchRequest recoverRequest = new BugBatchRequest();
        recoverRequest.setProjectId("bug-trash-project");
        recoverRequest.setSelectAll(true);
        recoverRequest.setExcludeIds(List.of("trash-bug-3", "trash-bug-5"));
        // 批量恢复Local缺陷
        this.requestPostWithOk(BUG_TRASH_BATCH_RECOVER, recoverRequest);
        // 勾选部分, 恢复部分
        BugBatchRequest deleteRequest = new BugBatchRequest();
        deleteRequest.setSelectAll(false);
        deleteRequest.setSelectIds(List.of("trash-bug-3"));
        // 批量恢复Local缺陷
        this.requestPostWithOk(BUG_TRASH_BATCH_DELETE, deleteRequest);
    }
}
