package io.metersphere.system.service;

import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.UserRole;
import io.metersphere.system.domain.UserRoleExample;
import io.metersphere.system.dto.UserRoleOption;
import io.metersphere.system.mapper.ExtUserRoleMapper;
import io.metersphere.system.mapper.UserRoleMapper;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 系统设置的接口增删改查都是针对全局用户组
 *
 * @author jianxing
 * @date : 2023-6-8
 */
@Service
public class GlobalUserRoleService {

    public static String GLOBAL_SCOPE = "GLOBAL";

    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private ExtUserRoleMapper extUserRoleMapper;

    public List<UserRole> list() {
        UserRoleExample example = new UserRoleExample();
        example.createCriteria().andScopeIdEqualTo(GLOBAL_SCOPE);
        return userRoleMapper.selectByExample(example);
    }

    public UserRole get(String id) {
        // todo 只能获取全局
        return userRoleMapper.selectByPrimaryKey(id);
    }

    public UserRole add(UserRole userRole) {
        // todo 只能添加自定义全局
        userRoleMapper.insert(userRole);
        return userRole;
    }

    public UserRole update(UserRole userRole) {
        // todo 只能修改自定义全局
        userRoleMapper.updateByPrimaryKeySelective(userRole);
        return userRole;
    }

    public String delete(String id) {
        // todo 只能删除自定义全局
        userRoleMapper.deleteByPrimaryKey(id);
        return id;
    }

    public void checkRoleIsGlobalAndHaveMember(@Valid @NotEmpty List<String> roleIdList, boolean isSystem) {
        List<String> globalRoleList = extUserRoleMapper.selectGlobalRoleList(roleIdList, isSystem);
        if (globalRoleList.size() != roleIdList.size()) {
            throw new MSException("role.not.global");
        }
        if (!globalRoleList.contains("member")) {
            throw new MSException(Translator.get("role.not.contains.member"));
        }
    }

    public List<UserRoleOption> getGlobalSystemRoleList() {
        UserRoleExample example = new UserRoleExample();
        example.createCriteria().andScopeIdEqualTo("global").andTypeEqualTo("SYSTEM");
        List<UserRoleOption> returnList = new ArrayList<>();
        userRoleMapper.selectByExample(example).forEach(userRole -> {
            UserRoleOption userRoleOption = new UserRoleOption();
            userRoleOption.setId(userRole.getId());
            userRoleOption.setName(userRole.getName());
            userRoleOption.setSelected(StringUtils.equals(userRole.getId(), "member"));
            userRoleOption.setCloseable(!StringUtils.equals(userRole.getId(), "member"));
            returnList.add(userRoleOption);
        });
        return returnList;
    }
}
