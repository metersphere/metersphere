package io.metersphere.system.controller;

import io.metersphere.sdk.dto.UserDTO;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.User;
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

import java.util.ArrayList;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserControllerTests {
    @Resource
    private MockMvc mockMvc;

    @Test
    @Order(1)
    public void testAddUser() throws Exception {
        UserDTO user = new UserDTO();
        user.setId("admin");
        user.setName("admin");
        user.setCreateUser("system");
        user.setSource("LOCAL");
        user.setEmail("bin@fit2cloud.com");
        user.setStatus("enabled");
        user.setCreateTime(System.currentTimeMillis());
        user.setUpdateTime(System.currentTimeMillis());

        user.setSeleniumServer("http://localhost:4444");

        mockMvc.perform(MockMvcRequestBuilders.post("/user/add")
                        .content(JSON.toJSONString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @Order(2)
    public void testAddUserFailed() throws Exception {
        UserDTO user = new UserDTO();
        user.setId("admin2");
        user.setCreateTime(System.currentTimeMillis());
        user.setUpdateTime(System.currentTimeMillis());

        user.setSeleniumServer("http://localhost:4444");

        mockMvc.perform(MockMvcRequestBuilders.post("/user/add")
                        .content(JSON.toJSONString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @Order(3)
    public void testGetUser() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user/get/admin"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.id").value("admin"));
    }

    @Test
    @Order(4)
    public void testSelectAll() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user/list-all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data[0].name").value("admin"));
    }


//    @Test
    @Order(5)
    public void testBatchAddUser() throws Exception {
        var users = new ArrayList<User>();
        for (int i = 0; i < 1000; i++) {
            User user = new User();
            user.setId("batch1_" + i);
            user.setName("batch1_" + i);
            user.setCreateUser("system");
            user.setSource("LOCAL");
            user.setEmail("bin@fit2cloud.com");
            user.setStatus("enabled");
            user.setCreateTime(System.currentTimeMillis());
            user.setUpdateTime(System.currentTimeMillis());
            users.add(user);
        }

        mockMvc.perform(MockMvcRequestBuilders.post("/user/batch-add")
                        .content(JSON.toJSONString(users))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

//    @Test
    @Order(6)
    public void testBatchAddUser2() throws Exception {
        var users = new ArrayList<User>();
        for (int i = 0; i < 1000; i++) {
            User user = new User();
            user.setId("batch2_" + i);
            user.setName("batch2_" + i);
            user.setCreateUser("system");
            user.setSource("LOCAL");
            user.setEmail("bin@fit2cloud.com");
            user.setStatus("enabled");
            user.setCreateTime(System.currentTimeMillis());
            user.setUpdateTime(System.currentTimeMillis());
            users.add(user);
        }

        mockMvc.perform(MockMvcRequestBuilders.post("/user/batch-add2")
                        .content(JSON.toJSONString(users))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

//    @Test
    @Order(7)
    public void testBatchAddUser3() throws Exception {
        var users = new ArrayList<User>();
        for (int i = 0; i < 1000; i++) {
            User user = new User();
            user.setId("batch3_" + i);
            user.setName("batch3_" + i);
            user.setCreateUser("system");
            user.setSource("LOCAL");
            user.setEmail("bin@fit2cloud.com");
            user.setStatus("enabled");
            user.setCreateTime(System.currentTimeMillis());
            user.setUpdateTime(System.currentTimeMillis());
            users.add(user);
        }

        mockMvc.perform(MockMvcRequestBuilders.post("/user/batch-add3")
                        .content(JSON.toJSONString(users))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

//    @Test
    @Order(8)
    public void testCount() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user/count")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").value(3001));
    }

}
