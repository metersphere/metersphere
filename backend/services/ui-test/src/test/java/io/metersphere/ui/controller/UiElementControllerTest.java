package io.metersphere.ui.controller;

import com.jayway.jsonpath.JsonPath;
import io.metersphere.sdk.util.JSON;
import io.metersphere.ui.domain.UiElement;
import jakarta.annotation.Resource;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UiElementControllerTest {

    @Resource
    private MockMvc mockMvc;

    private static String elementId;

    // 添加元素
    @Test
    @Order(1)
    public void testAddUiElement() {
        UiElement uiElement = new UiElement();
        uiElement.setName("test");
        uiElement.setCreateUser("admin");
        uiElement.setProjectId("1");
        uiElement.setLocationType("id");
        uiElement.setLocation("kw");
        uiElement.setModuleId("null");
        uiElement.setNum(33);
        uiElement.setVersionId(UUID.randomUUID().toString());
        uiElement.setLatest(true);
        uiElement.setPos(1L);
        try {
            var result = mockMvc.perform(MockMvcRequestBuilders.post("/ui_element/add")
                            .content(JSON.toJSONString(uiElement))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn();
            elementId = JsonPath.read(result.getResponse().getContentAsString(), "$.data.id");

        } catch (Exception e) {
            fail("insert fail");
        }

    }

    @Test
    @Order(2)
    public void testEditUiElement() {
        UiElement uiElement = new UiElement();
        uiElement.setId(elementId);
        uiElement.setProjectId("1");
        uiElement.setName("test");
        uiElement.setCreateUser("admin");
        uiElement.setLocationType("id");
        uiElement.setLocation("su");
        uiElement.setModuleId("null");
        uiElement.setNum(33);
        uiElement.setVersionId(UUID.randomUUID().toString());
        uiElement.setLatest(true);
        uiElement.setPos(1L);
        try {
            mockMvc.perform(MockMvcRequestBuilders.post("/ui_element/update")
                            .content(JSON.toJSONString(uiElement))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andDo(print());

        } catch (Exception e) {
            fail("update fail");
        }

    }

    @Test
    @Order(3)
    public void testSelectAll() {
        try {
            mockMvc.perform(MockMvcRequestBuilders.get("/ui_element/list"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andDo(print());

        } catch (Exception e) {
            fail("get fail");
        }

    }
}