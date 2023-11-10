package io.metersphere.api.controller;

import io.metersphere.api.dto.definition.ApiDefinitionDTO;
import io.metersphere.api.dto.request.ApiDefinitionPageRequest;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.uid.NumGenerator;
import io.metersphere.system.utils.Pager;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;

import java.nio.charset.StandardCharsets;
import java.util.*;


@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ApiDefinitionControllerTests extends BaseTest {

    private final static String URL_DEFINITION_ADD = "/api/definition/add";
    private final static String URL_DEFINITION_UPDATE = "/api/definition/update";
    private final static String URL_DEFINITION_BATCH_UPDATE = "/api/definition/batch-update";
    private final static String URL_DEFINITION_DELETE = "/api/definition/delete";
    private final static String URL_DEFINITION_BATCH_DELETE= "/api/definition/batch-del";
    private final static String URL_DEFINITION_PAGE= "/api/definition/page";

    private final static String DEFAULT_PROJECT_ID = "100001100001";



    @Test
    @Order(1)
    public void testAdd() throws Exception {
        LogUtils.info("create api test");
        // 创建一个 MockMultipartFile 对象，用于模拟文件上传
        MockMultipartFile file = new MockMultipartFile("files", "files", MediaType.APPLICATION_OCTET_STREAM_VALUE, "Test content".getBytes());

        // 创建一个 ApiDefinitionDTO 对象，用于模拟请求的一部分
        ApiDefinitionDTO request = new ApiDefinitionDTO();
        // 补充属性内容
        request.setId("test-api-id");
        request.setCreateUser("test-user");
        request.setCreateTime(System.currentTimeMillis());
        request.setUpdateUser("test-api-id");
        request.setUpdateTime(System.currentTimeMillis());
        request.setProjectId("test-project-id");
        request.setName("test-api-name");
        request.setProtocol("test-api-protocol");
        request.setPos(1l);
        request.setLatest(true);
        request.setStatus("test-api-status");
        request.setVersionId("test-api-version");
        request.setRefId("test-api-version");
        request.setDeleted(false);
        request.setModuleId("test-api-module-id");
        request.setNum(NumGenerator.nextNum("test-project-id", ApplicationNumScope.API_DEFINITION));
        LinkedMultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        paramMap.add("files", file);
        this.requestMultipartWithOkAndReturn(URL_DEFINITION_ADD, paramMap);
    }

    @Test
    @Order(2)
    public void testUpdate() throws Exception {
        LogUtils.info("delete api test");
        ApiDefinitionDTO request = new ApiDefinitionDTO();
        // 补充属性内容
        request.setId("test-api-id");
        request.setCreateUser("test-user");
        request.setCreateTime(System.currentTimeMillis());
        request.setUpdateUser("test-api-id");
        request.setUpdateTime(System.currentTimeMillis());
        request.setProjectId("test-project-id");
        request.setName("test-api-name");
        request.setProtocol("test-api-protocol");
        request.setPos(1l);
        request.setLatest(true);
        request.setStatus("test-api-status");
        request.setVersionId("test-api-version");
        request.setDeleted(false);

        this.requestPostWithOk(URL_DEFINITION_UPDATE, request);
    }

    @Test
    @Order(2)
    public void testBatchUpdate() throws Exception {
        LogUtils.info("delete api test");
        List<String> tests = new ArrayList<>();
        tests.add("test-api-id");

        this.requestPostWithOk(URL_DEFINITION_BATCH_UPDATE, tests);
    }

    @Test
    @Order(4)
    public void testDel() throws Exception {
        LogUtils.info("delete api test");
        List<String> tests = new ArrayList<>();
        tests.add("test-api-id");

        this.requestPostWithOk(URL_DEFINITION_DELETE, tests);
    }

    @Test
    @Order(5)
    public void testBatchDel() throws Exception {
        LogUtils.info("delete api test");
        List<String> tests = new ArrayList<>();
        tests.add("test-api-id");

        this.requestPostWithOk(URL_DEFINITION_BATCH_DELETE, tests);
    }

    @Test
    @Order(6)
    @Sql(scripts = {"/dml/init_api_definition.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void getPage() throws Exception {
        ApiDefinitionPageRequest request = new ApiDefinitionPageRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setCurrent(1);
        request.setPageSize(10);
        this.requestPost(URL_DEFINITION_PAGE, request);
        request.setSort(new HashMap<>() {{
            put("createTime", "asc");
        }});

        // ALL 全部 KEYWORD 关键字 FILTER 筛选 COMBINE 自定义
        String search = "KEYWORD";
        Map<String, List<String>> filters = new HashMap<>();
        Map<String, Object> map = new HashMap<>();
        switch (search) {
            case "ALL":
                // Perform all search types
                request.setKeyword("100");
                filters.put("status", Arrays.asList("Underway", "Completed"));
                filters.put("method", List.of("GET"));
                filters.put("version_id", List.of("1005704995741369851"));
                request.setFilter(filters);

                map.put("name", Map.of("operator", "like", "value", "test-1"));
                map.put("method", Map.of("operator", "in", "value", Arrays.asList("GET", "POST")));
                request.setCombine(map);
                break;
            case "KEYWORD":
                // 基础查询
                request.setKeyword("100");
                // 版本查询
                request.setVersionId("100570499574136985");
                break;
            case "FILTER":
                // 筛选
                filters.put("status", Arrays.asList("Underway", "Completed"));
                filters.put("method", List.of("GET"));
                filters.put("version_id", List.of("1005704995741369851"));
                request.setFilter(filters);
                break;
            case "COMBINE":
                // 自定义字段 测试
                map.put("name", Map.of("operator", "like", "value", "test-1"));
                map.put("method", Map.of("operator", "in", "value", Arrays.asList("GET", "POST")));
                request.setCombine(map);
                break;
            default:
                break;
        }

        MvcResult mvcResult = this.requestPostWithOkAndReturn(URL_DEFINITION_PAGE, request);
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
    }

}
