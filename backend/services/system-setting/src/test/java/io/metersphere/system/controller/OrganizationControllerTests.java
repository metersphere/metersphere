package io.metersphere.system.controller;

import base.BaseTest;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.dto.OrganizationDTO;
import io.metersphere.system.request.OrganizationRequest;
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
    @Sql(scripts = {"/ddl/init_organization.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testListOrganization() throws Exception {
        OrganizationRequest organizationRequest = new OrganizationRequest();
        organizationRequest.setCurrent(1);
        organizationRequest.setPageSize(10);
        organizationRequest.setKeyword("default");
        mockMvc.perform(MockMvcRequestBuilders.post(REQ_PREFIX + "/list")
                .content(JSON.toJSONString(organizationRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .header(SessionConstants.HEADER_TOKEN, sessionId)
                .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @Order(1)
    public void testListAllOrganization() throws Exception {
        OrganizationRequest organizationRequest = new OrganizationRequest();
        mockMvc.perform(MockMvcRequestBuilders.post(REQ_PREFIX + "/list-all")
                .content(JSON.toJSONString(organizationRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .header(SessionConstants.HEADER_TOKEN, sessionId)
                .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @Order(2)
    public void testAddOrganization() throws Exception {
        OrganizationDTO organizationDTO = new OrganizationDTO();
        organizationDTO.setName("default-organization");
        organizationDTO.setDescription("default-description");
        mockMvc.perform(MockMvcRequestBuilders.post(REQ_PREFIX + "/add")
                .content(JSON.toJSONString(organizationDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .header(SessionConstants.HEADER_TOKEN, sessionId)
                .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk());
    }

    @Test
    @Order(3)
    public void testUpdateOrganization() throws Exception {
        OrganizationDTO organizationDTO = new OrganizationDTO();
        organizationDTO.setId("default-organization-2");
        organizationDTO.setName("default-X");
        organizationDTO.setDescription("XXX-X");
        mockMvc.perform(MockMvcRequestBuilders.post(REQ_PREFIX + "/update")
                .content(JSON.toJSONString(organizationDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .header(SessionConstants.HEADER_TOKEN, sessionId)
                .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    public void testDeleteOrganization() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(REQ_PREFIX + "/delete/default-organization-2")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @Order(5)
    public void testUnDeleteOrganization() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(REQ_PREFIX + "/undelete/default-organization-2")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @Order(6)
    public void testEnableOrganization() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(REQ_PREFIX + "/enable/default-organization-2")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @Order(7)
    public void testDisableOrganization() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(REQ_PREFIX + "/disable/default-organization-2")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @Order(8)
    public void testGetDefaultOrganization() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(REQ_PREFIX + "/getDefault")
                .header(SessionConstants.HEADER_TOKEN, sessionId)
                .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andDo(MockMvcResultHandlers.print());
    }
}
