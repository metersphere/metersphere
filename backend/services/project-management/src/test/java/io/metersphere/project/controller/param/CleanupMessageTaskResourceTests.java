package io.metersphere.project.controller.param;

import io.metersphere.project.dto.MessageTaskDTO;
import io.metersphere.project.request.ProjectRobotRequest;
import io.metersphere.project.service.CleanupMessageTaskService;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class CleanupMessageTaskResourceTests extends BaseTest {
    @Resource
    private CleanupMessageTaskService resourceService;

    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_message_task.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testCleanupResource() throws Exception {
        ProjectRobotRequest request = new ProjectRobotRequest();
        request.setCurrent(1);
        request.setPageSize(5);
        List<MessageTaskDTO> list = getList();
        if (CollectionUtils.isNotEmpty(list)) {
            resourceService.deleteResources("test");
        }
        request = new ProjectRobotRequest();
        request.setCurrent(1);
        request.setPageSize(5);
        List<MessageTaskDTO> listAfter = getList();
        Assertions.assertTrue(CollectionUtils.isEmpty(listAfter));
    }

    @Test
    @Order(2)
    public void testCleanupReportResource() throws Exception {
        resourceService.cleanReportResources("test");
    }

    private List<MessageTaskDTO> getList() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/notice/message/task/get/test")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        return JSON.parseArray(JSON.toJSONString(resultHolder.getData()), MessageTaskDTO.class);
    }
}
