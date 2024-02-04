package io.metersphere.system.service;

import io.metersphere.sdk.constants.UserRoleScope;
import io.metersphere.sdk.constants.UserRoleType;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.UserRole;
import io.metersphere.system.domain.UserRoleExample;
import io.metersphere.system.dto.permission.PermissionDefinitionItem;
import io.metersphere.system.dto.sdk.request.PermissionSettingUpdateRequest;
import io.metersphere.system.dto.user.response.UserSelectOption;
import io.metersphere.system.mapper.ExtUserRoleMapper;
import io.metersphere.system.mapper.UserRoleMapper;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static io.metersphere.sdk.constants.InternalUserRole.MEMBER;
import static io.metersphere.system.controller.result.SystemResultCode.*;

/**
 * 系统设置的接口增删改查都是针对全局用户组
 *
 * @author jianxing
 * @date : 2023-6-8
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class GlobalUserRoleService extends BaseUserRoleService {

    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private ExtUserRoleMapper extUserRoleMapper;

    public List<UserRole> list() {
        UserRoleExample example = new UserRoleExample();
        example.createCriteria().andScopeIdEqualTo(UserRoleScope.GLOBAL);
        List<UserRole> userRoles = userRoleMapper.selectByExample(example);
        // 先按照类型排序，再按照创建时间排序
        userRoles.sort(Comparator.comparingInt(this::getTypeOrder)
                .thenComparingInt(item ->getInternal(item.getInternal()))
                .thenComparing(UserRole::getCreateTime));
        return userRoles;
    }

    private int getInternal(Boolean internal) {
        return BooleanUtils.isTrue(internal) ? 0 : 1;
    }

    private int getTypeOrder(UserRole userRole) {
        Map<String, Integer> typeOrderMap = new HashMap<>(3) {{
            put(UserRoleType.SYSTEM.name(), 1);
            put(UserRoleType.ORGANIZATION.name(), 2);
            put(UserRoleType.PROJECT.name(), 3);
        }};
        return typeOrderMap.getOrDefault(userRole.getType(), 0);
    }

    /**
     * 校验是否是全局用户组，非全局抛异常
     */
    @Override
    public void checkGlobalUserRole(UserRole userRole) {
        if (!StringUtils.equals(userRole.getScopeId(), UserRoleScope.GLOBAL)) {
            throw new MSException(GLOBAL_USER_ROLE_PERMISSION);
        }
    }

    /**
     * 校验用户是否是系统用户组
     */
    public void checkSystemUserGroup(UserRole userRole) {
        if (!StringUtils.equalsIgnoreCase(userRole.getType(), UserRoleType.SYSTEM.name())) {
            throw new MSException(GLOBAL_USER_ROLE_RELATION_SYSTEM_PERMISSION);
        }
    }

    @Override
    public UserRole add(UserRole userRole) {
        userRole.setInternal(false);
        userRole.setScopeId(UserRoleScope.GLOBAL);
        checkExist(userRole);
        return super.add(userRole);
    }

    public void checkExist(UserRole userRole) {
        if (StringUtils.isBlank(userRole.getName())) {
            return;
        }

        UserRoleExample example = new UserRoleExample();
        UserRoleExample.Criteria criteria = example.createCriteria()
                .andNameEqualTo(userRole.getName())
                .andScopeIdEqualTo(UserRoleScope.GLOBAL);
        if (StringUtils.isNoneBlank(userRole.getId())) {
            criteria.andIdNotEqualTo(userRole.getId());
        }

        List<UserRole> userRoles = userRoleMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(userRoles)) {
            throw new MSException(GLOBAL_USER_ROLE_EXIST);
        }
    }

    @Override
    public UserRole update(UserRole userRole) {
        UserRole originUserRole = getWithCheck(userRole.getId());
        checkGlobalUserRole(originUserRole);
        checkInternalUserRole(originUserRole);
        userRole.setInternal(false);
        checkExist(userRole);
        return super.update(userRole);
    }

    public void delete(String id, String currentUserId) {
        UserRole userRole = getWithCheck(id);
        checkGlobalUserRole(userRole);
        super.delete(userRole, MEMBER.getValue(), currentUserId, UserRoleScope.SYSTEM);
    }

    public void checkRoleIsGlobalAndHaveMember(@Valid @NotEmpty List<String> roleIdList, boolean isSystem) {
        List<String> globalRoleList = extUserRoleMapper.selectGlobalRoleList(roleIdList, isSystem);
        if (globalRoleList.size() != roleIdList.size()) {
            throw new MSException(Translator.get("role.not.global"));
        }
    }

    public List<UserSelectOption> getGlobalSystemRoleList() {
        UserRoleExample example = new UserRoleExample();
        example.createCriteria().andScopeIdEqualTo(UserRoleScope.GLOBAL)
                .andTypeEqualTo(UserRoleType.SYSTEM.name());
        List<UserSelectOption> returnList = new ArrayList<>();
        userRoleMapper.selectByExample(example).forEach(userRole -> {
            UserSelectOption userRoleOption = new UserSelectOption();
            userRoleOption.setId(userRole.getId());
            userRoleOption.setName(userRole.getName());
            userRoleOption.setSelected(StringUtils.equals(userRole.getId(), MEMBER.getValue()));
            userRoleOption.setCloseable(!StringUtils.equals(userRole.getId(), MEMBER.getValue()));
            returnList.add(userRoleOption);
        });
        return returnList;
    }


    public List<PermissionDefinitionItem> getPermissionSetting(String id) {
        UserRole userRole = getWithCheck(id);
        checkGlobalUserRole(userRole);
        return getPermissionSetting(userRole);
    }

    @Override
    public void updatePermissionSetting(PermissionSettingUpdateRequest request) {
        UserRole userRole = getWithCheck(request.getUserRoleId());
        checkGlobalUserRole(userRole);
        // 内置管理员级别用户组无法更改权限
        checkAdminUserRole(userRole);
        super.updatePermissionSetting(request);
    }
}
