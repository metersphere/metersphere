package io.metersphere.system.controller;

import io.metersphere.sdk.base.BaseTest;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.system.controller.param.ServiceIntegrationUpdateRequestDefinition;
import io.metersphere.system.domain.ServiceIntegration;
import io.metersphere.system.mapper.ServiceIntegrationMapper;
import io.metersphere.system.request.ServiceIntegrationUpdateRequest;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;

/**
 * @author jianxing
 * @date : 2023-8-9
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceIntegrationControllerTests extends BaseTest {
    private static final String BASE_PATH = "/service/integration/";
    private static final String LIST = "/list/{0}";
    private static final String VALIDATE_GET = "/validate/{0}";
    private static final String VALIDATE_POST = "/validate";
    private static final String SCRIPT_GET = "/script/{0}";
    @Resource
    private ServiceIntegrationMapper serviceIntegrationMapper;
    private static ServiceIntegration addServiceIntegration;
    @Override
    protected String getBasePath() {
        return BASE_PATH;
    }

    @Test
    public void list() throws Exception {
        // @@请求成功
        this.requestGetWithOkAndReturn(LIST, "orgId");
//        List<ServiceIntegration> serviceIntegrationList = getResultDataArray(mvcResult, ServiceIntegration.class);
        // todo 校验数据是否正确
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.SYSTEM_SERVICE_INTEGRATION_READ, LIST,"orgId");
    }

    @Test
    @Order(0)
    public void add() throws Exception {
        // @@请求成功
        ServiceIntegrationUpdateRequest request = new ServiceIntegrationUpdateRequest();
        request.setEnable(false);
        request.setPluginId("pluginId");
        request.setConfiguration(new HashMap<>());
        request.setOrganizationId("orgId");
        // todo 填充数据
        MvcResult mvcResult = this.requestPostWithOkAndReturn(DEFAULT_ADD, request);
        ServiceIntegration resultData = getResultData(mvcResult, ServiceIntegration.class);
        ServiceIntegration serviceIntegration = serviceIntegrationMapper.selectByPrimaryKey(resultData.getId());
        this.addServiceIntegration= serviceIntegration;
        // todo 校验请求成功数据
        // @@校验日志
        checkLog(this.addServiceIntegration.getId(), OperationLogType.ADD);
        // @@异常参数校验
        createdGroupParamValidateTest(ServiceIntegrationUpdateRequestDefinition.class, DEFAULT_ADD);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.SYSTEM_SERVICE_INTEGRATION_ADD, DEFAULT_ADD, request);
    }

    @Test
    @Order(1)
    public void update() throws Exception {
        // @@请求成功
        ServiceIntegrationUpdateRequest request = new ServiceIntegrationUpdateRequest();
        request.setId(addServiceIntegration.getId());
        request.setEnable(false);
        request.setPluginId("pluginId");
        request.setConfiguration(new HashMap<>());
        request.setOrganizationId("orgId");
        // todo 填充数据
        this.requestPostWithOk(DEFAULT_UPDATE, request);
        // 校验请求成功数据
//        ServiceIntegration serviceIntegration = serviceIntegrationMapper.selectByPrimaryKey(request.getId());
        // todo 校验请求成功数据
        // @@校验日志
        checkLog(request.getId(), OperationLogType.UPDATE);
        // @@异常参数校验
        updatedGroupParamValidateTest(ServiceIntegrationUpdateRequestDefinition.class, DEFAULT_UPDATE);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.SYSTEM_SERVICE_INTEGRATION_UPDATE, DEFAULT_UPDATE, request);
    }

    @Test
    public void delete() throws Exception {
        // @@请求成功
        this.requestGetWithOk(DEFAULT_DELETE, addServiceIntegration.getId());
        // todo 校验请求成功数据
        // @@校验日志
        checkLog(addServiceIntegration.getId(), OperationLogType.DELETE);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.SYSTEM_SERVICE_INTEGRATION_DELETE, DEFAULT_DELETE, addServiceIntegration.getId());
    }

    @Test
    public void validateGet() throws Exception {
        // @@请求成功
        this.requestGetWithOk(VALIDATE_GET, addServiceIntegration.getId());
        // todo 校验请求成功数据
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.SYSTEM_SERVICE_INTEGRATION_UPDATE, VALIDATE_GET, addServiceIntegration.getId());
    }

    @Test
    public void validatePost() throws Exception {
        // @@请求成功
        this.requestPostWithOk(VALIDATE_POST, new HashMap<>());
        // todo 校验请求成功数据
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.SYSTEM_SERVICE_INTEGRATION_UPDATE, VALIDATE_POST, new HashMap<>());
    }

    @Test
    public void getPluginScript() throws Exception {
        // @@请求成功
        this.requestGetWithOk(SCRIPT_GET, "pluginId");
        // todo 校验请求成功数据
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.SYSTEM_SERVICE_INTEGRATION_READ, SCRIPT_GET, "pluginId");
    }
}