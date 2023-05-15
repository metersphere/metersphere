package io.metersphere.system.controller;

import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.User;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.regex.Matcher;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTests {
    @Resource
    private MockMvc mockMvc;

    @Test
    public void testSelectAll() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user/list-all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.person.name").value("Jason"))
                .andDo(print());
    }

    @Test
    public void testSelectAll2() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user/get/admin"))
                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.person.name").value("Jason"))
                .andDo(print());
    }

    @Test
    public void testAddUser() throws Exception {
        User user = new User();
        user.setId("admin");
        user.setName("admin");
        mockMvc.perform(MockMvcRequestBuilders.post("/user/add")
                        .content(JSON.toJSONString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("40000"))
                .andDo(print());
    }

}
