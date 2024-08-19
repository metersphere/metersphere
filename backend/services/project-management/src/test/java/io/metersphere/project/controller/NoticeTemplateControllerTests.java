package io.metersphere.project.controller;

import io.metersphere.project.dto.MessageTemplateFieldDTO;
import io.metersphere.project.dto.MessageTemplateResultDTO;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.notice.constants.NoticeConstants;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NoticeTemplateControllerTests extends BaseTest {

    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_project_template.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void getTemplateFieldsSuccess() throws Exception {
        List<String> typeList = new ArrayList<>();
        getTypeList(typeList);
        for (String s : typeList) {
            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/notice/template/get/fields/project-template-test-1")
                            .header(SessionConstants.HEADER_TOKEN, sessionId)
                            .header(SessionConstants.CSRF_TOKEN, csrfToken)
                            .param("taskType", s))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
            String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
            ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
            MessageTemplateResultDTO messageTemplateResultDTO = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), MessageTemplateResultDTO.class);
            List<MessageTemplateFieldDTO> projectList = messageTemplateResultDTO.getFieldList();
        }
    }

    private static void getTypeList(List<String> typeList) {
        typeList.add(NoticeConstants.TaskType.API_DEFINITION_TASK);
        typeList.add(NoticeConstants.TaskType.API_SCENARIO_TASK);
        typeList.add(NoticeConstants.TaskType.TEST_PLAN_TASK);
        typeList.add(NoticeConstants.TaskType.CASE_REVIEW_TASK);
        typeList.add(NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK);
        typeList.add(NoticeConstants.TaskType.BUG_TASK);
        typeList.add(NoticeConstants.TaskType.JENKINS_TASK);
        typeList.add(NoticeConstants.TaskType.SCHEDULE_TASK);
        typeList.add(NoticeConstants.TaskType.API_REPORT_TASK);
        typeList.add(NoticeConstants.TaskType.BUG_SYNC_TASK);


    }

    @Test
    @Order(2)
    public void getTemplateFieldsEmptySuccess() throws Exception {
        List<String> typeList = new ArrayList<>();
        getTypeList(typeList);
        for (String s : typeList) {
            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/notice/template/get/fields/project-template-test-2" )
                            .header(SessionConstants.HEADER_TOKEN, sessionId)
                            .header(SessionConstants.CSRF_TOKEN, csrfToken)
                            .param("taskType", s))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
            String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
            ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
            MessageTemplateResultDTO messageTemplateResultDTO = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), MessageTemplateResultDTO.class);
            List<MessageTemplateFieldDTO> projectList = messageTemplateResultDTO.getFieldList();
            if (s.equals(NoticeConstants.TaskType.API_DEFINITION_TASK)) {
                List<String> collect = projectList.stream().map(OptionDTO::getId).toList();
                Assertions.assertFalse(collect.contains("grade"));
            }
            if (s.equals(NoticeConstants.TaskType.TEST_PLAN_TASK)) {
                List<String> collect = projectList.stream().map(OptionDTO::getId).toList();
                Assertions.assertFalse(collect.contains("aa"));
            }
            if (s.equals(NoticeConstants.TaskType.BUG_TASK)) {
                List<String> collect = projectList.stream().map(OptionDTO::getId).toList();
                Assertions.assertFalse(collect.contains("process"));
            }
            if (s.equals(NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK)) {
                List<String> collect = projectList.stream().map(OptionDTO::getId).toList();
                Assertions.assertFalse(collect.contains("level"));
            }
        }

    }
}
