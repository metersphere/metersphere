package io.metersphere.system.controller;

import com.jayway.jsonpath.JsonPath;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.UserRole;
import io.metersphere.system.dto.request.GlobalUserRoleRelationQueryRequest;
import io.metersphere.system.dto.request.PermissionSettingUpdateRequest;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GlobalUserRoleControllerTest {

    @Resource
    private MockMvc mockMvc;
    private static String sessionId;
    private static String csrfToken;

    private static final String BASE_URL = "/user/role/global/";

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

    @Test
    void list() throws Exception {
        this.requestPost("list", new GlobalUserRoleRelationQueryRequest());
    }

    @Test
    void getPermissionSetting() throws Exception {
        this.requestGet("permission/setting/1");
    }

    @Test
    void updatePermissionSetting() throws Exception {
        this.requestPost("list", new PermissionSettingUpdateRequest());
    }

    @Test
    void get() throws Exception {
        this.requestGet("get/1");
    }

    @Test
    void add() throws Exception {
        this.requestPost("add", new UserRole());
    }

    @Test
    void update() throws Exception {
        this.requestPost("update", new UserRole());
    }

    @Test
    void delete() throws Exception {
        this.requestGet("delete/1");
    }

    private void requestPost(String url, Object param) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + url)
                .header(SessionConstants.HEADER_TOKEN, sessionId)
                .header(SessionConstants.CSRF_TOKEN, csrfToken)
                .content(JSON.toJSONString(param))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    private MvcResult requestGet(String url) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + url)
                .header(SessionConstants.HEADER_TOKEN, sessionId)
                .header(SessionConstants.CSRF_TOKEN, csrfToken)
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }
}
