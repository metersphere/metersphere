package io.metersphere.system.controller;

import io.metersphere.sdk.constants.ParamConstants;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.domain.SystemParameter;
import io.metersphere.system.job.CleanHistoryJob;
import io.metersphere.system.job.CleanLogJob;
import io.metersphere.system.mapper.SystemParameterMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SystemParameterControllerTests extends BaseTest {


    public static final String BASE_INFO_SAVE_URL = "/system/parameter/save/base-info";

    public static final String BASE_INFO_URL = "/system/parameter/get/base-info";

    public static final String EMAIL_INFO_URL = "/system/parameter/get/email-info";

    public static final String EMAIL_INFO_SAVE_URL = "/system/parameter/edit/email-info";
    public static final String UPLOAD_CONFIG_SAVE_URL = "/system/parameter/edit/upload-config";


    public static final String EMAIL_INFO_TEST_CONNECT_URL = "/system/parameter/test/email";

    public static final String SAVE_BASE_URL = "/system/parameter/save/base-url";
    public static final String BASE_URL = "http://www.baidu.com";

    private static final ResultMatcher ERROR_REQUEST_MATCHER = status().is5xxServerError();

    public static final String LOG_CONFIG_URL = "/system/parameter/edit/clean-config";
    public static final String GET_LOG_CONFIG_URL = "/system/parameter/get/clean-config";
    public static final String GET_API_CONCURRENT_CONFIG_URL = "/system/parameter/get/api-concurrent-config";

    @Resource
    private CleanHistoryJob cleanHistoryJob;
    @Resource
    private CleanLogJob cleanLogJob;
    @Resource
    private SystemParameterMapper systemParameterMapper;

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
    @Order(4)
    public void testGetEmailInfo() throws Exception {
        this.requestGet(EMAIL_INFO_URL);
        requestGetPermissionTest(PermissionConstants.SYSTEM_PARAMETER_SETTING_BASE_READ, EMAIL_INFO_URL);
    }


    @Test
    @Order(3)
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

    @Test
    @Order(6)
    public void testEditUploadConfigInfo() throws Exception {
        List<SystemParameter> systemParameters = new ArrayList<>() {{
            add(new SystemParameter() {{
                setParamKey("upload.file.size");
                setParamValue("10");
                setType("text");
            }});
        }};
        this.requestPost(UPLOAD_CONFIG_SAVE_URL, systemParameters);
        requestPostPermissionTest(PermissionConstants.SYSTEM_PARAMETER_SETTING_BASE_READ_UPDATE, UPLOAD_CONFIG_SAVE_URL, systemParameters);
        systemParameters = new ArrayList<>() {{
            add(new SystemParameter() {{
                setParamKey("upload.file.size");
                setParamValue("20");
                setType("text");
            }});
        }};
        this.requestPost(UPLOAD_CONFIG_SAVE_URL, systemParameters);
    }

    @Test
    @Order(7)
    public void testEditUploadConfigInfoError() throws Exception {
        List<SystemParameter> systemParameters = new ArrayList<>() {{
            add(new SystemParameter() {{
                setParamKey("upload.file");
                setParamValue("10");
                setType("text");
            }});
        }};
        this.requestPost(UPLOAD_CONFIG_SAVE_URL, systemParameters, status().is5xxServerError());
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


    @Test
    @Order(6)
    @Sql(scripts = {"/dml/init_operation_history_test.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testCleanConfig() throws Exception {
        //定时任务覆盖
        cleanHistoryJob.cleanupLog();
        cleanLogJob.cleanupLog();

        this.requestGet(GET_LOG_CONFIG_URL);
        List<SystemParameter> systemParameters = new ArrayList<>() {{
            add(new SystemParameter() {{
                setParamKey("cleanConfig.operation.log");
                setParamValue("1D");
                setType("text");
            }});
            add(new SystemParameter() {{
                setParamKey("cleanConfig.operation.history");
                setParamValue("10");
                setType("text");
            }});
        }};
        this.requestPost(LOG_CONFIG_URL, systemParameters);

        List<SystemParameter> updateSystemParameters = new ArrayList<>() {{
            add(new SystemParameter() {{
                setParamKey("cleanConfig.operation.log");
                setParamValue("2D");
                setType("text");
            }});
            add(new SystemParameter() {{
                setParamKey("cleanConfig.operation.history");
                setParamValue("5");
                setType("text");
            }});
        }};
        this.requestPost(LOG_CONFIG_URL, updateSystemParameters);
        this.requestGet(GET_LOG_CONFIG_URL);

        cleanHistoryJob.cleanupLog();
        cleanLogJob.cleanupLog();

        updateSystemParameters = new ArrayList<>() {{
            add(new SystemParameter() {{
                setParamKey("cleanConfig.operation.log");
                setParamValue("2D");
                setType("text");
            }});
            add(new SystemParameter() {{
                setParamKey("cleanConfig.operation.history");
                setParamValue("100001");
                setType("text");
            }});
        }};
        this.requestPost(LOG_CONFIG_URL, updateSystemParameters);
        this.requestGet(GET_LOG_CONFIG_URL);

        cleanHistoryJob.cleanupLog();
        cleanLogJob.cleanupLog();

        //覆盖代码
        List<SystemParameter> updateConfigMonth = new ArrayList<>() {{
            add(new SystemParameter() {{
                setParamKey("cleanConfig.operation.log");
                setParamValue("2M");
                setType("text");
            }});
            add(new SystemParameter() {{
                setParamKey("cleanConfig.operation.history");
                setParamValue("5");
                setType("text");
            }});
        }};
        this.requestPost(LOG_CONFIG_URL, updateConfigMonth);
        cleanLogJob.cleanupLog();

        List<SystemParameter> updateConfigYear = new ArrayList<>() {{
            add(new SystemParameter() {{
                setParamKey("cleanConfig.operation.log");
                setParamValue("2Y");
                setType("text");
            }});
            add(new SystemParameter() {{
                setParamKey("cleanConfig.operation.history");
                setParamValue("5");
                setType("text");
            }});
        }};
        this.requestPost(LOG_CONFIG_URL, updateConfigYear);
        cleanLogJob.cleanupLog();
    }


    @Test
    @Order(7)
    public void testGetApiConcurrentConfig() throws Exception {
        this.requestGet(GET_API_CONCURRENT_CONFIG_URL);

        SystemParameter parameter = new SystemParameter() {{
            setParamKey(ParamConstants.ApiConcurrentConfig.API_CONCURRENT_CONFIG.getValue());
            setParamValue("4");
            setType("String");
        }};
        systemParameterMapper.insert(parameter);
        this.requestGet(GET_API_CONCURRENT_CONFIG_URL);
    }
}
