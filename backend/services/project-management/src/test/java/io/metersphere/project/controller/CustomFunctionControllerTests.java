package io.metersphere.project.controller;

import io.metersphere.project.domain.CustomFunction;
import io.metersphere.project.domain.CustomFunctionBlob;
import io.metersphere.project.dto.customfunction.CustomFunctionDTO;
import io.metersphere.project.dto.customfunction.request.CustomFunctionPageRequest;
import io.metersphere.project.dto.customfunction.request.CustomFunctionRequest;
import io.metersphere.project.dto.customfunction.request.CustomFunctionUpdateRequest;
import io.metersphere.project.enums.result.ProjectResultCode;
import io.metersphere.project.mapper.CustomFunctionBlobMapper;
import io.metersphere.project.mapper.CustomFunctionMapper;
import io.metersphere.project.service.CustomFunctionService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.controller.handler.result.MsHttpResultCode;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * @author: LAN
 * @date: 2024/1/11 19:02
 * @version: 1.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class CustomFunctionControllerTests extends BaseTest {
    private static final String BASE_PATH = "/project/custom/func/";
    private final static String ADD = BASE_PATH + "add";
    private final static String UPDATE = BASE_PATH + "update";
    private final static String DELETE = BASE_PATH + "delete/";
    private final static String PAGE = BASE_PATH + "page";
    private static final String DETAIL = BASE_PATH + "detail/";
    private static final String STATUS = BASE_PATH + "status";

    private static final String COLUMNS_OPTION = BASE_PATH + "columns-option/";

    private static CustomFunction customFunction;

    @Resource
    private CustomFunctionMapper customFunctionMapper;

    @Resource
    private CustomFunctionBlobMapper customFunctionBlobMapper;
    @Resource
    private CustomFunctionService customFunctionService;

    @Test
    @Order(1)
    public void testAdd() throws Exception {
        LogUtils.info("create custom function test");
        // 创建测试数据
        CustomFunctionRequest request = new CustomFunctionRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setName("test1test1test1test1test1test1");
        request.setStatus("UNDERWAY");
        // 执行方法调用
        request.setName("公共脚本test");
        this.requestPostWithOkAndReturn(ADD, request);
        request.setName("公共脚本test2");
        this.requestPostWithOkAndReturn(ADD, request);
        request.setScript("vars.get(\"variable_name\")\n" +
                "vars.put(\"variable_name\", \"variable_value\")\n" +
                "prev.getResponseHeaders()\n" +
                "prev.getResponseCode()\n" +
                "prev.getResponseDataAsString()");
        request.setParams("[{}]");
        request.setResult("2024-01-11 19:33:39 INFO 48551350 1-1 Thread started: 48551350 1-1\n" +
                "2024-01-11 19:33:39 ERROR 48551350 1-1 Problem in JSR223 script JSR223Sampler, message: {}\n" +
                "Both script file and script text are empty for element:JSR223Sampler\n" +
                "javax.script.ScriptException\n" +
                "   at org.apache.jmeter.util.JSR223TestElement.processFileOrScript(JSR223TestElement.java:240)\n" +
                "   at org.apache.jmeter.protocol.java.sampler.JSR223Sampler.sample(JSR223Sampler.java:72)\n" +
                "   at org.apache.jmeter.threads.JMeterThread.doSampling(JMeterThread.java:672)\n" +
                "   at org.apache.jmeter.threads.JMeterThread.executeSamplePackage(JMeterThread.java:590)\n" +
                "   at org.apache.jmeter.threads.JMeterThread.processSampler(JMeterThread.java:502)\n" +
                "   at org.apache.jmeter.threads.JMeterThread.run(JMeterThread.java:265)\n" +
                "   at java.base/java.lang.Thread.run(Thread.java:840)");

        request.setName("公共脚本test3");
        this.requestPostWithOkAndReturn(ADD, request);
        request.setName("公共脚本test4");
        this.requestPostWithOkAndReturn(ADD, request);
        request.setName("公共脚本test5");
        MvcResult mvcResult = this.requestPostWithOkAndReturn(ADD, request);
        // 校验请求成功数据
        CustomFunction resultData = getResultData(mvcResult, CustomFunction.class);
        customFunction = assertAddCustomFunction(request, resultData.getId());
        // 再插入一条数据，便于修改时重名校验
        request.setName("重名公共脚本test");
        request.setTags(new LinkedHashSet<>(List.of("tag1", "tag2")));
        mvcResult = this.requestPostWithOkAndReturn(ADD, request);
        resultData = getResultData(mvcResult, CustomFunction.class);
        assertAddCustomFunction(request, resultData.getId());
        // @@重名校验异常
        assertErrorCode(this.requestPost(ADD, request), ProjectResultCode.CUSTOM_FUNCTION_ALREADY_EXIST);

        // 校验项目是否存在
        request.setProjectId("111");
        request.setName("test123");
        assertErrorCode(this.requestPost(ADD, request), MsHttpResultCode.NOT_FOUND);

        // @@校验日志
        this.checkLog(customFunction.getId(), OperationLogType.ADD, ADD);
        // @@异常参数校验
        createdGroupParamValidateTest(CustomFunctionRequest.class, ADD);
        // @@校验权限
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setName("permission-st-6");
        requestPostPermissionTest(PermissionConstants.PROJECT_CUSTOM_FUNCTION_ADD, ADD, request);
    }

    private CustomFunction assertAddCustomFunction(CustomFunctionRequest request, String id) {
        CustomFunction customFunctionInfo = customFunctionMapper.selectByPrimaryKey(id);
        CustomFunctionBlob customFunctionBlob = customFunctionBlobMapper.selectByPrimaryKey(id);
        CustomFunction copyCustomFunction = BeanUtils.copyBean(new CustomFunction(), customFunctionInfo);
        BeanUtils.copyBean(copyCustomFunction, request);
        Assertions.assertEquals(customFunctionInfo, copyCustomFunction);
        if(customFunctionBlob != null){
            if(customFunctionBlob.getParams() != null) {
                Assertions.assertEquals(request.getParams(), new String(customFunctionBlob.getParams(), StandardCharsets.UTF_8));
            }
            if(customFunctionBlob.getScript() != null) {
            Assertions.assertEquals(request.getScript(), new String(customFunctionBlob.getScript(), StandardCharsets.UTF_8));
            }
            if(customFunctionBlob.getResult() != null) {
                Assertions.assertEquals(request.getResult(), new String(customFunctionBlob.getResult(), StandardCharsets.UTF_8));
            }
        }
        return customFunctionInfo;
    }

    @Test
    @Order(2)
    public void get() throws Exception {
        // @@请求成功
        MvcResult mvcResult = this.requestGetWithOkAndReturn(DETAIL + customFunction.getId());
        // 校验请求成功数据
        CustomFunctionDTO resultData = getResultData(mvcResult, CustomFunctionDTO.class);
        // 校验数据是否正确
        CustomFunctionDTO customFunctionDTO = new CustomFunctionDTO();
        CustomFunction customFunctionInfo = customFunctionMapper.selectByPrimaryKey(customFunction.getId());
        BeanUtils.copyBean(customFunctionDTO, customFunctionInfo);
        CustomFunctionBlob customFunctionBlob = customFunctionBlobMapper.selectByPrimaryKey(customFunction.getId());
        if (customFunctionBlob != null) {
            customFunctionDTO.setParams(new String(customFunctionBlob.getParams(), StandardCharsets.UTF_8));
            customFunctionDTO.setScript(new String(customFunctionBlob.getScript(), StandardCharsets.UTF_8));
            customFunctionDTO.setResult(new String(customFunctionBlob.getResult(), StandardCharsets.UTF_8));
        }
        Assertions.assertEquals(resultData, customFunctionDTO);

        assertErrorCode(this.requestGet(DETAIL + "111"), MsHttpResultCode.NOT_FOUND);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_CUSTOM_FUNCTION_READ, DETAIL + "111");
    }

    @Test
    @Order(3)
    public void testUpdate() throws Exception {
        LogUtils.info("update custom function test");

        CustomFunctionUpdateRequest request = new CustomFunctionUpdateRequest();
        BeanUtils.copyBean(request, customFunction);
        request.setName("test1test1test1test1test1test1");
        request.setScript("update-vars.get(\"variable_name\")\n" +
                "vars.put(\"variable_name\", \"variable_value\")\n" +
                "prev.getResponseHeaders()\n" +
                "prev.getResponseCode()\n" +
                "prev.getResponseDataAsString()");
        request.setParams("[{\"update-\":\"update-\"}]");
        request.setResult("update-2024-01-11 19:33:39 INFO 48551350 1-1 Thread started: 48551350 1-1\n" +
                "2024-01-11 19:33:39 ERROR 48551350 1-1 Problem in JSR223 script JSR223Sampler, message: {}\n" +
                "Both script file and script text are empty for element:JSR223Sampler\n" +
                "javax.script.ScriptException\n" +
                "   at org.apache.jmeter.util.JSR223TestElement.processFileOrScript(JSR223TestElement.java:240)\n" +
                "   at org.apache.jmeter.protocol.java.sampler.JSR223Sampler.sample(JSR223Sampler.java:72)\n" +
                "   at org.apache.jmeter.threads.JMeterThread.doSampling(JMeterThread.java:672)\n" +
                "   at org.apache.jmeter.threads.JMeterThread.executeSamplePackage(JMeterThread.java:590)\n" +
                "   at org.apache.jmeter.threads.JMeterThread.processSampler(JMeterThread.java:502)\n" +
                "   at org.apache.jmeter.threads.JMeterThread.run(JMeterThread.java:265)\n" +
                "   at java.base/java.lang.Thread.run(Thread.java:840)");


        this.requestPostWithOk(UPDATE, request);
        // 校验请求成功数据
        customFunction = assertUpdateCustomFunction(request, request.getId());

        // 修改 tags
        request.setTags(new LinkedHashSet<>(List.of("tag1", "tag2-update")));
        request.setName("公共脚本test公共脚本test公共脚本test公共脚本test公共脚本test公共脚本test公共脚本test公共脚本test公共脚本test");
        this.requestPostWithOk(UPDATE, request);
        // 校验请求成功数据
        assertUpdateCustomFunction(request, request.getId());

        request.setName("重名公共脚本test");
        // @@重名校验异常
        assertErrorCode(this.requestPost(UPDATE, request), ProjectResultCode.CUSTOM_FUNCTION_ALREADY_EXIST);

        // 校验项目是否存在
        request.setProjectId("111");
        request.setName("test123");
        assertErrorCode(this.requestPost(UPDATE, request), MsHttpResultCode.NOT_FOUND);

        // 校验数据是否存在
        request.setId("111");
        request.setName("test123");
        assertErrorCode(this.requestPost(UPDATE, request), MsHttpResultCode.NOT_FOUND);

        // @@校验日志
        checkLog(customFunction.getId(), OperationLogType.UPDATE, UPDATE);
        // @@异常参数校验
        createdGroupParamValidateTest(CustomFunctionUpdateRequest.class, UPDATE);
        // @@校验权限
        request.setId(customFunction.getId());
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setName("permission-st-6");
        requestPostPermissionTest(PermissionConstants.PROJECT_CUSTOM_FUNCTION_UPDATE, UPDATE, request);

    }

    private CustomFunction assertUpdateCustomFunction(CustomFunctionUpdateRequest request, String id) {
        CustomFunction customFunctionInfo = customFunctionMapper.selectByPrimaryKey(id);
        CustomFunctionBlob customFunctionBlob = customFunctionBlobMapper.selectByPrimaryKey(id);
        CustomFunction copyCustomFunction = new CustomFunction();
        BeanUtils.copyBean(copyCustomFunction, customFunctionInfo);
        BeanUtils.copyBean(copyCustomFunction, request);
        Assertions.assertEquals(customFunctionInfo, copyCustomFunction);
        if(customFunctionBlob != null){
            if(customFunctionBlob.getParams() != null) {
                Assertions.assertEquals(request.getParams(), new String(customFunctionBlob.getParams(), StandardCharsets.UTF_8));
            }
            if(customFunctionBlob.getScript() != null) {
                Assertions.assertEquals(request.getScript(), new String(customFunctionBlob.getScript(), StandardCharsets.UTF_8));
            }
            if(customFunctionBlob.getResult() != null) {
                Assertions.assertEquals(request.getResult(), new String(customFunctionBlob.getResult(), StandardCharsets.UTF_8));
            }
        }
        return customFunctionInfo;
    }

    @Test
    @Order(4)
    public void testUpdateStatus() throws Exception {
        LogUtils.info("update status custom function test");
        // @@请求成功
        CustomFunctionUpdateRequest customFunctionUpdateRequest = new CustomFunctionUpdateRequest();
        customFunctionUpdateRequest.setId(customFunction.getId());
        customFunctionUpdateRequest.setProjectId(customFunction.getProjectId());
        customFunctionUpdateRequest.setStatus("COMPLETED");
        // 执行方法调用
       this.requestPostWithOk(STATUS, customFunctionUpdateRequest);

        customFunction = customFunctionMapper.selectByPrimaryKey(customFunction.getId());
        Assertions.assertEquals(customFunction.getStatus(), customFunctionUpdateRequest.getStatus());
        // @@校验日志
        checkLog(customFunction.getId(), OperationLogType.UPDATE, STATUS);
        customFunctionUpdateRequest.setId("111");

        assertErrorCode(this.requestPost(STATUS, customFunctionUpdateRequest), MsHttpResultCode.NOT_FOUND);

        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_CUSTOM_FUNCTION_UPDATE, STATUS, customFunctionUpdateRequest);
    }



    @Test
    @Order(9)
    public void getPage() throws Exception {
        doCustomFunctionPage("KEYWORD");
        doCustomFunctionPage("FILTER");
    }

    private void doCustomFunctionPage(String search) throws Exception {
        CustomFunctionPageRequest request = new CustomFunctionPageRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setCurrent(1);
        request.setPageSize(10);
        request.setSort(Map.of("createTime", "asc"));
        switch (search) {
            case "KEYWORD" -> configureKeywordSearch(request);
            case "FILTER" -> configureFilterSearch(request);
            default -> {}
        }

        MvcResult mvcResult = this.requestPostWithOkAndReturn(PAGE, request);
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

//    进行中：UNDERWAY,
//    已完成：COMPLETED,


    private void configureKeywordSearch(CustomFunctionPageRequest request) {
        request.setKeyword("test");
        request.setSort(Map.of("status", "asc"));
    }

    private void configureFilterSearch(CustomFunctionPageRequest request) {
        Map<String, List<String>> filters = new HashMap<>();
        request.setSort(Map.of());
        filters.put("status", List.of("UNDERWAY"));
        filters.put("tags", List.of("tag1"));
        request.setFilter(filters);
    }

    @Test
    @Order(10)
    public void columnsOption() throws Exception {
        // @@请求成功
        this.requestGetWithOk(COLUMNS_OPTION + DEFAULT_PROJECT_ID);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_CUSTOM_FUNCTION_READ, COLUMNS_OPTION + "/" + DEFAULT_PROJECT_ID);
    }

    @Test
    @Order(11)
    public void testUncoveredFunc() {
        // 在项目管理模块没有调用，手动调用
        customFunctionService.getBlobByIds(List.of());
        customFunctionService.getByIds(List.of());
        Assertions.assertEquals(customFunctionService.getBlobByIds(List.of(customFunction.getId())).size(), 1);
        Assertions.assertEquals(customFunctionService.getByIds(List.of(customFunction.getId())).size(), 1);
    }

    @Test
    @Order(12)
    public void testDel() throws Exception {
        LogUtils.info("delete custom function test");
        // @@请求成功
        this.requestGetWithOk(DELETE + customFunction.getId());
        checkLog(customFunction.getId(), OperationLogType.DELETE);
        CustomFunction customFunctionInfo = customFunctionMapper.selectByPrimaryKey(customFunction.getId());
        Assertions.assertNull(customFunctionInfo);

        // blob是否删除
        CustomFunctionBlob customFunctionBlob = customFunctionBlobMapper.selectByPrimaryKey(customFunction.getId());
        Assertions.assertNull(customFunctionBlob);

        checkLog(customFunction.getId(), OperationLogType.DELETE);
        assertErrorCode(this.requestGet(DELETE + "111"), MsHttpResultCode.NOT_FOUND);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_CUSTOM_FUNCTION_DELETE, DELETE + "222");
    }

}
