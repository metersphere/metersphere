package io.metersphere.system.controller.user;

import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.dto.request.UserInviteRequest;
import io.metersphere.system.dto.request.user.UserChangeEnableRequest;
import io.metersphere.system.dto.request.user.UserRoleBatchRelationRequest;
import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.metersphere.system.dto.user.UserCreateInfo;
import io.metersphere.system.dto.user.response.UserSelectOption;
import io.metersphere.system.utils.user.UserParamUtils;
import io.metersphere.system.utils.user.UserRequestUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserControllerPermissionTests extends BaseTest {
    @Test
    public void permissionTest() throws Exception {
        //获取用户
        this.requestGetPermissionTest(PermissionConstants.SYSTEM_USER_READ, String.format(UserRequestUtils.URL_USER_GET, "admin"));

        //校验权限：用户创建
        UserCreateInfo paramUserInfo = new UserCreateInfo() {{
            setId("testId");
            setName("tianyang.no.permission.email");
            setEmail("tianyang.no.permission.email@126.com");
        }};
        List<UserSelectOption> paramRoleList = new ArrayList<>() {{
            this.add(
                    new UserSelectOption() {{
                        this.setId("member");
                        this.setName("member");
                    }});
        }};
        this.requestPostPermissionTest(PermissionConstants.SYSTEM_USER_ADD, UserRequestUtils.URL_USER_CREATE, UserParamUtils.getUserCreateDTO(
                paramRoleList,
                new ArrayList<>() {{
                    add(paramUserInfo);
                }}
        ));

        //校验权限：修改用户
        this.requestPostPermissionTest(PermissionConstants.SYSTEM_USER_UPDATE, UserRequestUtils.URL_USER_UPDATE, UserParamUtils.getUserUpdateDTO(paramUserInfo, paramRoleList));

        //校验权限：分页查询用户列表
        this.requestPostPermissionTest(PermissionConstants.SYSTEM_USER_READ, UserRequestUtils.URL_USER_PAGE, UserParamUtils.getDefaultPageRequest());

        //校验权限：启用/禁用用户
        UserChangeEnableRequest userChangeEnableRequest = new UserChangeEnableRequest();
        userChangeEnableRequest.setEnable(false);
        userChangeEnableRequest.setSelectIds(new ArrayList<>() {{
            this.add("admin");
        }});
        this.requestPostPermissionTest(PermissionConstants.SYSTEM_USER_UPDATE, UserRequestUtils.URL_USER_UPDATE_ENABLE, userChangeEnableRequest);

        //用户导入
        File jarFile = new File(
                Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/user_import_permission.xlsx")).getPath());

        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("file", jarFile);
        this.requestMultipartPermissionTest(PermissionConstants.SYSTEM_USER_IMPORT, UserRequestUtils.URL_USER_IMPORT, paramMap);

        //用户删除
        TableBatchProcessDTO request = new TableBatchProcessDTO();
        request.setSelectIds(new ArrayList<>() {{
            this.add("testId");
        }});
        this.requestPostPermissionTest(PermissionConstants.SYSTEM_USER_DELETE, UserRequestUtils.URL_USER_DELETE, request);

        //重置密码
        request = new TableBatchProcessDTO();
        request.setSelectIds(Collections.singletonList("admin"));
        this.requestPostPermissionTest(PermissionConstants.SYSTEM_USER_UPDATE, UserRequestUtils.URL_USER_RESET_PASSWORD, request);

        //批量添加用户到用户组
        UserRoleBatchRelationRequest userAndRoleBatchRequest = new UserRoleBatchRelationRequest();
        userAndRoleBatchRequest.setSelectIds(Collections.singletonList("admin"));
        userAndRoleBatchRequest.setRoleIds(Collections.singletonList("member"));
        this.requestPostPermissionTest(PermissionConstants.SYSTEM_USER_UPDATE, UserRequestUtils.URL_USER_ROLE_RELATION, userAndRoleBatchRequest);


        //校验权限：系统全局用户组获取
        this.requestGetPermissionTest(PermissionConstants.SYSTEM_USER_READ, UserRequestUtils.URL_GET_GLOBAL_SYSTEM);
        //        查看组织
        this.requestGetPermissionsTest(List.of(PermissionConstants.SYSTEM_USER_ROLE_READ, PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ), UserRequestUtils.URL_GET_ORGANIZATION);
        //查看项目
        this.requestGetPermissionsTest(List.of(PermissionConstants.SYSTEM_USER_ROLE_READ, PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ), UserRequestUtils.URL_GET_PROJECT);

        //        批量添加用户到项目
        UserRoleBatchRelationRequest roleBatchRelationRequest = new UserRoleBatchRelationRequest();
        roleBatchRelationRequest.setSelectIds(Collections.singletonList("admin"));
        roleBatchRelationRequest.setRoleIds(Collections.singletonList("member"));
        List<String> addMemberPermissionList = new ArrayList<>();
        addMemberPermissionList.add(PermissionConstants.SYSTEM_USER_UPDATE);
        addMemberPermissionList.add(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_MEMBER_ADD);
        this.requestPostPermissionsTest(addMemberPermissionList, UserRequestUtils.URL_ADD_PROJECT_MEMBER, roleBatchRelationRequest);
        //        批量添加用户到组织
        this.requestPostPermissionsTest(addMemberPermissionList, UserRequestUtils.URL_ADD_ORGANIZATION_MEMBER, roleBatchRelationRequest);

        //  邀请用户
        UserInviteRequest userInviteRequest = new UserInviteRequest();
        userInviteRequest.setUserRoleIds(Collections.singletonList("member"));
        userInviteRequest.setInviteEmails(Collections.singletonList("tianyang.song.invite.permission.1@test.email"));
        this.requestPostPermissionTest(PermissionConstants.SYSTEM_USER_INVITE, UserRequestUtils.URL_INVITE, userInviteRequest);
    }

}
