package io.metersphere.functional.controller;

import io.metersphere.functional.request.RelationshipAddRequest;
import io.metersphere.functional.request.RelationshipDeleteRequest;
import io.metersphere.functional.request.RelationshipPageRequest;
import io.metersphere.functional.request.RelationshipRequest;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.controller.handler.result.MsHttpResultCode;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class FunctionalCaseRelationshipControllerTests extends BaseTest {

    public static final String RELATE_PAGE = "/functional/case/relationship/relate/page";
    public static final String ADD = "/functional/case/relationship/add";
    public static final String PAGE = "/functional/case/relationship/page";
    public static final String DELETE = "/functional/case/relationship/delete";
    public static final String IDS = "/functional/case/relationship/get-ids/";


    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_relationship_test.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testRelatePage() throws Exception {
        RelationshipPageRequest request = new RelationshipPageRequest();
        request.setId("123");
        request.setProjectId("wx_relationship");
        request.setCurrent(1);
        request.setPageSize(10);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(RELATE_PAGE, request);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        request.setId("wx_relationship_1");
        request.setProjectId("wx_relationship");
        request.setSort(new HashMap<>() {{
            put("createTime", "desc");
        }});
        this.requestPostWithOkAndReturn(RELATE_PAGE, request);
    }


    @Test
    @Order(2)
    public void testAddRelationship() throws Exception {
        //前置用例
        RelationshipAddRequest request = new RelationshipAddRequest();
        request.setId("wx_relationship_1");
        request.setProjectId("wx_relationship");
        request.setType("PRE");
        request.setSelectIds(List.of("wx_relationship_3"));
        MvcResult mvcResult = this.requestPostWithOkAndReturn(ADD, request);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);

        //测试依赖关系 形成环
        request.setId("wx_relationship_3");
        request.setProjectId("wx_relationship");
        request.setType("PRE");
        request.setSelectIds(List.of("wx_relationship_1"));
        assertErrorCode(this.requestPost(ADD, request), MsHttpResultCode.FAILED);

        //后置用例
        request.setId("wx_relationship_4");
        request.setType("POST");
        request.setSelectIds(List.of("wx_relationship_5"));
        MvcResult postResult = this.requestPostWithOkAndReturn(ADD, request);
        // 获取返回值
        String postReturnData = postResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder postResultHolder = JSON.parseObject(postReturnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(postResultHolder);


        //增加覆盖率
        request.setId("wx_relationship_1");
        request.setType("POST");
        request.setSelectIds(List.of("wx_relationship_6"));
        this.requestPostWithOkAndReturn(ADD, request);

        request.setSelectIds(null);
        this.requestPostWithOkAndReturn(ADD, request);
    }


    @Test
    @Order(3)
    public void testRelationshipList() throws Exception {
        //分页查询前置用例列表
        RelationshipRequest request = new RelationshipRequest();
        request.setId("wx_relationship_1");
        request.setProjectId("wx_relationship");
        request.setType("PRE");
        request.setCurrent(1);
        request.setPageSize(10);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(PAGE, request);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);


        //分页查询后置用例列表
        request.setId("wx_relationship_4");
        request.setType("POST");
        MvcResult postResult = this.requestPostWithOkAndReturn(PAGE, request);
        // 获取返回值
        String postReturnData = postResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder postResultHolder = JSON.parseObject(postReturnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(postResultHolder);


        //增加覆盖率
        request.setId("wx_relationship_1");
        request.setType("POST");
        this.requestPostWithOkAndReturn(PAGE, request);
        request.setId("test1111");
        request.setType("POST");
        this.requestPostWithOkAndReturn(PAGE, request);
    }

    @Test
    @Order(4)
    public void testDelete() throws Exception {
        RelationshipDeleteRequest request = new RelationshipDeleteRequest();
        request.setId("relationship_1");
        request.setCaseId("wx_relationship_1");
        request.setType("POST");
        MvcResult postResult = this.requestPostWithOkAndReturn(DELETE, request);
        // 获取返回值
        String postReturnData = postResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder postResultHolder = JSON.parseObject(postReturnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(postResultHolder);

        //异常覆盖
        request.setId("test");
        request.setType("PRE");
        assertErrorCode(this.requestPost(DELETE, request), MsHttpResultCode.FAILED);
    }

    @Test
    @Order(5)
    public void testIds() throws Exception {
        MvcResult postResult = this.requestGetWithOkAndReturn(IDS + "123");
        // 获取返回值
        String postReturnData = postResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder postResultHolder = JSON.parseObject(postReturnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(postResultHolder);

        //异常覆盖
        RelationshipDeleteRequest request = new RelationshipDeleteRequest();
        request.setCaseId("wx_relationship_1");
        request.setId("test");
        request.setType("PRE");
        assertErrorCode(this.requestPost(DELETE, request), MsHttpResultCode.FAILED);
    }
}
