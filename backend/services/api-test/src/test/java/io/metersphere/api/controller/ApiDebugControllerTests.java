package io.metersphere.api.controller;

import io.metersphere.api.controller.param.ApiDebugAddRequestDefinition;
import io.metersphere.api.controller.param.ApiDebugUpdateRequestDefinition;
import io.metersphere.api.domain.ApiDebug;
import io.metersphere.api.domain.ApiDebugBlob;
import io.metersphere.api.dto.debug.ApiDebugAddRequest;
import io.metersphere.api.dto.debug.ApiDebugDTO;
import io.metersphere.api.dto.debug.ApiDebugSimpleDTO;
import io.metersphere.api.dto.debug.ApiDebugUpdateRequest;
import io.metersphere.api.mapper.ApiDebugBlobMapper;
import io.metersphere.api.mapper.ApiDebugMapper;
import io.metersphere.api.util.ApiDataUtils;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.api.request.http.MsHTTPElement;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.log.constants.OperationLogType;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static io.metersphere.api.controller.result.ApiResultCode.API_DEBUG_EXIST;
import static io.metersphere.system.controller.handler.result.MsHttpResultCode.NOT_FOUND;

/**
 * @author jianxing
 * @date : 2023-11-7
 */
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApiDebugControllerTests extends BaseTest {
    private static final String BASE_PATH = "/api/debug/";
    protected static final String DEFAULT_LIST = "list/{0}";
    protected static final String HTTP_PROTOCOL = "HTTP";

    @Resource
    private ApiDebugMapper apiDebugMapper;
    @Resource
    private ApiDebugBlobMapper apiDebugBlobMapper;
    private static ApiDebug addApiDebug;
    private static ApiDebug anotherAddApiDebug;
    @Override
    protected String getBasePath() {
        return BASE_PATH;
    }

    @Test
    @Order(0)
    public void listEmpty() throws Exception {
        // @@校验没有数据的情况
        this.requestGetWithOk(DEFAULT_LIST, HTTP_PROTOCOL);
    }

    @Test
    @Order(1)
    public void add() throws Exception {
        // @@请求成功
        ApiDebugAddRequest request = new ApiDebugAddRequest();
        request.setPath("http://test.com");
        request.setMethod("GET");
        request.setName("test");
        request.setProtocol(HTTP_PROTOCOL);
        request.setModuleId("default");
        request.setProjectId(DEFAULT_PROJECT_ID);
        MsHTTPElement msHttpElement = MsHTTPElementTest.getMsHttpElement();
        request.setRequest(ApiDataUtils.toJSONString(msHttpElement));
        MvcResult mvcResult = this.requestPostWithOkAndReturn(DEFAULT_ADD, request);
        // 校验请求成功数据
        ApiDebug resultData = getResultData(mvcResult, ApiDebug.class);
        this.addApiDebug = assertUpdateApiDebug(request, msHttpElement, resultData.getId());

        // 再插入一条数据，便于修改时重名校验
        request.setName("test1");
        mvcResult = this.requestPostWithOkAndReturn(DEFAULT_ADD, request);
        resultData = getResultData(mvcResult, ApiDebug.class);
        this.anotherAddApiDebug = assertUpdateApiDebug(request, msHttpElement, resultData.getId());

        // @@重名校验异常
        assertErrorCode(this.requestPost(DEFAULT_ADD, request), API_DEBUG_EXIST);
        // 校验项目是否存在
        request.setProjectId("111");
        assertErrorCode(this.requestPost(DEFAULT_ADD, request), NOT_FOUND);

        // @@校验日志
         checkLog(this.addApiDebug.getId(), OperationLogType.ADD);
        // @@异常参数校验
        createdGroupParamValidateTest(ApiDebugAddRequestDefinition.class, DEFAULT_ADD);
        // @@校验权限
        request.setProjectId(DEFAULT_PROJECT_ID);
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEBUG_ADD, DEFAULT_ADD, request);
    }

    private ApiDebug assertUpdateApiDebug(Object request, MsHTTPElement msHttpElement, String id) {
        ApiDebug apiDebug = apiDebugMapper.selectByPrimaryKey(id);
        ApiDebugBlob apiDebugBlob = apiDebugBlobMapper.selectByPrimaryKey(id);
        ApiDebug copyApiDebug = BeanUtils.copyBean(new ApiDebug(), apiDebug);
        copyApiDebug = BeanUtils.copyBean(copyApiDebug, request);
        Assertions.assertEquals(apiDebug, copyApiDebug);
        ApiDataUtils.setResolver(MsHTTPElement.class);
        Assertions.assertEquals(msHttpElement, ApiDataUtils.parseObject(new String(apiDebugBlob.getRequest()), AbstractMsTestElement.class));
        return apiDebug;
    }

    @Test
    @Order(2)
    public void update() throws Exception {
        // @@请求成功
        ApiDebugUpdateRequest request = new ApiDebugUpdateRequest();
        request.setId(addApiDebug.getId());
        request.setPath("http://tesat.com");
        request.setName("test1");
        request.setMethod("POST");
        request.setModuleId("default1");
        MsHTTPElement msHttpElement = MsHTTPElementTest.getMsHttpElement();
        msHttpElement.setName("test1");
        request.setRequest(ApiDataUtils.toJSONString(msHttpElement));
        this.requestPostWithOk(DEFAULT_UPDATE, request);
        // 校验请求成功数据
        assertUpdateApiDebug(request, msHttpElement, request.getId());

        // @@重名校验异常
        request.setModuleId("default");
        assertErrorCode(this.requestPost(DEFAULT_UPDATE, request), API_DEBUG_EXIST);

        // @@校验日志
        checkLog(request.getId(), OperationLogType.UPDATE);
        // @@异常参数校验
        updatedGroupParamValidateTest(ApiDebugUpdateRequestDefinition.class, DEFAULT_UPDATE);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEBUG_UPDATE, DEFAULT_UPDATE, request);
    }
    @Test
    @Order(3)
    public void list() throws Exception {
        // @@请求成功
        MvcResult mvcResult = this.requestGetWithOk(DEFAULT_LIST, HTTP_PROTOCOL)
                .andReturn();
        // 校验数据是否正确
        List<ApiDebugSimpleDTO> apiDebugList = getResultDataArray(mvcResult, ApiDebugSimpleDTO.class);
        Assertions.assertEquals(apiDebugList.size(), 2);
        Assertions.assertEquals(apiDebugList.get(0), BeanUtils.copyBean(new ApiDebugSimpleDTO(),
                apiDebugMapper.selectByPrimaryKey(addApiDebug.getId())));
        Assertions.assertEquals(apiDebugList.get(1),
                BeanUtils.copyBean(new ApiDebugSimpleDTO(), apiDebugMapper.selectByPrimaryKey(anotherAddApiDebug.getId())));

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_DEBUG_READ, DEFAULT_LIST, HTTP_PROTOCOL);
    }


    @Test
    @Order(4)
    public void get() throws Exception {
        // @@请求成功
        MvcResult mvcResult = this.requestGetWithOk(DEFAULT_GET, addApiDebug.getId())
                .andReturn();
        ApiDataUtils.setResolver(MsHTTPElement.class);
        ApiDebugDTO apiDebugDTO = ApiDataUtils.parseObject(JSON.toJSONString(parseResponse(mvcResult).get("data")), ApiDebugDTO.class);
        // 校验数据是否正确
        ApiDebugDTO copyApiDebugDTO = BeanUtils.copyBean(new ApiDebugDTO(), apiDebugMapper.selectByPrimaryKey(addApiDebug.getId()));
        ApiDebugBlob apiDebugBlob = apiDebugBlobMapper.selectByPrimaryKey(addApiDebug.getId());
        copyApiDebugDTO.setRequest(ApiDataUtils.parseObject(new String(apiDebugBlob.getRequest()), AbstractMsTestElement.class));
        Assertions.assertEquals(apiDebugDTO, copyApiDebugDTO);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_DEBUG_READ, DEFAULT_GET, apiDebugDTO.getId());
    }

    @Test
    @Order(5)
    public void delete() throws Exception {
        // @@请求成功
        this.requestGetWithOk(DEFAULT_DELETE, addApiDebug.getId());
        // 校验请求成功数据
        Assertions.assertNull(apiDebugMapper.selectByPrimaryKey(addApiDebug.getId()));
        Assertions.assertNull(apiDebugBlobMapper.selectByPrimaryKey(addApiDebug.getId()));
        // @@校验日志
        checkLog(addApiDebug.getId(), OperationLogType.DELETE);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_DEBUG_DELETE, DEFAULT_DELETE, addApiDebug.getId());
    }
}