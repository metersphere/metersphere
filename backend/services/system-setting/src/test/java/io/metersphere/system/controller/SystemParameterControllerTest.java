package io.metersphere.system.controller;

import com.jayway.jsonpath.JsonPath;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.SystemParameter;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SystemParameterControllerTest {

    @Resource
    private MockMvc mockMvc;

    private static String sessionId;
    private static String csrfToken;

    @BeforeEach
    public void login() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .content("{\"username\":\"admin\",\"password\":\"metersphere\"}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        sessionId = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.data.sessionId");
        csrfToken = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.data.csrfToken");
    }

    @Test
    @Order(1)
    public void testSaveBaseInfo() throws Exception {

        List<SystemParameter> systemParameters = new ArrayList<>();
        SystemParameter systemParameter = new SystemParameter();
        systemParameter.setParamKey("base.url");
        systemParameter.setParamValue("https://baidu.com");
        systemParameter.setType("text");
        SystemParameter parameter = new SystemParameter();
        parameter.setParamKey("base.prometheus.host");
        parameter.setParamValue("http://127.0.0.1:1111");
        parameter.setType("text");
        systemParameters.add(systemParameter);
        systemParameters.add(parameter);

        mockMvc.perform(MockMvcRequestBuilders.post("/system/parameter/save/baseInfo")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(systemParameters))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

    }


    @Test
    @Order(2)
    public void testGetBaseInfo() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/system/parameter/get/baseInfo")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk())
                .andDo(print());

    }

    @Test
    @Order(3)
    public void testGetEmailInfo()throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/system/parameter/get/emailInfo")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk());
    }


    @Test
    @Order(4)
    public void testEditEmailInfo()throws Exception {

        List<SystemParameter> systemParameters = new ArrayList<>();
        SystemParameter systemParameter1 = new SystemParameter();
        systemParameter1.setParamKey("smtp.host");
        systemParameter1.setParamValue("xxx.xxx.com");
        systemParameter1.setType("text");

        SystemParameter systemParameter2 = new SystemParameter();
        systemParameter2.setParamKey("smtp.port");
        systemParameter2.setParamValue("xxx");
        systemParameter2.setType("text");

        SystemParameter systemParameter3 = new SystemParameter();
        systemParameter3.setParamKey("smtp.account");
        systemParameter3.setParamValue("aaa@qq.com");
        systemParameter3.setType("text");


        systemParameters.add(systemParameter1);
        systemParameters.add(systemParameter2);
        systemParameters.add(systemParameter3);

        mockMvc.perform(MockMvcRequestBuilders.post("/system/parameter/edit/emailInfo")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                .content(JSON.toJSONString(systemParameters))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andDo(print());
    }

    @Test
    @Order(4)
    public void testEmailConnec()throws Exception {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("smtp.host","xx");
        hashMap.put("smtp.port","xx");
        hashMap.put("smtp.account","xx");
        hashMap.put("smtp.password","xx");
        hashMap.put("smtp.from","xx");
        hashMap.put("smtp.recipient","xx");
        hashMap.put("smtp.ssl","ture");
        hashMap.put("smtp.tls","false");
        mockMvc.perform(MockMvcRequestBuilders.post("/system/parameter/test/email")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(hashMap))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }
}
