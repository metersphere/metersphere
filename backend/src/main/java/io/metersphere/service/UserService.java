package io.metersphere.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtUserMapper;
import io.metersphere.base.mapper.ext.ExtUserRoleMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CodingUtil;
import io.metersphere.controller.request.UserRequest;
import io.metersphere.controller.request.member.AddMemberRequest;
import io.metersphere.controller.request.member.QueryMemberRequest;
import io.metersphere.controller.request.organization.AddOrgMemberRequest;
import io.metersphere.controller.request.organization.QueryOrgMemberRequest;
import io.metersphere.dto.UserDTO;
import io.metersphere.dto.UserPassDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.user.SessionUser;
import io.metersphere.user.SessionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private RoleMapper roleMapper;
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private ExtUserRoleMapper extUserRoleMapper;
    @Resource
    private OrganizationMapper organizationMapper;
    @Resource
    private WorkspaceMapper workspaceMapper;
    @Resource
    private ExtUserMapper extUserMapper;

    public UserDTO insert(User user) {
        checkUserParam(user);
        createUser(user);
        return getUserDTO(user.getId());
    }

    private void checkUserParam(User user) {
        if (StringUtils.isBlank(user.getName())) {
            MSException.throwException(Translator.get("user_name_is_null"));
        }

        if (StringUtils.isBlank(user.getEmail())) {
            MSException.throwException(Translator.get("user_email_is_null"));
        }
        // password
    }

    private void createUser(User userRequest) {
        User user = new User();
        BeanUtils.copyProperties(userRequest, user);
        user.setCreateTime(System.currentTimeMillis());
        user.setUpdateTime(System.currentTimeMillis());
        // 默认1:启用状态
        user.setStatus("1");
        // 密码使用 MD5
        user.setPassword(CodingUtil.md5(user.getPassword()));
        UserExample userExample = new UserExample();
        UserExample.Criteria criteria = userExample.createCriteria();
        criteria.andEmailEqualTo(user.getEmail());
        List<User> userList = userMapper.selectByExample(userExample);
        if (!CollectionUtils.isEmpty(userList)) {
            MSException.throwException(Translator.get("user_email_already_exists"));
        }
        userMapper.insertSelective(user);
    }

    public UserDTO getUserDTO(String userId) {

        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return null;
        }
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        //
        UserRoleExample userRoleExample = new UserRoleExample();
        userRoleExample.createCriteria().andUserIdEqualTo(userId);
        List<UserRole> userRoleList = userRoleMapper.selectByExample(userRoleExample);

        if (CollectionUtils.isEmpty(userRoleList)) {
            return userDTO;
        }
        // 设置 user_role
        userDTO.setUserRoles(userRoleList);

        List<String> roleIds = userRoleList.stream().map(UserRole::getRoleId).collect(Collectors.toList());

        RoleExample roleExample = new RoleExample();
        roleExample.createCriteria().andIdIn(roleIds);

        List<Role> roleList = roleMapper.selectByExample(roleExample);
        userDTO.setRoles(roleList);

        return userDTO;
    }

    public List<User> getUserList() {
        return userMapper.selectByExample(null);
    }

    public List<User> getUserListWithRequest(UserRequest request) {
        return extUserMapper.getUserList(request);
    }

    public void deleteUser(String userId) {
        userMapper.deleteByPrimaryKey(userId);
    }

    public void updateUser(User user) {
        // MD5
        user.setPassword(CodingUtil.md5(user.getPassword()));
        user.setUpdateTime(System.currentTimeMillis());
        userMapper.updateByPrimaryKeySelective(user);
    }

    public void switchUserRole(String sign, String sourceId) {
        SessionUser sessionUser = SessionUtils.getUser();
        // 获取最新UserDTO
        UserDTO user = getUserDTO(sessionUser.getId());

        User newUser = new User();
        if (StringUtils.equals("organization", sign)) {
            user.setLastOrganizationId(sourceId);
            user.setLastWorkspaceId("");
        }
        if (StringUtils.equals("workspace", sign)) {
            Workspace workspace = workspaceMapper.selectByPrimaryKey(sourceId);
            user.setLastOrganizationId(workspace.getOrganizationId());
            user.setLastWorkspaceId(sourceId);
        }
        BeanUtils.copyProperties(user, newUser);
        // 切换工作空间或组织之后更新 session 里的 user
        SessionUtils.putUser(SessionUser.fromUser(user));
        userMapper.updateByPrimaryKeySelective(newUser);
    }

    public User getUserInfo(String userId) {
        return userMapper.selectByPrimaryKey(userId);
    }

    public List<User> getMemberList(QueryMemberRequest request) {
        return extUserRoleMapper.getMemberList(request);
    }

    public void addMember(AddMemberRequest request) {
        if (!CollectionUtils.isEmpty(request.getUserIds())) {
            for (String userId : request.getUserIds()) {
                UserRoleExample userRoleExample = new UserRoleExample();
                userRoleExample.createCriteria().andUserIdEqualTo(userId).andSourceIdEqualTo(request.getWorkspaceId());
                List<UserRole> userRoles = userRoleMapper.selectByExample(userRoleExample);
                if (userRoles.size() > 0) {
                    User user = userMapper.selectByPrimaryKey(userId);
                    String username = user.getName();
                    MSException.throwException("The user [" + username + "] already exists in the current workspace！");
                } else {
                    for (String roleId : request.getRoleIds()) {
                        UserRole userRole = new UserRole();
                        userRole.setRoleId(roleId);
                        userRole.setSourceId(request.getWorkspaceId());
                        userRole.setUserId(userId);
                        userRole.setId(UUID.randomUUID().toString());
                        userRole.setUpdateTime(System.currentTimeMillis());
                        userRole.setCreateTime(System.currentTimeMillis());
                        userRoleMapper.insertSelective(userRole);
                    }
                }
            }
        }
    }

    public void deleteMember(String workspaceId, String userId) {
        UserRoleExample example = new UserRoleExample();
        example.createCriteria().andRoleIdLike("%test%")
                .andUserIdEqualTo(userId).andSourceIdEqualTo(workspaceId);
        userRoleMapper.deleteByExample(example);
    }

    public void addOrganizationMember(AddOrgMemberRequest request) {
        if (!CollectionUtils.isEmpty(request.getUserIds())) {
            for (String userId : request.getUserIds()) {
                UserRoleExample userRoleExample = new UserRoleExample();
                userRoleExample.createCriteria().andUserIdEqualTo(userId).andSourceIdEqualTo(request.getOrganizationId());
                List<UserRole> userRoles = userRoleMapper.selectByExample(userRoleExample);
                if (userRoles.size() > 0) {
                    User user = userMapper.selectByPrimaryKey(userId);
                    String username = user.getName();
                    MSException.throwException("The user [" + username + "] already exists in the current organization！");
                } else {
                    for (String roleId : request.getRoleIds()) {
                        UserRole userRole = new UserRole();
                        userRole.setId(UUID.randomUUID().toString());
                        userRole.setRoleId(roleId);
                        userRole.setSourceId(request.getOrganizationId());
                        userRole.setUserId(userId);
                        userRole.setUpdateTime(System.currentTimeMillis());
                        userRole.setCreateTime(System.currentTimeMillis());
                        userRoleMapper.insertSelective(userRole);
                    }
                }
            }
        }
    }

    public void delOrganizationMember(String organizationId, String userId) {
        UserRoleExample userRoleExample = new UserRoleExample();
        userRoleExample.createCriteria().andRoleIdLike("%org%").andUserIdEqualTo(userId).andSourceIdEqualTo(organizationId);
        userRoleMapper.deleteByExample(userRoleExample);
    }

    public List<User> getOrgMemberList(QueryOrgMemberRequest request) {
        return extUserRoleMapper.getOrgMemberList(request);
    }

    public boolean checkUserPassword(String userId, String password) {
        if (StringUtils.isBlank(userId)) {
            MSException.throwException(Translator.get("user_name_is_null"));
        }
        if (StringUtils.isBlank(password)) {
            MSException.throwException(Translator.get("password_is_null"));
        }
        UserExample example = new UserExample();
        example.createCriteria().andIdEqualTo(userId).andPasswordEqualTo(CodingUtil.md5(password));
        return userMapper.countByExample(example) > 0;
    }

    /**
     * 查询该组织外的其他用户列表
     */
    public List<User> getBesideOrgMemberList(String orgId) {
        return extUserRoleMapper.getBesideOrgMemberList(orgId);
    }

    public void setLanguage(String lang) {
        if (SessionUtils.getUser() != null) {
            User user = new User();
            user.setId(SessionUtils.getUser().getId());
            user.setLanguage(lang);
            updateUser(user);
            SessionUtils.getUser().setLanguage(lang);
        }
    }

    /*修改当前用户用户密码*/
    private User getUserPassDTO(UserPassDTO UserPassDTO) {
        if (SessionUtils.getUser() != null) {
            User user = userMapper.selectByPrimaryKey(SessionUtils.getUser().getId());
            String pwd = user.getPassword();
            String prepwd = CodingUtil.md5(UserPassDTO.getPassword(),"utf-8");
            String newped = UserPassDTO.getNewpassword();
            if (StringUtils.isNotBlank(prepwd)) {
                if (prepwd.trim().equals(pwd.trim())) {
                    user.setPassword(CodingUtil.md5(newped));
                    user.setUpdateTime(System.currentTimeMillis());
                    return user;
                }
            }
            MSException.throwException("密码修改失败");
        }
        return null;
    }

    public int updatePassword(UserPassDTO UserPassDTO) {
        User user = getUserPassDTO(UserPassDTO);
        return userMapper.updatePassword(user);
    }
    /*管理员修改用户密码*/
    private User getUserDTO(UserPassDTO UserPassDTO){
        User user= userMapper.selectByPrimaryKey(UserPassDTO.getId());
        String newped = UserPassDTO.getNewpassword();
        user.setPassword(CodingUtil.md5(newped));
        user.setUpdateTime(System.currentTimeMillis());
        return user;
    }
    public int updateUserPassword(UserPassDTO UserPassDTO){
        User user=getUserDTO(UserPassDTO);
        int i=userMapper.updatePassword(user);
        return i;
    }

}
