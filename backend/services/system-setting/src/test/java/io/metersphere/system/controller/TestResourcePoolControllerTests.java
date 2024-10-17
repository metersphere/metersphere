package io.metersphere.system.controller;

import io.metersphere.sdk.constants.ResourcePoolTypeEnum;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.TestResourcePool;
import io.metersphere.system.domain.TestResourcePoolBlob;
import io.metersphere.system.domain.TestResourcePoolOrganization;
import io.metersphere.system.domain.TestResourcePoolOrganizationExample;
import io.metersphere.system.dto.pool.*;
import io.metersphere.system.dto.sdk.QueryResourcePoolRequest;
import io.metersphere.system.mapper.TestResourcePoolBlobMapper;
import io.metersphere.system.mapper.TestResourcePoolMapper;
import io.metersphere.system.mapper.TestResourcePoolOrganizationMapper;
import io.metersphere.system.service.TestResourcePoolService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TestResourcePoolControllerTests extends BaseTest {

    @Resource
    private MockMvc mockMvc;
    @Resource
    private TestResourcePoolOrganizationMapper testResourcePoolOrganizationMapper;
    @Resource
    private TestResourcePoolMapper testResourcePoolMapper;
    @Resource
    private TestResourcePoolBlobMapper testResourcePoolBlobMapper;

    @Resource
    private MockServerClient mockServerClient;

    protected TestResourcePoolService testResourcePoolService;

    @Value("${embedded.mockserver.host}")
    private String host;
    @Value("${embedded.mockserver.port}")
    private int port;

    private static final String TEST_RESOURCE_POOL_ADD = "/test/resource/pool/add";
    private static final String TEST_RESOURCE_POOL_UPDATE = "/test/resource/pool/update";
    private static final String TEST_RESOURCE_POOL_CAPACITY_LIST = "/test/resource/pool/capacity/task/list";
    private static final String TEST_RESOURCE_POOL_CAPACITY_DETAIL = "/test/resource/pool/capacity/detail";


    private static final ResultMatcher ERROR_REQUEST_MATCHER = status().is5xxServerError();

    private static final String configurationWidthOutOrgIds = "{\n" +
            "  \"loadTestImage\": \"123\",\n" +
            "  \"loadTestHeap\": \"123\",\n" +
            "  \"nodesList\":[{\n" +
            "       \"ip\":\"192.168.20.17\",\n" +
            "       \"port\": \"1194\",\n" +
            "       \"monitor\": \"9100\",\n" +
            "       \"concurrentNumber\": \"100\",\n" +
            "      \"singleTaskConcurrentNumber\": 3\n" +
            "   }],\n" +
            "\"ip\":\"172.2.130.1\",\n" +
            "\"token\":\"dsdfssdsvgsd\",\n" +
            "\"namespace\":\"测试\",\n" +
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
            "      \"concurrentNumber\": \"100\",\n" +
            "      \"singleTaskConcurrentNumber\": 3\n" +
            "    }\n" +
            "  ],\n" +
            "  \"orgIds\": [\"sys_default_organization_2\",\"sys_default_organization_3\"],\n" +
            "  \"ip\": \"172.2.130.1\",\n" +
            "  \"token\": \"dsdfssdsvgsd\",\n" +
            "  \"namespace\": \"测试\",\n" +
            "  \"concurrentNumber\": 3,\n" +
            "  \"podThreads\": 2,\n" +
            "  \"jobDefinition\": \"jsfsjs\",\n" +
            "  \"deployName\": \"hello\",\n" +
            "  \"uiGrid\": \"localhost:4444\",\n" +
            "\"girdConcurrentNumber\":2\n" +
            "}";

    private static final String configurationNoNode = "{\n" +
            "  \"loadTestImage\": \"123\",\n" +
            "  \"loadTestHeap\": \"123\",\n" +
            "  \"nodesList\": [\n" +
            "  ],\n" +
            "  \"orgIds\": [\"sys_default_organization_2\",\"sys_default_organization_3\",\"sys_default_organization_gyq\"],\n" +
            "  \"ip\": \"172.2.130.1\",\n" +
            "  \"token\": \"dsdfssdsvgsd\",\n" +
            "  \"namespace\": \"测试\",\n" +
            "  \"concurrentNumber\": 3,\n" +
            "  \"podThreads\": 2,\n" +
            "  \"jobDefinition\": \"jsfsjs\",\n" +
            "  \"deployName\": \"hello\",\n" +
            "  \"uiGrid\": \"localhost:4444\",\n" +
            "\"girdConcurrentNumber\":2\n" +
            "}";


    private TestResourcePool addTestResourcePoolSuccess(String name, Boolean allOrg, Boolean partOrg, Boolean useApi, Boolean useLoad, Boolean useUi, Boolean noNode, String type) throws Exception {
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
        setResources(testResourcePoolRequest, partOrg, noNode);
        String userId = SessionUtils.getUserId();
        TestResourcePoolDTO testResourcePool = new TestResourcePoolDTO();
        BeanUtils.copyBean(testResourcePool, testResourcePoolRequest);
        testResourcePool.setCreateUser(userId);
        testResourcePool.setCreateTime(System.currentTimeMillis());
        //应用全部 关系表里不会存值
        TestResourcePool testResourcePoolRequest1 = addTestResourcePool(testResourcePool);
        List<TestResourcePoolOrganization> testResourcePoolOrganizations = getTestResourcePoolOrganizations(testResourcePoolRequest1);
        if (allOrg) {
            Assertions.assertTrue(CollectionUtils.isEmpty(testResourcePoolOrganizations));
        }
        return testResourcePoolRequest1;
    }

    public TestResourcePool addTestResourcePool(TestResourcePoolDTO testResourcePool) {
        testResourcePoolService = CommonBeanFactory.getBean(TestResourcePoolService.class);
        String id = IDGenerator.nextStr();
        TestResourcePoolBlob testResourcePoolBlob = new TestResourcePoolBlob();
        testResourcePoolBlob.setId(id);
        TestResourceDTO testResourceDTO = testResourcePool.getTestResourceDTO();
        if (CollectionUtils.isEmpty(testResourceDTO.getNodesList())) {
            testResourceDTO.setNodesList(new ArrayList<>());
        }
        String configuration = JSON.toJSONString(testResourceDTO);
        testResourcePoolBlob.setConfiguration(configuration.getBytes());
        buildTestPoolBaseInfo(testResourcePool, id);
        testResourcePoolMapper.insert(testResourcePool);
        testResourcePoolBlobMapper.insert(testResourcePoolBlob);
        testResourcePool.setId(id);
        return testResourcePool;
    }

    public static void buildTestPoolBaseInfo(TestResourcePool testResourcePool, String id) {
        testResourcePool.setId(id);
        testResourcePool.setUpdateTime(System.currentTimeMillis());
        if (testResourcePool.getEnable() == null) {
            testResourcePool.setEnable(true);
        }
        testResourcePool.setDeleted(false);
    }

    @Test
    @Order(1)
    void addTestResourcePoolOne() throws Exception {
        // 选全部资源池，部分没值 资源池节点为NODE use： api load ui
        this.addTestResourcePoolSuccess("test_pool_1", true, false, true, true, true, false, ResourcePoolTypeEnum.NODE.getName());

    }

    @Test
    @Order(2)
    void addTestResourcePoolTwo() throws Exception {
        // 选全部资源池，部分没值 资源池节点为NODE use： api load
        this.addTestResourcePoolSuccess("test_pool_2", true, false, true, true, false, false, ResourcePoolTypeEnum.NODE.getName());

    }

    @Test
    @Order(3)
    void addTestResourcePoolThree() throws Exception {
        // 选全部资源池，部分没值 资源池节点为NODE use： api
        this.addTestResourcePoolSuccess("test_pool_3", true, false, true, false, false, false, ResourcePoolTypeEnum.NODE.getName());

    }

    @Test
    @Order(4)
    void addTestResourcePoolFour() throws Exception {
        // 选全部资源池，部分没值 资源池节点为NODE use：
        this.addTestResourcePoolSuccess("test_pool_4", true, false, false, false, false, false, ResourcePoolTypeEnum.NODE.getName());

    }

    @Test
    @Order(5)
    void addTestResourcePoolFive() throws Exception {
        //用途只是标记，没有实际影响所以这里每种只测一遍。其余以api为例
        // 选全部资源池，部分有值 资源池节点为NODE use： api
        this.addTestResourcePoolSuccess("test_pool_5", true, true, true, false, false, false, ResourcePoolTypeEnum.NODE.getName());

    }

    @Test
    @Order(6)
    void addTestResourcePoolSix() throws Exception {
        // 不选全部资源池，部分有值 资源池节点为NODE use： api
        this.addTestResourcePoolSuccess("test_pool_6", false, true, true, false, false, false, ResourcePoolTypeEnum.NODE.getName());

    }

    @Test
    @Order(7)
    void addTestResourcePoolSeven() throws Exception {
        //资源池的应用与类型无关 这里资源池正确的顺序就到此为止。换个类型只测一遍就行
        // 不选全部资源池，部分有值 资源池节点为NODE use： api
        this.addTestResourcePoolSuccess("test_pool_7", false, true, true, false, false, false, ResourcePoolTypeEnum.K8S.getName());
    }


    private List<TestResourcePoolOrganization> getTestResourcePoolOrganizations(TestResourcePool testResourcePoolRequest1) {
        TestResourcePoolOrganizationExample testResourcePoolOrganizationExample = new TestResourcePoolOrganizationExample();
        testResourcePoolOrganizationExample.createCriteria().andTestResourcePoolIdEqualTo(testResourcePoolRequest1.getId());
        return testResourcePoolOrganizationMapper.selectByExample(testResourcePoolOrganizationExample);
    }

    @Test
    @Order(8)
    /*@Sql(scripts = {"/dml/init_test_resource_pool.sql"},
            config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED),
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)*/
    void listResourcePoolsWidthSearch() throws Exception {
        listByKeyWord("test_pool_1");
    }

    @Test
    @Order(9)
    void listResourcePoolsNoSearch() throws Exception {
        QueryResourcePoolRequest request = new QueryResourcePoolRequest();
        request.setCurrent(1);
        request.setPageSize(5);
        request.setEnable(true);
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
        TestResourcePoolDTO testResourcePoolDTO = JSON.parseArray(JSON.toJSONString(sortPageData.getList()), TestResourcePoolDTO.class).getFirst();
        Assertions.assertNull(testResourcePoolDTO.getInUsed());
    }

    @Test
    @Sql(scripts = {"/dml/init_test_resource_pool.sql"},
            config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED),
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Order(10)
    void getResourcePoolsDetail() throws Exception {
        getDetail("/test/resource/pool/detail/103", status().isOk());
    }

    @Test
    @Order(11)
    void getResourcePoolsDetailWidthBlobK8s() throws Exception {
        TestResourcePool testResourcePoolRequest1 = this.addTestResourcePoolSuccess("test_pool_blob_k8s", false, true, true, true, true, false, ResourcePoolTypeEnum.K8S.getName());
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

        Assertions.assertNotNull(testResourcePoolReturnDTO.getTestResourceReturnDTO().getIp());
        Assertions.assertNotNull(testResourcePoolReturnDTO.getTestResourceReturnDTO().getToken());
        Assertions.assertNotNull(testResourcePoolReturnDTO.getTestResourceReturnDTO().getNamespace());
        Assertions.assertNotNull(testResourcePoolReturnDTO.getTestResourceReturnDTO().getDeployName());
        Assertions.assertTrue(testResourcePoolReturnDTO.getTestResourceReturnDTO().getConcurrentNumber() > 0);
        Assertions.assertTrue(testResourcePoolReturnDTO.getTestResourceReturnDTO().getPodThreads() > 0);
    }

    @Test
    @Order(12)
    void getResourcePoolsDetailWidthBlobNode() throws Exception {
        TestResourcePool testResourcePoolRequest1 = this.addTestResourcePoolSuccess("test_pool_blob_node", false, true, true, true, true, false, ResourcePoolTypeEnum.NODE.getName());
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


        Assertions.assertFalse(testResourcePoolReturnDTO.getTestResourceReturnDTO().getNodesList().isEmpty());
        for (TestResourceNodeDTO testResourceNodeDTO : testResourcePoolReturnDTO.getTestResourceReturnDTO().getNodesList()) {
            Assertions.assertNotNull(testResourceNodeDTO.getIp());
            Assertions.assertNotNull(testResourceNodeDTO.getPort());
            Assertions.assertNotNull(testResourceNodeDTO.getConcurrentNumber());
        }

    }

    @Test
    @Order(13)
    void getResourcePoolsDetailWidthBlobNoOrgIds() throws Exception {
        TestResourcePool testResourcePoolRequest1 = this.addTestResourcePoolSuccess("test_pool_blob_no_org_id", true, false, true, false, false, false, ResourcePoolTypeEnum.K8S.getName());
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
    @Order(14)
    void getResourcePoolsDetailFiled() throws Exception {
        String id = "1034";
        getDetail("/test/resource/pool/detail/" + id, ERROR_REQUEST_MATCHER);

        TestResourcePool testResourcePoolRequest1 = this.addTestResourcePoolSuccess("test_pool_blob_no_node", true, false, true, false, false, true, ResourcePoolTypeEnum.K8S.getName());
        getDetail("/test/resource/pool/detail/" + testResourcePoolRequest1.getId(), status().isOk());

    }

    private void getDetail(String id, ResultMatcher errorRequestMatcher) throws Exception {
        QueryResourcePoolRequest request = new QueryResourcePoolRequest();
        request.setCurrent(1);
        request.setPageSize(5);
        mockMvc.perform(MockMvcRequestBuilders.get(id)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(errorRequestMatcher)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @Order(15)
    void updateTestResourcePool() throws Exception {
        createMockUrl();
        TestResourcePool testResourcePoolRequest1 = this.addTestResourcePoolSuccess("test_pool_blob2", false, true, true, false, false, false, ResourcePoolTypeEnum.K8S.getName());
        String id = testResourcePoolRequest1.getId();
        TestResourcePoolRequest testResourcePoolRequest = new TestResourcePoolRequest();
        testResourcePoolRequest.setId(id);
        testResourcePoolRequest.setName("test_pool_update");
        testResourcePoolRequest.setType(ResourcePoolTypeEnum.NODE.getName());
        setResources(testResourcePoolRequest, false, false);
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

    @Test
    @Order(16)
    void updateTestResourcePoolWithOrgIds() throws Exception {
        createMockUrl();
        TestResourcePool testResourcePoolRequest1 = this.addTestResourcePoolSuccess("test_pool_blob3", false, true, true, false, false, false, ResourcePoolTypeEnum.K8S.getName());
        String id = testResourcePoolRequest1.getId();
        TestResourcePoolRequest testResourcePoolRequest = new TestResourcePoolRequest();
        testResourcePoolRequest.setId(id);
        testResourcePoolRequest.setName("test_pool_update_1");
        testResourcePoolRequest.setType(ResourcePoolTypeEnum.NODE.getName());
        setResources(testResourcePoolRequest, true, false);
        testResourcePoolRequest.setApiTest(true);
        testResourcePoolRequest.setLoadTest(false);
        testResourcePoolRequest.setUiTest(false);
        testResourcePoolRequest.setAllOrg(false);
        mockMvc.perform(MockMvcRequestBuilders.post(TEST_RESOURCE_POOL_UPDATE)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(testResourcePoolRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        listByKeyWord("test_pool_update_1");
    }

    private void createMockUrl() {
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
    }

    private void setResources(TestResourcePoolRequest testResourcePoolDTO, boolean isPart, boolean noNode) {
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
        if (noNode) {
            testResourceDTO = JSON.parseObject(configurationNoNode, TestResourceDTO.class);
        }
        testResourcePoolDTO.setTestResourceDTO(testResourceDTO);
    }

    @Test
    @Order(17)
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
            TestResourcePoolRequest testResourcePoolRequest = generatorDto(true, false, false, false, false, false, false, false);
            testResourcePoolRequest.setId("");
            this.requestPost(urlType, url, id, testResourcePoolRequest, status().isBadRequest());

            TestResourcePool testResourcePoolRequest1 = this.addTestResourcePoolSuccess("test_pool_blob2", false, true, true, false, false, false, ResourcePoolTypeEnum.K8S.getName());
            id = testResourcePoolRequest1.getId();
        }

        //资源池名称重复
        TestResourcePoolDTO testPool4 = listByKeyWord("test_pool_4");
        TestResourcePoolRequest testResourcePoolRequest = generatorDto(true, false, false, false, false, false, false, false);
        testResourcePoolRequest.setName("test_pool_blob2");
        this.requestPost(urlType, url, testPool4.getId(), testResourcePoolRequest, status().is5xxServerError());
        //资源池类型为空
        testResourcePoolRequest = generatorDto(true, false, true, false, false, false, false, false);
        this.requestPost(urlType, url, id, testResourcePoolRequest, status().isBadRequest());
        //api 类型 资源池节点集合为空
        testResourcePoolRequest = generatorDto(true, false, false, true, false, false, false, false);
        this.requestPost(urlType, url, id, testResourcePoolRequest, ERROR_REQUEST_MATCHER);
        //资源池节点不为空，但是内容为空 ip 为空
        testResourcePoolRequest = generatorDto(true, false, false, true, false, true, false, false);
        this.requestPost(urlType, url, id, testResourcePoolRequest, ERROR_REQUEST_MATCHER);
        //资源池节点不为空，但是内容为空 port 为空
        testResourcePoolRequest = generatorDto(true, false, false, true, false, false, true, false);
        this.requestPost(urlType, url, id, testResourcePoolRequest, ERROR_REQUEST_MATCHER);
        //资源池节点不为空，但是内容为空 最大线程数 为空
        testResourcePoolRequest = generatorDto(true, false, false, true, false, false, false, true);
        this.requestPost(urlType, url, id, testResourcePoolRequest, ERROR_REQUEST_MATCHER);
        //性能测试类型 资源池节点集合为空
        testResourcePoolRequest = generatorDto(false, false, false, true, false, false, false, false);
        this.requestPost(urlType, url, id, testResourcePoolRequest, ERROR_REQUEST_MATCHER);
        //资源池节点不为空，但是内容为空ip为空
        testResourcePoolRequest = generatorDto(false, false, false, true, false, true, false, false);
        this.requestPost(urlType, url, id, testResourcePoolRequest, ERROR_REQUEST_MATCHER);
        //资源池节点不为空，但是内容为空 port 为空
        testResourcePoolRequest = generatorDto(false, false, false, true, false, false, true, false);
        this.requestPost(urlType, url, id, testResourcePoolRequest, ERROR_REQUEST_MATCHER);
        //资源池节点不为空，但是内容为空 port 为空
        testResourcePoolRequest = generatorDto(false, false, false, true, false, false, false, false);
        this.requestPost(urlType, url, id, testResourcePoolRequest, ERROR_REQUEST_MATCHER);
        //资源池节点不为空，但是内容为空 最大线程数 为空
        testResourcePoolRequest = generatorDto(false, false, false, true, false, false, false, true);
        this.requestPost(urlType, url, id, testResourcePoolRequest, ERROR_REQUEST_MATCHER);
        //应用组织
        testResourcePoolRequest = generatorDto(true, false, false, false, true, false, false, false);
        this.requestPost(urlType, url, id, testResourcePoolRequest, status().isBadRequest());
        //部分组织
        testResourcePoolRequest = generatorDto(true, false, false, false, false, false, false, false);
        testResourcePoolRequest.setAllOrg(false);
        testResourcePoolRequest.setTestResourceDTO(JSON.parseObject(configurationWidthOutOrgIds, TestResourceDTO.class));
        this.requestPost(urlType, url, id, testResourcePoolRequest, ERROR_REQUEST_MATCHER);
    }

    public TestResourcePoolDTO listByKeyWord(String keyWord) throws Exception {
        QueryResourcePoolRequest request = new QueryResourcePoolRequest();
        request.setCurrent(1);
        request.setPageSize(5);
        request.initKeyword(keyWord);
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
        TestResourcePoolDTO testResourcePoolDTO = JSON.parseArray(JSON.toJSONString(sortPageData.getList()), TestResourcePoolDTO.class).getFirst();
        Assertions.assertTrue(StringUtils.equals(testResourcePoolDTO.getName(), keyWord));
        return testResourcePoolDTO;
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

    private TestResourcePoolRequest generatorDto(boolean useApiType, boolean noName, boolean noType, boolean noResources, boolean noAllOrg, boolean noIp, boolean noPort, boolean noConcurrentNumber) {
        TestResourcePoolRequest testResourcePoolDTO = new TestResourcePoolRequest();
        //没名字
        if (!noName) {
            testResourcePoolDTO.setName("test_pool_test");
        }
        //没类型
        if (!noType) {
            testResourcePoolDTO.setType(ResourcePoolTypeEnum.NODE.getName());
        }
        //没资源池（用途为API 或者 性能测试的校验）
        if (!noResources) {
            testResourcePoolDTO.setApiTest(true);
            setResources(testResourcePoolDTO, true, false);
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

            if (noConcurrentNumber) {
                testResourceNodeDTO.setConcurrentNumber(null);
            } else {
                testResourceNodeDTO.setConcurrentNumber(1);
            }
            if (!noIp && !noPort && !noConcurrentNumber) {
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



    @Test
    @Order(18)
    public void getTestResourcePoolCapacityDetail() throws Exception {
        TestResourcePool testResourcePool = new TestResourcePool();
        testResourcePool.setId("gyq_pool_delete_enable");
        testResourcePool.setType("NODE");
        testResourcePool.setEnable(true);
        testResourcePool.setDeleted(true);
        testResourcePool.setAllOrg(true);
        testResourcePool.setName("测试");
        testResourcePool.setUpdateTime(System.currentTimeMillis());
        testResourcePool.setCreateTime(System.currentTimeMillis());
        testResourcePool.setCreateUser("admin");
        testResourcePool.setServerUrl("172.06.200.15");
        testResourcePoolMapper.insert(testResourcePool);
        TestResourcePoolCapacityRequest  request = new TestResourcePoolCapacityRequest();
        request.setPoolId("test_pool_1");
        request.setIp("172.16.200.8");
        request.setPort("8082");
        this.requestPost(TEST_RESOURCE_POOL_CAPACITY_DETAIL, request);
        request.setCurrent(1);
        request.setPageSize(10);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(TEST_RESOURCE_POOL_CAPACITY_DETAIL, request);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        request.setPoolId("gyq_pool_delete_enable");
        mvcResult = this.requestPostWithOkAndReturn(TEST_RESOURCE_POOL_CAPACITY_LIST, request);
        // 获取返回值
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        testResourcePool.setEnable(false);
        testResourcePool.setDeleted(false);
        testResourcePoolMapper.updateByPrimaryKeySelective(testResourcePool);
        request.setPoolId("gyq_pool_delete_enable");
        mvcResult = this.requestPostWithOkAndReturn(TEST_RESOURCE_POOL_CAPACITY_LIST, request);
        // 获取返回值
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        testResourcePool.setEnable(true);
        testResourcePool.setDeleted(true);
        testResourcePoolMapper.updateByPrimaryKeySelective(testResourcePool);
        request.setPoolId("test_pool_1");
        request.setIp(null);
        mvcResult = this.requestPostWithOkAndReturn(TEST_RESOURCE_POOL_CAPACITY_LIST, request);
        // 获取返回值
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);

    }

    @Test
    @Order(19)
    public void getSystemTaskItemPage() throws Exception {
        TestResourcePoolCapacityRequest  request = new TestResourcePoolCapacityRequest();
        request.setPoolId("test_pool_1");
        request.setIp("172.16.200.8");
        request.setPort("8082");
        request.setCurrent(1);
        request.setPageSize(10);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(TEST_RESOURCE_POOL_CAPACITY_LIST, request);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        request.setIp(null);
        request.setPort(null);
        mvcResult = this.requestPostWithOkAndReturn(TEST_RESOURCE_POOL_CAPACITY_LIST, request);
        // 获取返回值
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        request.setPoolId("null");
        mvcResult = this.requestPostWithOkAndReturn(TEST_RESOURCE_POOL_CAPACITY_LIST, request);
        // 获取返回值
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);

        request.setPoolId("gyq_pool_delete_enable");
        mvcResult = this.requestPostWithOkAndReturn(TEST_RESOURCE_POOL_CAPACITY_LIST, request);
        // 获取返回值
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        TestResourcePool testResourcePool = new TestResourcePool();
        testResourcePool.setId("gyq_pool_delete_enable");
        testResourcePool.setDeleted(false);
        testResourcePoolMapper.updateByPrimaryKeySelective(testResourcePool);
        request.setPoolId("gyq_pool_delete_enable");
        mvcResult = this.requestPostWithOkAndReturn(TEST_RESOURCE_POOL_CAPACITY_LIST, request);
        // 获取返回值
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);

    }

}