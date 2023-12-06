package io.metersphere.system.controller.user;

import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.util.CodingUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.UserExample;
import io.metersphere.system.domain.UserExtendExample;
import io.metersphere.system.dto.request.user.PersonalUpdatePasswordRequest;
import io.metersphere.system.dto.request.user.PersonalUpdateRequest;
import io.metersphere.system.dto.user.UserDTO;
import io.metersphere.system.mapper.UserExtendMapper;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.utils.user.PersonalRequestUtils;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PersonalControllerTests extends BaseTest {
    private static String loginUser = "admin";

    @Resource
    private UserMapper userMapper;

    @Test
    @Order(0)
    void testPersonalGetId() throws Exception {
        //查询自己的
        this.requestGetWithOk(String.format(PersonalRequestUtils.URL_PERSONAL_GET, loginUser));
        //查询非登录人
        this.requestGet(String.format(PersonalRequestUtils.URL_PERSONAL_GET, IDGenerator.nextStr())).andExpect(status().is5xxServerError());

        //权限校验
        this.requestGetPermissionTest(PermissionConstants.SYSTEM_PERSONAL_READ, String.format(PersonalRequestUtils.URL_PERSONAL_GET, loginUser));
    }

    private UserDTO selectUserDTO(String id) throws Exception {
        MvcResult result = this.requestGetAndReturn(String.format(PersonalRequestUtils.URL_PERSONAL_GET, loginUser));
        ResultHolder resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        return JSON.parseObject(JSON.toJSONString(resultHolder.getData()), UserDTO.class);
    }

    @Resource
    private UserExtendMapper userExtendMapper;
    @Test
    @Order(1)
    void testPersonalUpdateInfo() throws Exception {
        PersonalUpdateRequest request = new PersonalUpdateRequest();
        request.setId(loginUser);
        request.setEmail("admin_update@metersphere.io");
        request.setUsername("admin_update");
        request.setPhone("1111111111");
        this.requestPostWithOk(PersonalRequestUtils.URL_PERSONAL_UPDATE_INFO, request);
        UserDTO userDTO = this.selectUserDTO(loginUser);
        this.checkUserInformation(userDTO, request);

        //修改头像
        UserExtendExample example = new UserExtendExample();
        example.createCriteria().andIdEqualTo(loginUser);
        Assertions.assertEquals(userExtendMapper.countByExample(example), 0);

        request = new PersonalUpdateRequest();
        request.setId(loginUser);
        request.setEmail("admin_update@metersphere.io");
        request.setUsername("admin_update");
        request.setPhone("1111111111");
        request.setAvatar(IDGenerator.nextStr());
        this.requestPostWithOk(PersonalRequestUtils.URL_PERSONAL_UPDATE_INFO, request);
        userDTO = this.selectUserDTO(loginUser);
        Assertions.assertEquals(userDTO.getAvatar(), request.getAvatar());
        //多次修改头像
        Assertions.assertEquals(userExtendMapper.countByExample(example), 1L);
        request.setAvatar(IDGenerator.nextStr());
        this.requestPostWithOk(PersonalRequestUtils.URL_PERSONAL_UPDATE_INFO, request);
        userDTO = this.selectUserDTO(loginUser);
        Assertions.assertEquals(userDTO.getAvatar(), request.getAvatar());

        //修改回去
        request = new PersonalUpdateRequest();
        request.setId(loginUser);
        request.setEmail("admin@metersphere.io");
        request.setUsername("'Administrator'");
        request.setPhone("12345678901");
        this.requestPostWithOk(PersonalRequestUtils.URL_PERSONAL_UPDATE_INFO, request);
        userDTO = this.selectUserDTO(loginUser);
        this.checkUserInformation(userDTO, request);

        //修改非登录人
        request = new PersonalUpdateRequest();
        request.setId(IDGenerator.nextStr());
        request.setEmail("admin@metersphere.io");
        request.setUsername("'Administrator'");
        request.setPhone("12345678901");
        this.requestPost(PersonalRequestUtils.URL_PERSONAL_UPDATE_INFO, request).andExpect(status().is5xxServerError());

        //参数校验
        request = new PersonalUpdateRequest();
        request.setId(loginUser);
        request.setEmail("admin@metersphere.io");
        request.setUsername("'Administrator'");
        this.requestPost(PersonalRequestUtils.URL_PERSONAL_UPDATE_INFO, request).andExpect(status().isBadRequest());

        request = new PersonalUpdateRequest();
        request.setId(loginUser);
        request.setEmail("admin@metersphere.io");
        request.setPhone("12345678901");
        this.requestPost(PersonalRequestUtils.URL_PERSONAL_UPDATE_INFO, request).andExpect(status().isBadRequest());

        request = new PersonalUpdateRequest();
        request.setId(loginUser);
        request.setUsername("'Administrator'");
        request.setPhone("12345678901");
        this.requestPost(PersonalRequestUtils.URL_PERSONAL_UPDATE_INFO, request).andExpect(status().isBadRequest());

        request = new PersonalUpdateRequest();
        request.setEmail("admin@metersphere.io");
        request.setUsername("'Administrator'");
        request.setPhone("12345678901");
        this.requestPost(PersonalRequestUtils.URL_PERSONAL_UPDATE_INFO, request).andExpect(status().isBadRequest());

        //权限校验
        request = new PersonalUpdateRequest();
        request.setId(loginUser);
        request.setEmail("admin@metersphere.io");
        request.setUsername("'Administrator'");
        request.setPhone("12345678901");
        this.requestPostPermissionTest(PermissionConstants.SYSTEM_PERSONAL_READ_UPDATE, PersonalRequestUtils.URL_PERSONAL_UPDATE_INFO, request);
    }

    private void checkUserInformation(UserDTO userDTO, PersonalUpdateRequest request) {
        if (request.getId() != null) {
            Assertions.assertEquals(request.getId(), userDTO.getId());
        }
        if (request.getEmail() != null) {
            Assertions.assertEquals(request.getEmail(), userDTO.getEmail());
        }
        if (request.getUsername() != null) {
            Assertions.assertEquals(request.getUsername(), userDTO.getName());
        }
        if (request.getPhone() != null) {
            Assertions.assertEquals(request.getPhone(), userDTO.getPhone());
        }
    }

    @Test
    @Order(2)
    void testPersonalUpdatePassword() throws Exception {
        PersonalUpdatePasswordRequest request = new PersonalUpdatePasswordRequest();
        request.setId(loginUser);
        request.setOldPassword(CodingUtils.md5("metersphere"));
        request.setNewPassword(CodingUtils.md5("metersphere222"));
        this.requestPostWithOk(PersonalRequestUtils.URL_PERSONAL_UPDATE_PASSWORD, request);

        UserExample example = new UserExample();
        example.createCriteria().andIdEqualTo(loginUser).andPasswordEqualTo(CodingUtils.md5("metersphere222"));
        Assertions.assertEquals(userMapper.countByExample(example), 1);

        //修改回去
        request = new PersonalUpdatePasswordRequest();
        request.setId(loginUser);
        request.setOldPassword(CodingUtils.md5("metersphere222"));
        request.setNewPassword(CodingUtils.md5("metersphere"));
        this.requestPostWithOk(PersonalRequestUtils.URL_PERSONAL_UPDATE_PASSWORD, request);
        example.clear();
        example.createCriteria().andIdEqualTo(loginUser).andPasswordEqualTo(CodingUtils.md5("metersphere"));
        Assertions.assertEquals(userMapper.countByExample(example), 1L);

        //密码错误
        request = new PersonalUpdatePasswordRequest();
        request.setId(loginUser);
        request.setOldPassword(CodingUtils.md5("metersphere222"));
        request.setNewPassword(CodingUtils.md5("metersphere"));
        this.requestPost(PersonalRequestUtils.URL_PERSONAL_UPDATE_PASSWORD, request).andExpect(status().is5xxServerError());

        //参数校验
        request = new PersonalUpdatePasswordRequest();
        request.setOldPassword(CodingUtils.md5("metersphere222"));
        request.setNewPassword(CodingUtils.md5("metersphere"));
        this.requestPost(PersonalRequestUtils.URL_PERSONAL_UPDATE_PASSWORD, request).andExpect(status().isBadRequest());

        request = new PersonalUpdatePasswordRequest();
        request.setId(loginUser);
        request.setNewPassword(CodingUtils.md5("metersphere"));
        this.requestPost(PersonalRequestUtils.URL_PERSONAL_UPDATE_PASSWORD, request).andExpect(status().isBadRequest());

        request = new PersonalUpdatePasswordRequest();
        request.setId(loginUser);
        request.setOldPassword(CodingUtils.md5("metersphere222"));
        this.requestPost(PersonalRequestUtils.URL_PERSONAL_UPDATE_PASSWORD, request).andExpect(status().isBadRequest());

        //修改非当前人
        request = new PersonalUpdatePasswordRequest();
        request.setId(IDGenerator.nextStr());
        request.setOldPassword(CodingUtils.md5("metersphere"));
        request.setNewPassword(CodingUtils.md5("metersphere333"));
        this.requestPost(PersonalRequestUtils.URL_PERSONAL_UPDATE_PASSWORD, request).andExpect(status().is5xxServerError());

        //权限校验
        request = new PersonalUpdatePasswordRequest();
        request.setId(loginUser);
        request.setOldPassword(CodingUtils.md5("metersphere222"));
        request.setNewPassword(CodingUtils.md5("metersphere"));
        this.requestPostPermissionTest(PermissionConstants.SYSTEM_PERSONAL_READ_UPDATE, PersonalRequestUtils.URL_PERSONAL_UPDATE_PASSWORD, request);

        //最后检查密码是否回归原密码
        example.clear();
        example.createCriteria().andIdEqualTo(loginUser).andPasswordEqualTo(CodingUtils.md5("metersphere"));
        Assertions.assertEquals(userMapper.countByExample(example), 1);
    }

}
