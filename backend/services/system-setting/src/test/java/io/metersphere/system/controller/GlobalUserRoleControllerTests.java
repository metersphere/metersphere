package io.metersphere.system.controller;

import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.param.PermissionSettingUpdateRequestDefinition;
import io.metersphere.system.controller.param.UserRoleUpdateRequestDefinition;
import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserRole;
import io.metersphere.system.domain.UserRoleRelation;
import io.metersphere.system.domain.UserRoleRelationExample;
import io.metersphere.system.dto.permission.Permission;
import io.metersphere.system.dto.permission.PermissionDefinitionItem;
import io.metersphere.system.dto.sdk.request.PermissionSettingUpdateRequest;
import io.metersphere.system.dto.sdk.request.UserRoleUpdateRequest;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.mapper.UserRoleMapper;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import io.metersphere.system.service.BaseUserRolePermissionService;
import io.metersphere.system.service.BaseUserRoleRelationService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.utils.SessionUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;

import java.util.*;
import java.util.stream.Collectors;

import static io.metersphere.sdk.constants.InternalUserRole.ADMIN;
import static io.metersphere.sdk.constants.InternalUserRole.MEMBER;
import static io.metersphere.system.controller.handler.result.CommonResultCode.ADMIN_USER_ROLE_PERMISSION;
import static io.metersphere.system.controller.handler.result.CommonResultCode.INTERNAL_USER_ROLE_PERMISSION;
import static io.metersphere.system.controller.handler.result.MsHttpResultCode.NOT_FOUND;
import static io.metersphere.system.controller.result.SystemResultCode.GLOBAL_USER_ROLE_EXIST;
import static io.metersphere.system.controller.result.SystemResultCode.GLOBAL_USER_ROLE_PERMISSION;


@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GlobalUserRoleControllerTests extends BaseTest {
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private BaseUserRolePermissionService baseUserRolePermissionService;
    @Resource
    private BaseUserRoleRelationService baseUserRoleRelationService;
    private static final String BASE_PATH = "/user/role/global/";
    private static final String PERMISSION_SETTING = "permission/setting/{0}";
    private static final String PERMISSION_UPDATE = "permission/update";

    // 保存创建的用户组，方便之后的修改和删除测试使用
    private static UserRole addUserRole;
    private static UserRole anotherUserRole;
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserRoleRelationMapper userRoleRelationMapper;

    @Override
    protected String getBasePath() {
        return BASE_PATH;
    }

    @Test
    @Order(0)
    void add() throws Exception {
        // @@请求成功
        UserRoleUpdateRequest request = new UserRoleUpdateRequest();
        request.setName("test");
        request.setType(UserRoleType.SYSTEM.name());
        request.setDescription("test desc");
        MvcResult mvcResult = this.requestPostWithOkAndReturn(DEFAULT_ADD, request);
        UserRole resultData = getResultData(mvcResult, UserRole.class);
        UserRole userRole = userRoleMapper.selectByPrimaryKey(resultData.getId());
        // 校验请求成功数据
        this.addUserRole = userRole;
        Assertions.assertEquals(request.getName(), userRole.getName());
        Assertions.assertEquals(request.getType(), userRole.getType());
        Assertions.assertEquals(request.getDescription(), userRole.getDescription());

        // @@校验日志
        checkLog(this.addUserRole.getId(), OperationLogType.ADD);

        // @@重名校验异常
        assertErrorCode(this.requestPost(DEFAULT_ADD, request), GLOBAL_USER_ROLE_EXIST);

        // 在添加一条数据，供删除没有关联用户的用户组使用，提高覆盖率
        request.setName("other name");
        MvcResult anotherMvcResult = this.requestPostWithOkAndReturn(DEFAULT_ADD, request);
        anotherUserRole = userRoleMapper.selectByPrimaryKey(getResultData(anotherMvcResult, UserRole.class).getId());

        // @@异常参数校验
        createdGroupParamValidateTest(UserRoleUpdateRequestDefinition.class, DEFAULT_ADD);

        // @@校验权限
        requestPostPermissionTest(PermissionConstants.SYSTEM_USER_ROLE_ADD, DEFAULT_ADD, request);
    }

    @Test
    @Order(1)
    void update() throws Exception {
        // @@请求成功
        UserRoleUpdateRequest request = new UserRoleUpdateRequest();
        request.setId(addUserRole.getId());
        request.setName("test update");
        request.setType(UserRoleType.SYSTEM.name());
        request.setDescription("test desc !!!!");
        this.requestPostWithOk(DEFAULT_UPDATE, request);
        // 校验请求成功数据
        UserRole userRoleResult = userRoleMapper.selectByPrimaryKey(request.getId());
        Assertions.assertEquals(request.getName(), userRoleResult.getName());
        Assertions.assertEquals(request.getType(), userRoleResult.getType());
        Assertions.assertEquals(request.getDescription(), userRoleResult.getDescription());

        // 不修改信息
        UserRoleUpdateRequest emptyRequest = new UserRoleUpdateRequest();
        emptyRequest.setId(addUserRole.getId());
        this.requestPostWithOk(DEFAULT_UPDATE, emptyRequest);

        // @@校验日志
        checkLog(request.getId(), OperationLogType.UPDATE);

        // @@操作非全局用户组异常
        BeanUtils.copyBean(request, getNonGlobalUserRole());
        assertErrorCode(this.requestPost(DEFAULT_UPDATE, request), GLOBAL_USER_ROLE_PERMISSION);

        // @@操作内置用户组异常
        request.setId(ADMIN.getValue());
        request.setName(ADMIN.getValue());
        assertErrorCode(this.requestPost(DEFAULT_UPDATE, request), INTERNAL_USER_ROLE_PERMISSION);

        // @@重名校验异常
        request.setId(addUserRole.getId());
        request.setName("系统管理员");
        assertErrorCode(this.requestPost(DEFAULT_UPDATE, request), GLOBAL_USER_ROLE_EXIST);

        // @@校验 NOT_FOUND 异常
        request.setId("1111");
        request.setName("系统管理员1");
        assertErrorCode(this.requestPost(DEFAULT_UPDATE, request), NOT_FOUND);

        // @@异常参数校验
        updatedGroupParamValidateTest(UserRoleUpdateRequestDefinition.class, DEFAULT_UPDATE);

        // @@校验权限
        requestPostPermissionTest(PermissionConstants.SYSTEM_USER_ROLE_UPDATE, DEFAULT_UPDATE, request);
    }

    @Test
    @Order(2)
    void list() throws Exception {
        // @@请求成功
        MvcResult mvcResult = this.requestGetWithOk(DEFAULT_LIST)
                .andReturn();
        List<UserRole> userRoles = getResultDataArray(mvcResult, UserRole.class);

        // 校验是否是全局用户组
        userRoles.forEach(item -> Assertions.assertTrue(StringUtils.equalsIgnoreCase(item.getScopeId(), UserRoleScope.GLOBAL)));

        // 校验是否包含全部的内置用户组
        List<String> userRoleIds = userRoles.stream().map(UserRole::getId).toList();
        List<String> internalUserRoleIds = Arrays.stream(InternalUserRole.values())
                .map(InternalUserRole::getValue)
                .toList();
        Assertions.assertTrue(CollectionUtils.isSubCollection(internalUserRoleIds, userRoleIds));

        // 校验排序是否正常
        Map<String, Integer> typeOrderMap = new HashMap<>(3) {{
            put(UserRoleType.SYSTEM.name(), 1);
            put(UserRoleType.ORGANIZATION.name(), 2);
            put(UserRoleType.PROJECT.name(), 3);
        }};
        String userRoleType = UserRoleType.SYSTEM.name();
        long lastCreateTime = -1;
        for (UserRole userRole : userRoles) {
            // 判断是否按照 type 为 SYSTEM，ORGANIZATION， PROJECT 排序
            if (typeOrderMap.get(userRole.getType()) < typeOrderMap.get(userRoleType)) {
                Assertions.fail();
            } else if (typeOrderMap.get(userRole.getType()).equals(typeOrderMap.get(userRoleType))) {
                // 相等，比较创建时间
                if (userRole.getCreateTime() < lastCreateTime) {
                    Assertions.fail();
                }
            }
            lastCreateTime = userRole.getCreateTime();
            userRoleType = userRole.getType();
        }

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.SYSTEM_USER_ROLE_READ, DEFAULT_LIST);
    }

    @Test
    @Order(3)
    void updatePermissionSetting() throws Exception {
        PermissionSettingUpdateRequest request = new PermissionSettingUpdateRequest();
        request.setPermissions(new ArrayList<>() {{
            PermissionSettingUpdateRequest.PermissionUpdateRequest permission1
                    = new PermissionSettingUpdateRequest.PermissionUpdateRequest();
            permission1.setEnable(true);
            permission1.setId(PermissionConstants.SYSTEM_USER_READ);
            add(permission1);
            PermissionSettingUpdateRequest.PermissionUpdateRequest permission2
                    = new PermissionSettingUpdateRequest.PermissionUpdateRequest();
            permission2.setEnable(false);
            permission2.setId(PermissionConstants.SYSTEM_USER_ROLE_READ);
            add(permission2);
        }});

        // @@请求成功
        request.setUserRoleId(addUserRole.getId());
        this.requestPostWithOk(PERMISSION_UPDATE, request);
        // 获取该用户组拥有的权限
        Set<String> permissionIds = baseUserRolePermissionService.getPermissionIdSetByRoleId(request.getUserRoleId());
        Set<String> requestPermissionIds = request.getPermissions().stream()
                .filter(PermissionSettingUpdateRequest.PermissionUpdateRequest::getEnable)
                .map(PermissionSettingUpdateRequest.PermissionUpdateRequest::getId)
                .collect(Collectors.toSet());
        // 校验请求成功数据
        Assertions.assertEquals(requestPermissionIds, permissionIds);

        // @@校验日志
        checkLog(request.getUserRoleId(), OperationLogType.UPDATE);

        // @@操作非全局用户组异常
        request.setUserRoleId(getNonGlobalUserRole().getId());
        assertErrorCode(this.requestPost(PERMISSION_UPDATE, request), GLOBAL_USER_ROLE_PERMISSION);

        // @@操作内置用户组异常
        request.setUserRoleId(ADMIN.getValue());
        assertErrorCode(this.requestPost(PERMISSION_UPDATE, request), ADMIN_USER_ROLE_PERMISSION);

        // @@校验 NOT_FOUND 异常
        request.setUserRoleId("1111");
        assertErrorCode(this.requestPost(PERMISSION_UPDATE, request), NOT_FOUND);

        // @@异常参数校验
        paramValidateTest(PermissionSettingUpdateRequestDefinition.class, PERMISSION_UPDATE);

        // @@校验权限
        requestPostPermissionTest(PermissionConstants.SYSTEM_USER_ROLE_UPDATE, PERMISSION_UPDATE, request);
    }

    @Test
    @Order(4)
    void getPermissionSetting() throws Exception {
        // @@请求成功
        MvcResult mvcResult = this.requestGetWithOkAndReturn(PERMISSION_SETTING, ADMIN.getValue());
        List<PermissionDefinitionItem> permissionDefinition = getResultDataArray(mvcResult, PermissionDefinitionItem.class);
        // 获取该用户组拥有的权限
        Set<String> permissionIds = baseUserRolePermissionService.getPermissionIdSetByRoleId(ADMIN.getValue());
        // 设置勾选项
        permissionDefinition.forEach(firstLevel -> {
            List<PermissionDefinitionItem> children = firstLevel.getChildren();
            boolean allCheck = true;
            for (PermissionDefinitionItem secondLevel : children) {
                List<Permission> permissions = secondLevel.getPermissions();
                if (CollectionUtils.isEmpty(permissions)) {
                    continue;
                }
                boolean secondAllCheck = true;
                for (Permission p : permissions) {
                    if (permissionIds.contains(p.getId())) {
                        // 如果有权限这里校验开启
                        Assertions.assertTrue(p.getEnable());
                        // 使用完移除
                        permissionIds.remove(p.getId());
                    } else {
                        // 如果没有权限校验关闭
                        secondAllCheck = false;
                    }
                }
                if (!secondAllCheck) {
                    // 如果二级菜单有未勾选，则一级菜单设置为未勾选
                    allCheck = false;
                }
            }
        });
        // 校验是不是获取的数据中包含了该用户组所有的权限
        Assertions.assertTrue(CollectionUtils.isEmpty(permissionIds));

        // @@操作非全局用户组异常
        assertErrorCode(this.requestGet(PERMISSION_SETTING, getNonGlobalUserRole().getId()), GLOBAL_USER_ROLE_PERMISSION);

        // @@校验 NOT_FOUND 异常
        assertErrorCode(this.requestGet(PERMISSION_SETTING, "111"), NOT_FOUND);

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.SYSTEM_USER_ROLE_READ, PERMISSION_SETTING, ADMIN.getValue());
    }

    @Test
    @Order(5)
    void delete() throws Exception {
        // 校验删除该用户组，没有用户组的用户会默认添加系统成员用户组
        UserRoleRelation userRoleRelation = prepareOneLimitTest(addUserRole.getId());
        // @@请求成功
        this.requestGetWithOk(DEFAULT_DELETE, addUserRole.getId());
        // 校验请求成功数据
        Assertions.assertNull(userRoleMapper.selectByPrimaryKey(addUserRole.getId()));
        // 校验用户组与权限的关联关系是否删除
        Assertions.assertTrue(CollectionUtils.isEmpty(baseUserRolePermissionService.getByRoleId(addUserRole.getId())));
        // 校验用户组与用户的关联关系是否删除
        Assertions.assertTrue(CollectionUtils.isEmpty(baseUserRoleRelationService.getByRoleId(addUserRole.getId())));

        // 校验删除该用户组，没有用户组的用户会默认添加系统成员用户组
        UserRoleRelationExample example = new UserRoleRelationExample();
        example.createCriteria().andUserIdEqualTo(userRoleRelation.getUserId());
        List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(example);
        Assertions.assertTrue(userRoleRelations.size() == 1);
        Assertions.assertTrue(StringUtils.equalsIgnoreCase(userRoleRelations.getFirst().getRoleId(), MEMBER.getValue()));
        clearOneLimitTest(userRoleRelation.getUserId());

        // 删除没有关联用户的用户组
        this.requestGetWithOk(DEFAULT_DELETE, anotherUserRole.getId());

        // @@校验日志
        checkLog(addUserRole.getId(), OperationLogType.DELETE);

        // @@操作非全局用户组异常
        assertErrorCode(this.requestGet(DEFAULT_DELETE, getNonGlobalUserRole().getId()), GLOBAL_USER_ROLE_PERMISSION);

        // @@操作内置用户组异常
        assertErrorCode(this.requestGet(DEFAULT_DELETE, ADMIN.getValue()), INTERNAL_USER_ROLE_PERMISSION);

        // @@校验 NOT_FOUND 异常
        assertErrorCode(this.requestGet(DEFAULT_DELETE, "111"), NOT_FOUND);

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.SYSTEM_USER_ROLE_DELETE, DEFAULT_DELETE, addUserRole.getId());
    }

    /**
     * 插入一条非全局用户组，并返回
     */
    protected UserRole getNonGlobalUserRole() {
        // 插入一条非全局用户组数据
        UserRole nonGlobalUserRole = userRoleMapper.selectByPrimaryKey(ADMIN.getValue());
        nonGlobalUserRole.setName("非全局用户组");
        nonGlobalUserRole.setScopeId("not global");
        nonGlobalUserRole.setId(IDGenerator.nextStr());
        userRoleMapper.insert(nonGlobalUserRole);
        return nonGlobalUserRole;
    }

    /**
     * 创建一个用户和只有一个用户组的
     * 用于测试删除该用户组后，没有用户组的用户会默认添加系统成员用户组
     */
    private UserRoleRelation prepareOneLimitTest(String userRoleId) {
        // 插入一条用户数据
        User user = new User();
        user.setId(IDGenerator.nextStr());
        user.setCreateUser(SessionUtils.getUserId());
        user.setName("test one user role");
        user.setSource(UserSource.LOCAL.name());
        user.setEmail("1111111111@qq.com");
        user.setCreateTime(System.currentTimeMillis());
        user.setUpdateTime(System.currentTimeMillis());
        user.setCreateUser(ADMIN.getValue());
        user.setUpdateUser(ADMIN.getValue());
        user.setEnable(true);
        user.setDeleted(false);
        user.setCftToken("NONE");
        userMapper.insert(user);
        UserRoleRelation roleRelation = new UserRoleRelation();
        roleRelation.setId(IDGenerator.nextStr());
        roleRelation.setCreateTime(System.currentTimeMillis());
        roleRelation.setRoleId(userRoleId);
        roleRelation.setCreateUser(ADMIN.getValue());
        roleRelation.setUserId(user.getId());
        roleRelation.setSourceId(UserRoleScope.SYSTEM);
        roleRelation.setOrganizationId(UserRoleScope.SYSTEM);
        userRoleRelationMapper.insert(roleRelation);
        return roleRelation;
    }

    /**
     * 清理测试数据
     */
    private void clearOneLimitTest(String userId) {
        userMapper.deleteByPrimaryKey(userId);
    }
}