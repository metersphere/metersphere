package io.metersphere.system.controller;

import io.metersphere.sdk.base.BaseTest;
import io.metersphere.sdk.constants.ResourcePoolTypeEnum;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.controller.handler.ResultHolder;
import io.metersphere.sdk.dto.*;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Pager;
import io.metersphere.system.domain.TestResourcePool;
import io.metersphere.system.domain.TestResourcePoolOrganization;
import io.metersphere.system.domain.TestResourcePoolOrganizationExample;
import io.metersphere.system.mapper.TestResourcePoolOrganizationMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.junit.jupiter.api.*;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.Header;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TestResourcePoolControllerTests extends BaseTest {

    @Resource
    private MockMvc mockMvc;
    @Resource
    private TestResourcePoolOrganizationMapper testResourcePoolOrganizationMapper;
    @Resource
    private  MockServerClient mockServerClient;

    @Value("${embedded.mockserver.host}")
    private String host;
    @Value("${embedded.mockserver.port}")
    private int port;

    private static final String TEST_RESOURCE_POOL_ADD = "/test/resource/pool/add";
    private static final String TEST_RESOURCE_POOL_UPDATE = "/test/resource/pool/update";

    private static final ResultMatcher ERROR_REQUEST_MATCHER = status().is5xxServerError();

    private static final String configurationWidthOutOrgIds = "{\n" +
            "  \"loadTestImage\": \"123\",\n" +
            "  \"loadTestHeap\": \"123\",\n" +
            "  \"nodesList\":[{\n" +
            "       \"ip\":\"192.168.20.17\",\n" +
            "       \"port\": \"1194\",\n" +
            "       \"monitor\": \"9100\",\n" +
            "       \"concurrentNumber\": 100\n" +
            "   }],\n" +
            "\"ip\":\"172.2.130.1\",\n" +
            "\"token\":\"dsdfssdsvgsd\",\n" +
            "\"nameSpaces\":\"测试\",\n" +
            "\"concurrentNumber\":3,\n" +
            "\"podThreads\":2,\n" +
            "\"jobDefinition\":\"jsfsjs\",\n" +
            "\"deployName\":\"hello\",\n" +
            "\"uiGrid\":\"localhost:4444\",\n" +
            "\"girdConcurrentNumber\":2\n" +
            "}";

    private static final String configuration = "{\n" +
            "  \"loadTestImage\": \"123\",\n" +
            "  \"loadTestHeap\": \"123\",\n" +
            "  \"nodesList\": [\n" +
            "    {\n" +
            "      \"ip\": \"172.16.200.8\",\n" +
            "      \"port\": \"8082\",\n" +
            "      \"monitor\": \"9100\",\n" +
            "      \"concurrentNumber\": 100\n" +
            "    }\n" +
            "  ],\n" +
            "  \"orgIds\": [\"sys_default_organization_2\",\"sys_default_organization_3\"],\n" +
            "  \"ip\": \"172.2.130.1\",\n" +
            "  \"token\": \"dsdfssdsvgsd\",\n" +
            "  \"nameSpaces\": \"测试\",\n" +
            "  \"concurrentNumber\": 3,\n" +
            "  \"podThreads\": 2,\n" +
            "  \"jobDefinition\": \"jsfsjs\",\n" +
            "  \"deployName\": \"hello\",\n" +
            "  \"uiGrid\": \"localhost:4444\",\n" +
            "\"girdConcurrentNumber\":2\n" +
            "}";


    private MvcResult addTestResourcePoolSuccess(String name, Boolean allOrg, Boolean partOrg, Boolean useApi, Boolean useLoad, Boolean useUi, String type) throws Exception {
        TestResourcePoolRequest testResourcePoolRequest = new TestResourcePoolRequest();
        testResourcePoolRequest.setId("");
        testResourcePoolRequest.setName(name);
        testResourcePoolRequest.setType(type);
        testResourcePoolRequest.setApiTest(useApi);
        testResourcePoolRequest.setLoadTest(useLoad);
        testResourcePoolRequest.setUiTest(useUi);
        //添加成功 需要加应用组织的 全部 部分组织的测试 既有全部又有list
        //应用全部
        testResourcePoolRequest.setAllOrg(allOrg);
        setResources(testResourcePoolRequest, partOrg);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(TEST_RESOURCE_POOL_ADD)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(testResourcePoolRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        //应用全部 关系表里不会存值
        TestResourcePool testResourcePoolRequest1 = getResult(mvcResult);
        List<TestResourcePoolOrganization> testResourcePoolOrganizations = getTestResourcePoolOrganizations(testResourcePoolRequest1);
        if (allOrg) {
            Assertions.assertTrue(CollectionUtils.isEmpty(testResourcePoolOrganizations));
        }
        if (!allOrg) {
            Assertions.assertTrue((CollectionUtils.isNotEmpty(testResourcePoolOrganizations) && testResourcePoolOrganizations.size() == 2));
        }



        return mvcResult;
    }

    @Test
    @Order(1)
    void addTestResourcePoolOne() throws Exception {
        // 选全部资源池，部分没值 资源池节点为NODE use： api load ui
        this.addTestResourcePoolSuccess("test_pool_1", true, false, true, true, true, ResourcePoolTypeEnum.NODE.name());

    }

    @Test
    @Order(2)
    void addTestResourcePoolTwo() throws Exception {
        // 选全部资源池，部分没值 资源池节点为NODE use： api load
        this.addTestResourcePoolSuccess("test_pool_2", true, false, true, true, false, ResourcePoolTypeEnum.NODE.name());

    }

    @Test
    @Order(3)
    void addTestResourcePoolThree() throws Exception {
        // 选全部资源池，部分没值 资源池节点为NODE use： api
        this.addTestResourcePoolSuccess("test_pool_3", true, false, true, false, false, ResourcePoolTypeEnum.NODE.name());

    }

    @Test
    @Order(4)
    void addTestResourcePoolFour() throws Exception {
        // 选全部资源池，部分没值 资源池节点为NODE use：
        this.addTestResourcePoolSuccess("test_pool_4", true, false, false, false, false, ResourcePoolTypeEnum.NODE.name());

    }

    @Test
    @Order(5)
    void addTestResourcePoolFive() throws Exception {
        //用途只是标记，没有实际影响所以这里每种只测一遍。其余以api为例
        // 选全部资源池，部分有值 资源池节点为NODE use： api
        this.addTestResourcePoolSuccess("test_pool_5", true, true, true, false, false, ResourcePoolTypeEnum.NODE.name());

    }

    @Test
    @Order(6)
    void addTestResourcePoolSix() throws Exception {
        // 不选全部资源池，部分有值 资源池节点为NODE use： api
        this.addTestResourcePoolSuccess("test_pool_6", false, true, true, false, false, ResourcePoolTypeEnum.NODE.name());

    }

    @Test
    @Order(7)
    void addTestResourcePoolSeven() throws Exception {
        //资源池的应用与类型无关 这里资源池正确的顺序就到此为止。换个类型只测一遍就行
        // 不选全部资源池，部分有值 资源池节点为NODE use： api
        this.addTestResourcePoolSuccess("test_pool_7", false, true, true, false, false, ResourcePoolTypeEnum.K8S.name());
    }

    @Test
    @Order(8)
    void addTestResourcePoolFailedBySameName() throws Exception {
        TestResourcePoolRequest testResourcePoolRequest = new TestResourcePoolRequest();
        testResourcePoolRequest.setName("test_pool_7");
        testResourcePoolRequest.setType(ResourcePoolTypeEnum.K8S.name());
        testResourcePoolRequest.setApiTest(true);
        testResourcePoolRequest.setLoadTest(false);
        testResourcePoolRequest.setUiTest(false);
        //添加成功 需要加应用组织的 全部 部分组织的测试 既有全部又有list
        //应用全部
        testResourcePoolRequest.setAllOrg(false);
        setResources(testResourcePoolRequest, true);
        mockMvc.perform(MockMvcRequestBuilders.post(TEST_RESOURCE_POOL_ADD)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(testResourcePoolRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(ERROR_REQUEST_MATCHER)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    }

    private static TestResourcePool getResult(MvcResult mvcResult) throws UnsupportedEncodingException {
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        return JSON.parseObject(JSON.toJSONString(resultHolder.getData()), TestResourcePool.class);
    }

    private List<TestResourcePoolOrganization> getTestResourcePoolOrganizations(TestResourcePool testResourcePoolRequest1) {
        TestResourcePoolOrganizationExample testResourcePoolOrganizationExample = new TestResourcePoolOrganizationExample();
        testResourcePoolOrganizationExample.createCriteria().andTestResourcePoolIdEqualTo(testResourcePoolRequest1.getId());
        return testResourcePoolOrganizationMapper.selectByExample(testResourcePoolOrganizationExample);
    }

    @Test
    @Order(9)
    void addUiTestResourcePoolFiled() throws Exception {
        this.dealTestResourcePoolFiled("ADD");
    }

    @Test
    @Order(10)
    /*@Sql(scripts = {"/dml/init_test_resource_pool.sql"},
            config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED),
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)*/
    void listResourcePoolsWidthSearch() throws Exception {
        listByKeyWord("test_pool_1");
    }

    @Test
    @Order(11)
    void listResourcePoolsNoSearch() throws Exception {
        QueryResourcePoolRequest request = new QueryResourcePoolRequest();
        request.setCurrent(1);
        request.setPageSize(5);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/test/resource/pool/page")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String sortData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder sortHolder = JSON.parseObject(sortData, ResultHolder.class);
        Pager<?> sortPageData = JSON.parseObject(JSON.toJSONString(sortHolder.getData()), Pager.class);
        // 返回值中取出第一条ID最大的数据, 并判断是否是default-admin
        TestResourcePoolDTO testResourcePoolDTO = JSON.parseArray(JSON.toJSONString(sortPageData.getList()), TestResourcePoolDTO.class).get(0);
        Assertions.assertTrue(testResourcePoolDTO.getInUsed());
    }

    @Test
    @Sql(scripts = {"/dml/init_test_resource_pool.sql"},
            config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED),
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Order(12)
    void getResourcePoolsDetail() throws Exception {
        QueryResourcePoolRequest request = new QueryResourcePoolRequest();
        request.setCurrent(1);
        request.setPageSize(5);
        mockMvc.perform(MockMvcRequestBuilders.get("/test/resource/pool/detail/103")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @Order(13)
    void getResourcePoolsDetailWidthBlobK8s() throws Exception {
        MvcResult testPoolBlob = this.addTestResourcePoolSuccess("test_pool_blob_k8s", false, true, true, true, true, ResourcePoolTypeEnum.K8S.name());
        TestResourcePool testResourcePoolRequest1 = getResult(testPoolBlob);
        String id = testResourcePoolRequest1.getId();
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/test/resource/pool/detail/" + id)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        TestResourcePoolReturnDTO testResourcePoolReturnDTO = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), TestResourcePoolReturnDTO.class);
        Assertions.assertTrue((CollectionUtils.isNotEmpty(testResourcePoolReturnDTO.getTestResourceReturnDTO().getOrgIdNameMap())));
        if (testResourcePoolRequest1.getUiTest()) {
            Assertions.assertNotNull(testResourcePoolReturnDTO.getTestResourceReturnDTO().getUiGrid());
            Assertions.assertTrue(testResourcePoolReturnDTO.getTestResourceReturnDTO().getGirdConcurrentNumber() > 0);
        }

        if (testResourcePoolRequest1.getApiTest()) {
            Assertions.assertNotNull(testResourcePoolReturnDTO.getTestResourceReturnDTO().getIp());
            Assertions.assertNotNull(testResourcePoolReturnDTO.getTestResourceReturnDTO().getToken());
            Assertions.assertNotNull(testResourcePoolReturnDTO.getTestResourceReturnDTO().getNameSpaces());
            Assertions.assertNotNull(testResourcePoolReturnDTO.getTestResourceReturnDTO().getDeployName());
            Assertions.assertTrue(testResourcePoolReturnDTO.getTestResourceReturnDTO().getConcurrentNumber() > 0);
            Assertions.assertTrue(testResourcePoolReturnDTO.getTestResourceReturnDTO().getPodThreads() > 0);
        }

        if (testResourcePoolRequest1.getLoadTest()) {
            Assertions.assertNotNull(testResourcePoolReturnDTO.getTestResourceReturnDTO().getIp());
            Assertions.assertNotNull(testResourcePoolReturnDTO.getTestResourceReturnDTO().getToken());
            Assertions.assertNotNull(testResourcePoolReturnDTO.getTestResourceReturnDTO().getNameSpaces());
            Assertions.assertTrue(testResourcePoolReturnDTO.getTestResourceReturnDTO().getConcurrentNumber() > 0);
            Assertions.assertTrue(testResourcePoolReturnDTO.getTestResourceReturnDTO().getPodThreads() > 0);
        }

    }

    @Test
    @Order(14)
    void getResourcePoolsDetailWidthBlobNode() throws Exception {
        MvcResult testPoolBlob = this.addTestResourcePoolSuccess("test_pool_blob_node", false, true, true, true, true, ResourcePoolTypeEnum.NODE.name());
        TestResourcePool testResourcePoolRequest1 = getResult(testPoolBlob);
        String id = testResourcePoolRequest1.getId();
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/test/resource/pool/detail/" + id)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        TestResourcePoolReturnDTO testResourcePoolReturnDTO = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), TestResourcePoolReturnDTO.class);
        Assertions.assertTrue((CollectionUtils.isNotEmpty(testResourcePoolReturnDTO.getTestResourceReturnDTO().getOrgIdNameMap())));
        if (testResourcePoolRequest1.getUiTest()) {
            Assertions.assertNotNull(testResourcePoolReturnDTO.getTestResourceReturnDTO().getUiGrid());
            Assertions.assertTrue(testResourcePoolReturnDTO.getTestResourceReturnDTO().getGirdConcurrentNumber() > 0);
        }

        if (testResourcePoolRequest1.getApiTest()) {
            Assertions.assertTrue(testResourcePoolReturnDTO.getTestResourceReturnDTO().getNodesList().size()>0);
            for (TestResourceNodeDTO testResourceNodeDTO : testResourcePoolReturnDTO.getTestResourceReturnDTO().getNodesList()) {
                Assertions.assertNotNull(testResourceNodeDTO.getIp());
                Assertions.assertNotNull(testResourceNodeDTO.getPort());
                Assertions.assertNotNull(testResourceNodeDTO.getConcurrentNumber());
            }
        }

        if (testResourcePoolRequest1.getLoadTest()) {
            Assertions.assertTrue(testResourcePoolReturnDTO.getTestResourceReturnDTO().getNodesList().size()>0);
            for (TestResourceNodeDTO testResourceNodeDTO : testResourcePoolReturnDTO.getTestResourceReturnDTO().getNodesList()) {
                Assertions.assertNotNull(testResourceNodeDTO.getIp());
                Assertions.assertNotNull(testResourceNodeDTO.getPort());
                Assertions.assertNotNull(testResourceNodeDTO.getConcurrentNumber());
                Assertions.assertNotNull(testResourceNodeDTO.getMonitor());
            }
        }

    }

    @Test
    @Order(15)
    void getResourcePoolsDetailWidthBlobNoOtgIds() throws Exception {
        MvcResult testPoolBlob = this.addTestResourcePoolSuccess("test_pool_blob_no_org_id", true, false, true, false, false, ResourcePoolTypeEnum.K8S.name());
        TestResourcePool testResourcePoolRequest1 = getResult(testPoolBlob);
        String id = testResourcePoolRequest1.getId();
        QueryResourcePoolRequest request = new QueryResourcePoolRequest();
        request.setCurrent(1);
        request.setPageSize(5);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/test/resource/pool/detail/" + id)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        TestResourcePoolReturnDTO testResourcePoolReturnDTO = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), TestResourcePoolReturnDTO.class);
        Assertions.assertTrue((CollectionUtils.isEmpty(testResourcePoolReturnDTO.getTestResourceReturnDTO().getOrgIdNameMap())));

    }


    @Test
    @Order(16)
    void getResourcePoolsDetailFiled() throws Exception {
        QueryResourcePoolRequest request = new QueryResourcePoolRequest();
        request.setCurrent(1);
        request.setPageSize(5);
        mockMvc.perform(MockMvcRequestBuilders.get("/test/resource/pool/detail/1034")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(ERROR_REQUEST_MATCHER)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @Order(17)
    void updateTestResourcePool() throws Exception {
        MvcResult testPoolBlob = this.addTestResourcePoolSuccess("test_pool_blob2", false, true, true, false, false, ResourcePoolTypeEnum.K8S.name());
        TestResourcePool testResourcePoolRequest1 = getResult(testPoolBlob);
        String id = testResourcePoolRequest1.getId();
        TestResourcePoolRequest testResourcePoolRequest = new TestResourcePoolRequest();
        testResourcePoolRequest.setId(id);
        testResourcePoolRequest.setName("test_pool_update");
        testResourcePoolRequest.setType(ResourcePoolTypeEnum.NODE.name());
        setResources(testResourcePoolRequest, false);
        testResourcePoolRequest.setApiTest(true);
        testResourcePoolRequest.setLoadTest(false);
        testResourcePoolRequest.setUiTest(false);
        testResourcePoolRequest.setAllOrg(true);
        mockMvc.perform(MockMvcRequestBuilders.post(TEST_RESOURCE_POOL_UPDATE)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(testResourcePoolRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        listByKeyWord("test_pool_update");
    }

    private  void setResources(TestResourcePoolRequest testResourcePoolDTO, boolean isPart) {
        TestResourceDTO testResourceDTO;
        if (isPart) {
            testResourceDTO = JSON.parseObject(configuration, TestResourceDTO.class);
            testResourceDTO.getNodesList().forEach(testResourceNodeDTO -> {
                testResourceNodeDTO.setIp(host);
                testResourceNodeDTO.setPort(port + "");
            });
        } else {
            testResourceDTO = JSON.parseObject(configurationWidthOutOrgIds, TestResourceDTO.class);
        }
        testResourcePoolDTO.setTestResourceDTO(testResourceDTO);
    }

    @Test
    @Order(18)
    void updateUiTestResourcePoolFiled() throws Exception {
        this.dealTestResourcePoolFiled("UPDATE");
    }

    private void dealTestResourcePoolFiled(String urlType) throws Exception {
        String url;
        String id = "";
        if (StringUtils.equals(urlType, "ADD")) {
            url = TEST_RESOURCE_POOL_ADD;
        } else {
            //更新 ID 为空
            url = TEST_RESOURCE_POOL_UPDATE;
            TestResourcePoolRequest testResourcePoolRequest = generatorDto(true, false, false, false, false, false, false, false, false);
            testResourcePoolRequest.setId("");
            this.requestPost(urlType, url, id, testResourcePoolRequest, status().isBadRequest());

            MvcResult testPoolBlob = this.addTestResourcePoolSuccess("test_pool_blob2", false, true, true, false, false, ResourcePoolTypeEnum.K8S.name());
            TestResourcePool testResourcePoolRequest1 = getResult(testPoolBlob);
            id = testResourcePoolRequest1.getId();
        }

        //资源池名称为空
        TestResourcePoolRequest testResourcePoolRequest = generatorDto(true, true, false, false, false, false, false, false, false);
        this.requestPost(urlType, url, id, testResourcePoolRequest, status().isBadRequest());
        //资源池类型为空
        testResourcePoolRequest = generatorDto(true, false, true, false, false, false, false, false, false);
        this.requestPost(urlType, url, id, testResourcePoolRequest, status().isBadRequest());
        //api 类型 资源池节点集合为空
        testResourcePoolRequest = generatorDto(true, false, false, true, false, false, false, false, false);
        this.requestPost(urlType, url, id, testResourcePoolRequest, ERROR_REQUEST_MATCHER);
        //资源池节点不为空，但是内容为空 ip 为空
        testResourcePoolRequest = generatorDto(true, false, false, true, false, true, false, false, false);
        this.requestPost(urlType, url, id, testResourcePoolRequest, ERROR_REQUEST_MATCHER);
        //资源池节点不为空，但是内容为空 port 为空
        testResourcePoolRequest = generatorDto(true, false, false, true, false, false, true, false, false);
        this.requestPost(urlType, url, id, testResourcePoolRequest, ERROR_REQUEST_MATCHER);
        //资源池节点不为空，但是内容为空 最大线程数 为空
        testResourcePoolRequest = generatorDto(true, false, false, true, false, false, false, false, true);
        this.requestPost(urlType, url, id, testResourcePoolRequest, ERROR_REQUEST_MATCHER);
        //性能测试类型 资源池节点集合为空
        testResourcePoolRequest = generatorDto(false, false, false, true, false, false, false, false, false);
        this.requestPost(urlType, url, id, testResourcePoolRequest, ERROR_REQUEST_MATCHER);
        //资源池节点不为空，但是内容为空ip为空
        testResourcePoolRequest = generatorDto(false, false, false, true, false, true, false, false, false);
        this.requestPost(urlType, url, id, testResourcePoolRequest, ERROR_REQUEST_MATCHER);
        //资源池节点不为空，但是内容为空 port 为空
        testResourcePoolRequest = generatorDto(false, false, false, true, false, false, true, false, false);
        this.requestPost(urlType, url, id, testResourcePoolRequest, ERROR_REQUEST_MATCHER);
        //资源池节点不为空，但是内容为空 port 为空
        testResourcePoolRequest = generatorDto(false, false, false, true, false, false, false, true, false);
        this.requestPost(urlType, url, id, testResourcePoolRequest, ERROR_REQUEST_MATCHER);
        //资源池节点不为空，但是内容为空 最大线程数 为空
        testResourcePoolRequest = generatorDto(false, false, false, true, false, false, false, false, true);
        this.requestPost(urlType, url, id, testResourcePoolRequest, ERROR_REQUEST_MATCHER);
        //应用组织
        testResourcePoolRequest = generatorDto(true, false, false, false, true, false, false, false, false);
        this.requestPost(urlType, url, id, testResourcePoolRequest, status().isBadRequest());
        //部分组织
        testResourcePoolRequest = generatorDto(true, false, false, false, false, false, false, false, false);
        testResourcePoolRequest.setAllOrg(false);
        testResourcePoolRequest.setTestResourceDTO(JSON.parseObject(configurationWidthOutOrgIds, TestResourceDTO.class));
        this.requestPost(urlType, url, id, testResourcePoolRequest, ERROR_REQUEST_MATCHER);
    }

    @Test
    //单独执行时请打开
    /*@Sql(scripts = {"/dml/init_test_resource_pool.sql"},
            config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED),
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)*/
    @Order(19)
    void deleteTestResourcePool() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/test/resource/pool/delete/103")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    }

    @Test
    @Order(20)
    void deleteTestResourcePoolFiled() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/test/resource/pool/delete/105")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(ERROR_REQUEST_MATCHER)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    }

    @Test
    @Order(21)
    void unableTestResourcePoolSuccess() throws Exception {
        MvcResult testPoolBlob = this.addTestResourcePoolSuccess("test_pool_blob4", false, true, true, false, false, ResourcePoolTypeEnum.K8S.name());
        TestResourcePool testResourcePoolRequest1 = getResult(testPoolBlob);
        String id = testResourcePoolRequest1.getId();
        mockMvc.perform(MockMvcRequestBuilders.post("/test/resource/pool/set/enable/"+id)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    }

    @Test
    @Order(22)
    void createExpectationForInvalidAuth() throws Exception {
        mockServerClient
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/status"))
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeaders(
                                        new Header("Content-Type", "application/json; charset=utf-8"),
                                        new Header("Cache-Control", "public, max-age=86400"))
                                .withBody(JSON.toJSONString(ResultHolder.success("OK")))
                );
        MvcResult testPoolBlob = this.addTestResourcePoolSuccess("test_pool_blob3", false, true, true, false, false, ResourcePoolTypeEnum.NODE.name());
        TestResourcePool testResourcePoolRequest1 = getResult(testPoolBlob);
        String id = testResourcePoolRequest1.getId();
        mockMvc.perform(MockMvcRequestBuilders.post("/test/resource/pool/set/enable/"+id)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    }

    @Test
    @Order(23)
    void unableTestResourcePoolFiled() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/test/resource/pool/set/enable/105")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(ERROR_REQUEST_MATCHER)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    }



    void listByKeyWord(String keyWord) throws Exception {
        QueryResourcePoolRequest request = new QueryResourcePoolRequest();
        request.setCurrent(1);
        request.setPageSize(5);
        request.setKeyword(keyWord);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/test/resource/pool/page")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String sortData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder sortHolder = JSON.parseObject(sortData, ResultHolder.class);
        Pager<?> sortPageData = JSON.parseObject(JSON.toJSONString(sortHolder.getData()), Pager.class);
        // 返回值中取出第一条ID最大的数据, 并判断是否是default-admin
        TestResourcePoolDTO testResourcePoolDTO = JSON.parseArray(JSON.toJSONString(sortPageData.getList()), TestResourcePoolDTO.class).get(0);
        Assertions.assertTrue(StringUtils.equals(testResourcePoolDTO.getName(), keyWord));
    }

    private void requestPost(String urlType, String url, String id, TestResourcePoolRequest testResourcePoolRequest, ResultMatcher resultMatcher) throws Exception {
        if (!StringUtils.equals(urlType, "ADD")) {
            testResourcePoolRequest.setId(id);
        }
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(testResourcePoolRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(resultMatcher)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    private TestResourcePoolRequest generatorDto(boolean useApiType, boolean noName, boolean noType, boolean noResources, boolean noAllOrg, boolean noIp, boolean noPort, boolean noMonitor, boolean noConcurrentNumber) {
        TestResourcePoolRequest testResourcePoolDTO = new TestResourcePoolRequest();
        //没名字
        if (!noName) {
            testResourcePoolDTO.setName("test_pool_test");
        }
        //没类型
        if (!noType) {
            testResourcePoolDTO.setType(ResourcePoolTypeEnum.NODE.name());
        }
        //没资源池（用途为API 或者 性能测试的校验）
        if (!noResources) {
            testResourcePoolDTO.setApiTest(true);
            setResources(testResourcePoolDTO, true);
        } else {
            testResourcePoolDTO.setApiTest(useApiType);
            testResourcePoolDTO.setLoadTest(!useApiType);
            TestResourceDTO testResourceDTO = JSON.parseObject(configuration, TestResourceDTO.class);
            //有资源池（用途为API 或者 性能测试的校验）但 IP port monitor ConcurrentNumber 为空
            TestResourceNodeDTO testResourceNodeDTO = new TestResourceNodeDTO();
            if (noIp) {
                testResourceNodeDTO.setIp("");
            } else {
                testResourceNodeDTO.setIp(host);
            }
            if (noPort) {
                testResourceNodeDTO.setPort("");
            } else {
                testResourceNodeDTO.setPort(port + "");
            }
            if (noMonitor) {
                testResourceNodeDTO.setMonitor(" ");
            } else {
                testResourceNodeDTO.setMonitor("11");
            }
            if (noConcurrentNumber) {
                testResourceNodeDTO.setConcurrentNumber(null);
            } else {
                testResourceNodeDTO.setConcurrentNumber(1);
            }
            if (!noIp && !noPort && !noMonitor && !noConcurrentNumber) {
                testResourceDTO.setNodesList(null);
            } else {
                List<TestResourceNodeDTO> testResourceNodeDTOS = new ArrayList<>();
                testResourceNodeDTOS.add(testResourceNodeDTO);
                testResourceDTO.setNodesList(testResourceNodeDTOS);
            }
            testResourcePoolDTO.setTestResourceDTO(testResourceDTO);
        }
        //没选全部
        if (!noAllOrg) {
            testResourcePoolDTO.setAllOrg(true);
        } else {
            testResourcePoolDTO.getTestResourceDTO().setOrgIds(null);
        }

        return testResourcePoolDTO;
    }


}