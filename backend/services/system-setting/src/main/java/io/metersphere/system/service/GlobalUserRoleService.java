package io.metersphere.system.service;

import io.metersphere.sdk.dto.PermissionDefinitionItem;
import io.metersphere.sdk.dto.request.PermissionSettingUpdateRequest;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.service.BaseUserRoleService;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.UserRole;
import io.metersphere.system.domain.UserRoleExample;
import io.metersphere.system.dto.UserRoleOption;
import io.metersphere.system.mapper.ExtUserRoleMapper;
import io.metersphere.system.mapper.UserRoleMapper;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
    public static final String GLOBAL_SCOPE = "GLOBAL";
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private ExtUserRoleMapper extUserRoleMapper;

    public List<UserRole> list() {
        UserRoleExample example = new UserRoleExample();
        example.createCriteria().andScopeIdEqualTo(GLOBAL_SCOPE);
        return userRoleMapper.selectByExample(example);
    }

    /**
     * 校验是否是全局用户组，非全局抛异常
     */
    public void checkGlobalUserRole(UserRole userRole) {
        if (!StringUtils.equals(userRole.getScopeId(), GLOBAL_SCOPE)) {
            throw new MSException(GLOBAL_USER_ROLE_PERMISSION);
        }
    }

    /**
     * 校验用户是否是系统用户组
     */
    public void checkSystemUserGroup(UserRole userRole) {
        if (!StringUtils.equals(userRole.getType(), GlobalUserRoleService.SYSTEM_TYPE)) {
            throw new MSException(GLOBAL_USER_ROLE_RELATION_SYSTEM_PERMISSION);
        }
    }

    @Override
    public UserRole add(UserRole userRole) {
        userRole.setInternal(false);
        userRole.setScopeId(GLOBAL_SCOPE);
        checkExist(userRole);
        return super.add(userRole);
    }

    public void checkExist(UserRole userRole) {
        UserRoleExample example = new UserRoleExample();
        UserRoleExample.Criteria criteria = example.createCriteria()
                .andNameEqualTo(userRole.getName())
                .andScopeIdEqualTo(GLOBAL_SCOPE);
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
        UserRole originUserRole = get(userRole.getId());
        checkGlobalUserRole(originUserRole);
        checkInternalUserRole(originUserRole);
        userRole.setInternal(false);
        checkExist(userRole);
        return super.update(userRole);
    }

    public void delete(String id) {
        UserRole userRole = get(id);
        checkGlobalUserRole(userRole);
        delete(userRole);
    }

    public void checkRoleIsGlobalAndHaveMember(@Valid @NotEmpty List<String> roleIdList, boolean isSystem) {
        List<String> globalRoleList = extUserRoleMapper.selectGlobalRoleList(roleIdList, isSystem);
        if (globalRoleList.size() != roleIdList.size()) {
            throw new MSException("role.not.global");
        }
        if (!globalRoleList.contains(MEMBER.getValue())) {
            throw new MSException(Translator.get("role.not.contains.member"));
        }
    }

    public List<UserRoleOption> getGlobalSystemRoleList() {
        UserRoleExample example = new UserRoleExample();
        example.createCriteria().andScopeIdEqualTo(GLOBAL_SCOPE).andTypeEqualTo(SYSTEM_TYPE);
        List<UserRoleOption> returnList = new ArrayList<>();
        userRoleMapper.selectByExample(example).forEach(userRole -> {
            UserRoleOption userRoleOption = new UserRoleOption();
            userRoleOption.setId(userRole.getId());
            userRoleOption.setName(userRole.getName());
            userRoleOption.setSelected(StringUtils.equals(userRole.getId(), MEMBER.getValue()));
            userRoleOption.setCloseable(!StringUtils.equals(userRole.getId(), MEMBER.getValue()));
            returnList.add(userRoleOption);
        });
        return returnList;
    }


    public List<PermissionDefinitionItem> getPermissionSetting(String id) {
        UserRole userRole = get(id);
        checkGlobalUserRole(userRole);
        return getPermissionSetting(userRole);
    }

    @Override
    public void updatePermissionSetting(PermissionSettingUpdateRequest request) {
        UserRole userRole = get(request.getUserRoleId());
        checkGlobalUserRole(userRole);
        checkInternalUserRole(userRole);
        super.updatePermissionSetting(request);
    }
}
