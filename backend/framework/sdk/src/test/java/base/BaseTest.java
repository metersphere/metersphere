package base;

import base.param.InvalidateParamInfo;
import base.param.ParamGeneratorFactory;
import com.jayway.jsonpath.JsonPath;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.controller.handler.result.IResultCode;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Pager;
import io.metersphere.sdk.domain.OperationLogExample;
import io.metersphere.sdk.mapper.OperationLogMapper;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class BaseTest {
    @Resource
    private MockMvc mockMvc;
    protected static String sessionId;
    protected static String csrfToken;
    @Resource
    private OperationLogMapper operationLogMapper;

    /**
     * 可以重写该方法定义 BASE_PATH
     */
    protected String getBasePath() {
        return StringUtils.EMPTY;
    }

    @BeforeEach
    public void login() throws Exception {
        if (StringUtils.isAnyBlank(sessionId, csrfToken)) {
            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/login")
                            .content("{\"username\":\"admin\",\"password\":\"metersphere\"}")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn();
            sessionId = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.data.sessionId");
            csrfToken = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.data.csrfToken");
        }
    }

    protected MockHttpServletRequestBuilder getPostRequestBuilder(String url, Object param, Object... uriVariables) {
        return MockMvcRequestBuilders.post(getBasePath() + url, uriVariables)
                .header(SessionConstants.HEADER_TOKEN, sessionId)
                .header(SessionConstants.CSRF_TOKEN, csrfToken)
                .content(JSON.toJSONString(param))
                .contentType(MediaType.APPLICATION_JSON);
    }

    protected MockHttpServletRequestBuilder getRequestBuilder(String url, Object... uriVariables) {
        return MockMvcRequestBuilders.get(getBasePath() + url, uriVariables)
                .header(SessionConstants.HEADER_TOKEN, sessionId)
                .header(SessionConstants.CSRF_TOKEN, csrfToken);
    }

    protected ResultActions requestPost(String url, Object param, Object... uriVariables) throws Exception {
        return mockMvc.perform(getPostRequestBuilder(url, param, uriVariables))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    protected MvcResult requestPostAndReturn(String url, Object param, Object... uriVariables) throws Exception {
        return this.requestPost(url, param, uriVariables).andReturn();
    }

    protected ResultActions requestGet(String url, Object... uriVariables) throws Exception {
        return mockMvc.perform(getRequestBuilder(url, uriVariables))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    protected MvcResult requestGetAndReturn(String url, Object... uriVariables) throws Exception {
        return this.requestGet(url, uriVariables).andReturn();
    }

    protected ResultActions requestGetWithOk(String url, Object... uriVariables) throws Exception {
        return mockMvc.perform(getRequestBuilder(url, uriVariables))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    protected MvcResult requestGetWithOkAndReturn(String url, Object... uriVariables) throws Exception {
        return this.requestGetWithOk(url, uriVariables).andReturn();
    }

    protected ResultActions requestPostWithOk(String url, Object param, Object... uriVariables) throws Exception {
        return mockMvc.perform(getPostRequestBuilder(url, param, uriVariables))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    protected MvcResult requestPostWithOkAndReturn(String url, Object param, Object... uriVariables) throws Exception {
        return this.requestPostWithOk(url, param, uriVariables).andReturn();
    }

    protected <T> T getResultData(MvcResult mvcResult, Class<T> clazz) throws Exception {
        Object data = JSON.parseMap(mvcResult.getResponse().getContentAsString()).get("data");
        return JSON.parseObject(JSON.toJSONString(data), clazz);
    }

    protected <T> List<T> getResultDataArray(MvcResult mvcResult, Class<T> clazz) throws Exception {
        Object data = JSON.parseMap(mvcResult.getResponse().getContentAsString()).get("data");
        return JSON.parseArray(JSON.toJSONString(data), clazz);
    }

    /**
     * 校验错误响应码
     */
    protected void assertErrorCode(ResultActions resultActions, IResultCode resultCode) throws Exception {
        resultActions
                .andExpect(
                        jsonPath("$.code")
                                .value(resultCode.getCode())
                );
    }

    protected <T> Pager<List<T>> getPageResult(MvcResult mvcResult, Class<T> clazz) throws Exception {
        Map<String, Object> pagerResult = (Map<String, Object>) JSON.parseMap(mvcResult.getResponse().getContentAsString()).get("data");
        List<T> list = JSON.parseArray(JSON.toJSONString(pagerResult.get("list")), clazz);
        Pager pager = new Pager();
        pager.setPageSize(Long.valueOf(pagerResult.get("pageSize").toString()));
        pager.setCurrent(Long.valueOf(pagerResult.get("current").toString()));
        pager.setTotal(Long.valueOf(pagerResult.get("total").toString()));
        pager.setList(list);
        return pager;
    }

    protected void checkLog(String resourceId, OperationLogType operationLogType) throws Exception {
        OperationLogExample example = new OperationLogExample();
        example.createCriteria().andSourceIdEqualTo(resourceId).andTypeEqualTo(operationLogType.name());
        operationLogMapper.selectByExample(example).stream()
                .filter(operationLog -> operationLog.getSourceId().equals(resourceId))
                .filter(operationLog -> operationLog.getType().equals(operationLogType.name()))
                .filter(operationLog -> StringUtils.isNotBlank(operationLog.getProjectId()))
                .filter(operationLog -> StringUtils.isNotBlank(operationLog.getModule()))
                .findFirst()
                .orElseThrow(() -> new Exception("日志不存在，请补充操作日志"));
    }

    /**
     * Created 分组参数校验
     */
    protected <T> void createdGroupParamValidateTest(Class<T> clazz, String path) throws Exception {
        paramValidateTest(Created.class, clazz, path);
    }

    /**
     * Updated 分组参数校验
     */
    protected <T> void updatedGroupParamValidateTest(Class<T> clazz, String path) throws Exception {
        paramValidateTest(Updated.class, clazz, path);
    }

    /**
     * 没有分组的参数校验
     */
    protected <T> void paramValidateTest(Class<T> clazz, String path) throws Exception {
        paramValidateTest(null, clazz, path);
    }

    /**
     * 根据指定的参数定义，自动生成异常参数，进行参数校验的测试
     *
     * @param group 分组
     * @param clazz 参数类名
     * @param path  请求路径
     * @param <T>   参数类型
     * @throws Exception
     */
    private <T> void paramValidateTest(Class group, Class<T> clazz, String path) throws Exception {
        System.out.println("paramValidateTest-start: ====================================");
        System.out.println("url: " + getBasePath() + path);

        List<InvalidateParamInfo> invalidateParamInfos = ParamGeneratorFactory.generateInvalidateParams(group, clazz);
        for (InvalidateParamInfo invalidateParamInfo : invalidateParamInfos) {
            System.out.println("valid: " + invalidateParamInfo.getName() + " " + invalidateParamInfo.getAnnotation().getSimpleName());
            System.out.println("param: " + JSON.toJSONString(invalidateParamInfo.getValue()));
            MvcResult mvcResult = this.requestPostAndReturn(path, invalidateParamInfo.getValue());
            // 校验错误是否是参数错误
            Assertions.assertEquals(400, mvcResult.getResponse().getStatus());
            Map resultData = getResultData(mvcResult, Map.class);
            System.out.println("result: " + resultData);
            // 校验错误信息中包含了该字段
            Assertions.assertTrue(resultData.containsKey(invalidateParamInfo.getName()));
        }
        System.out.println("paramValidateTest-end: ====================================");
    }
}
