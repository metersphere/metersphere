package io.metersphere.system.controller;

import base.BaseTest;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.request.OrganizationMemberRequest;
import io.metersphere.system.request.OrganizationRequest;
import io.metersphere.system.request.ProjectRequest;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.Arrays;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrganizationControllerTests extends BaseTest{

    @Resource
    private MockMvc mockMvc;

    public static final String REQ_PREFIX = "/organization";

    @Test
    @Order(0)
    @Sql(scripts = {"/dml/init_organization.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testListOrganization() throws Exception {
        OrganizationRequest organizationRequest = new OrganizationRequest();
        organizationRequest.setCurrent(1);
        organizationRequest.setPageSize(10);
        organizationRequest.setKeyword("default");
        mockMvc.perform(MockMvcRequestBuilders.post(REQ_PREFIX + "/list")
                .header(SessionConstants.HEADER_TOKEN, sessionId)
                .header(SessionConstants.CSRF_TOKEN, csrfToken)
                .content(JSON.toJSONString(organizationRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @Order(1)
    public void testListAllOrganization() throws Exception {
        OrganizationRequest organizationRequest = new OrganizationRequest();
        organizationRequest.setCurrent(1);
        organizationRequest.setPageSize(10);
        mockMvc.perform(MockMvcRequestBuilders.post(REQ_PREFIX + "/list-all")
                .header(SessionConstants.HEADER_TOKEN, sessionId)
                .header(SessionConstants.CSRF_TOKEN, csrfToken)
                .content(JSON.toJSONString(organizationRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @Order(2)
    public void testGetDefaultOrganization() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(REQ_PREFIX + "/default")
                .header(SessionConstants.HEADER_TOKEN, sessionId)
                .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @Order(3)
    public void testListOrganizationMember() throws Exception {
        OrganizationRequest organizationRequest = new OrganizationRequest();
        organizationRequest.setCurrent(1);
        organizationRequest.setPageSize(10);
        organizationRequest.setKeyword("admin");
        organizationRequest.setOrganizationId("default-organization-2");
        mockMvc.perform(MockMvcRequestBuilders.post(REQ_PREFIX + "/list-member")
                .header(SessionConstants.HEADER_TOKEN, sessionId)
                .header(SessionConstants.CSRF_TOKEN, csrfToken)
                .content(JSON.toJSONString(organizationRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @Order(4)
    public void testAddOrganizationMember() throws Exception {
        OrganizationMemberRequest organizationMemberRequest = new OrganizationMemberRequest();
        organizationMemberRequest.setOrganizationId("default-organization-3");
        organizationMemberRequest.setMemberIds(Arrays.asList("admin", "default-admin"));
        mockMvc.perform(MockMvcRequestBuilders.post(REQ_PREFIX + "/add-member")
                .header(SessionConstants.HEADER_TOKEN, sessionId)
                .header(SessionConstants.CSRF_TOKEN, csrfToken)
                .content(JSON.toJSONString(organizationMemberRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @Order(5)
    public void testRemoveOrganizationMember() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(REQ_PREFIX + "/remove-member/default-organization-3/admin")
                .header(SessionConstants.HEADER_TOKEN, sessionId)
                .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk());
    }

    @Test
    @Order(6)
    public void testGetOrganizationProject() throws Exception {
        ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setCurrent(1);
        projectRequest.setPageSize(10);
        projectRequest.setOrganizationId("default-organization-2");
        mockMvc.perform(MockMvcRequestBuilders.post(REQ_PREFIX + "/list-project")
                .header(SessionConstants.HEADER_TOKEN, sessionId)
                .header(SessionConstants.CSRF_TOKEN, csrfToken)
                .content(JSON.toJSONString(projectRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }
}
