package io.metersphere.system.controller;

import io.metersphere.sdk.base.BaseTest;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.SystemParameter;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SystemParameterControllerTests extends BaseTest {


    public static final String BASE_INFO_SAVE_URL = "/system/parameter/save/base-info";

    public static final String BASE_INFO_URL = "/system/parameter/get/base-info";

    public static final String EMAIL_INFO_URL = "/system/parameter/get/email-info";

    public static final String EMAIL_INFO_SAVE_URL = "/system/parameter/edit/email-info";


    public static final String EMAIL_INFO_TEST_CONNECT_URL = "/system/parameter/test/email";

    public static final String SAVE_BASE_URL = "/system/parameter/save/base-url";
    public static final String BASE_URL = "http://www.baidu.com";

    private static final ResultMatcher ERROR_REQUEST_MATCHER = status().is5xxServerError();


    @Test
    @Order(1)
    public void testSaveBaseUrl() throws Exception {
        this.requestGet(SAVE_BASE_URL + "?baseUrl=" + BASE_URL);
    }


    @Test
    @Order(2)
    public void testSaveBaseInfo() throws Exception {

        List<SystemParameter> systemParameters = new ArrayList<>() {{
            add(new SystemParameter() {{
                setParamKey("base.url");
                setParamValue("https://baidu.com");
                setType("text");
            }});
            add(new SystemParameter() {{
                setParamKey("base.prometheus.host");
                setParamValue("http://127.0.0.1:1111");
                setType("text");
            }});
        }};

        this.requestPost(BASE_INFO_SAVE_URL, systemParameters);
        requestPostPermissionTest(PermissionConstants.SYSTEM_PARAMETER_SETTING_BASE_READ_UPDATE, BASE_INFO_SAVE_URL, systemParameters);
    }


    @Test
    @Order(2)
    public void testGetBaseInfo() throws Exception {
        this.requestGet(BASE_INFO_URL);
        requestGetPermissionTest(PermissionConstants.SYSTEM_PARAMETER_SETTING_BASE_READ, BASE_INFO_URL);
    }

    @Test
    @Order(3)
    public void testGetEmailInfo() throws Exception {
        this.requestGet(EMAIL_INFO_URL);
        requestGetPermissionTest(PermissionConstants.SYSTEM_PARAMETER_SETTING_BASE_READ, EMAIL_INFO_URL);
    }


    @Test
    @Order(4)
    public void testEditEmailInfo() throws Exception {

        List<SystemParameter> systemParameters = new ArrayList<>() {{
            add(new SystemParameter() {{
                setParamKey("smtp.host");
                setParamValue("https://baidu.com");
                setType("text");
            }});
            add(new SystemParameter() {{
                setParamKey("smtp.port");
                setParamValue("8080");
                setType("text");
            }});
            add(new SystemParameter() {{
                setParamKey("smtp.account");
                setParamValue("aaa@fit2cloud.com");
                setType("text");
            }});
            add(new SystemParameter() {{
                setParamKey("smtp.ssl");
                setParamValue("true");
                setType("text");
            }});
        }};
        this.requestPost(EMAIL_INFO_SAVE_URL, systemParameters);
        requestPostPermissionTest(PermissionConstants.SYSTEM_PARAMETER_SETTING_BASE_READ_UPDATE, EMAIL_INFO_SAVE_URL, systemParameters);
    }

    @Test
    @Order(4)
    public void testEmailConnect() throws Exception {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("smtp.host", "https://baidu.com");
        hashMap.put("smtp.port", "80");
        hashMap.put("smtp.account", "aaa@fit2cloud.com");
        hashMap.put("smtp.password", "test");
        hashMap.put("smtp.from", "aaa@fit2cloud.com");
        hashMap.put("smtp.recipient", "aaa@fit2cloud.com");
        hashMap.put("smtp.ssl", "true");
        hashMap.put("smtp.tsl", "false");
        this.requestPost(EMAIL_INFO_TEST_CONNECT_URL, hashMap, ERROR_REQUEST_MATCHER);
    }


    @Test
    @Order(5)
    public void testSaveBaseInfoError() throws Exception {
        List<SystemParameter> systemParameters = new ArrayList<>() {{
            add(new SystemParameter() {{
                setParamKey("base.url");
                setParamValue("https://baidu.com");
                setType("text");
            }});
            add(new SystemParameter() {{
                setParamKey("");
                setParamValue("");
                setType("text");
            }});
        }};
        this.requestPost(BASE_INFO_SAVE_URL, systemParameters);
        requestPostPermissionTest(PermissionConstants.SYSTEM_PARAMETER_SETTING_BASE_READ_UPDATE, BASE_INFO_SAVE_URL, systemParameters);
    }


    private MvcResult requestPost(String url, Object param) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(param))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                
                .andReturn();
    }

    private MvcResult requestGet(String url) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk()).andReturn();
    }

    private void requestPost(String url, Object param, ResultMatcher resultMatcher) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(param))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(resultMatcher)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
