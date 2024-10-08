package io.metersphere.api.controller;

import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.domain.TestResourcePool;
import io.metersphere.system.domain.TestResourcePoolBlob;
import io.metersphere.system.dto.pool.TestResourceDTO;
import io.metersphere.system.dto.pool.TestResourcePoolDTO;
import io.metersphere.system.dto.pool.TestResourcePoolRequest;
import io.metersphere.system.mapper.TestResourcePoolBlobMapper;
import io.metersphere.system.mapper.TestResourcePoolMapper;
import io.metersphere.system.service.TestResourcePoolService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.utils.SessionUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class KubernetesEngineTests extends BaseTest {

    @Resource
    private TestResourcePoolMapper testResourcePoolMapper;
    @Resource
    private TestResourcePoolBlobMapper testResourcePoolBlobMapper;

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
            "      \"concurrentNumber\": 100\n" +
            "    }\n" +
            "  ],\n" +
            "  \"orgIds\": [\"sys_default_organization_2\",\"sys_default_organization_3\"],\n" +
            "  \"ip\": \"172.2.130.1\",\n" +
            "  \"token\": \"test\",\n" +
            "  \"namespace\": \"test\",\n" +
            "  \"concurrentNumber\": 3,\n" +
            "  \"podThreads\": 2,\n" +
            "  \"jobDefinition\": \"jsfsjs\",\n" +
            "  \"deployName\": \"hello\",\n" +
            "  \"uiGrid\": \"localhost:4444\",\n" +
            "\"girdConcurrentNumber\":2\n" +
            "}";

    @Value("${embedded.mockserver.host}")
    private String host;
    @Value("${embedded.mockserver.port}")
    private int port;

    protected TestResourcePoolService testResourcePoolService;

    private void setResources(TestResourcePoolRequest testResourcePoolDTO, boolean isPart) {
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

    private String addPool(String type) {
        TestResourcePoolRequest testResourcePoolRequest = new TestResourcePoolRequest();
        testResourcePoolRequest.setId("");
        testResourcePoolRequest.setName("api_test_pool");
        testResourcePoolRequest.setType(type);
        testResourcePoolRequest.setApiTest(false);
        testResourcePoolRequest.setLoadTest(false);
        testResourcePoolRequest.setUiTest(false);
        //添加成功 需要加应用组织的 全部 部分组织的测试 既有全部又有list
        //应用全部
        testResourcePoolRequest.setAllOrg(true);
        setResources(testResourcePoolRequest, false);
        String userId = SessionUtils.getUserId();
        TestResourcePoolDTO testResourcePool = new TestResourcePoolDTO();
        BeanUtils.copyBean(testResourcePool, testResourcePoolRequest);
        testResourcePool.setCreateUser(userId);
        testResourcePool.setCreateTime(System.currentTimeMillis());
        //应用全部 关系表里不会存值
        TestResourcePool testResourcePoolRequest1 = addTestResourcePool(testResourcePool);

        return testResourcePoolRequest1.getId();
    }

    public TestResourcePool addTestResourcePool(TestResourcePoolDTO testResourcePool) {
        testResourcePoolService = CommonBeanFactory.getBean(TestResourcePoolService.class);
        String id = IDGenerator.nextStr();
        testResourcePoolService.checkTestResourcePool(testResourcePool);
        TestResourcePoolBlob testResourcePoolBlob = new TestResourcePoolBlob();
        testResourcePoolBlob.setId(id);
        TestResourceDTO testResourceDTO = testResourcePool.getTestResourceDTO();
        testResourcePoolService.checkAndSaveOrgRelation(testResourcePool, id, testResourceDTO);
        testResourcePoolService.checkApiConfig(testResourceDTO, testResourcePool, testResourcePool.getType());
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
}
