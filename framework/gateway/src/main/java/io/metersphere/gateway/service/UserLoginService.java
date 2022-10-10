package io.metersphere.gateway.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.commons.constants.UserSource;
import io.metersphere.commons.constants.UserStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.CodingUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.GroupResourceDTO;
import io.metersphere.dto.UserDTO;
import io.metersphere.dto.UserGroupPermissionDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.request.LoginRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.server.WebSession;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserLoginService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserGroupMapper userGroupMapper;
    @Resource
    private GroupMapper groupMapper;
    @Resource
    private UserGroupPermissionMapper userGroupPermissionMapper;
    @Resource
    private ProjectMapper projectMapper;

    public Optional<SessionUser> login(LoginRequest request, WebSession session) {
        UserDTO userDTO;
        switch (request.getAuthenticate()) {
            case "OIDC":
            case "CAS":
                userDTO = loginSsoMode(request.getUsername(), request.getAuthenticate());
                break;
            case "LDAP":
                userDTO = loginLdapMode(request.getUsername(), request.getAuthenticate());
                break;
            default:
                userDTO = loginLocalMode(request.getUsername(), request.getPassword());
                break;
        }
        return Optional.of(SessionUser.fromUser(userDTO, session.getId()));
    }

    private UserDTO loginLdapMode(String userId, String authenticate) {
        return getLoginUser(userId, Collections.singletonList(authenticate));
    }

    private UserDTO loginSsoMode(String userId, String authType) {
        return getLoginUser(userId, Collections.singletonList(authType));
    }

    public UserDTO loginLocalMode(String userId, String password) {
        UserDTO user = getLoginUser(userId, Collections.singletonList(UserSource.LOCAL.name()));
        if (user == null) {
            user = getUserDTOByEmail(userId, UserSource.LOCAL.name());
            if (user == null) {
                throw new RuntimeException(Translator.get("password_is_incorrect"));
            }
            userId = user.getId();
        }
        // 密码验证
        if (!checkUserPassword(userId, password)) {
            throw new RuntimeException(Translator.get("password_is_incorrect"));
        }
        user.setPassword(null);
        return user;
    }

    public UserDTO getLoginUser(String userId, List<String> list) {
        UserExample example = new UserExample();
        example.createCriteria().andIdEqualTo(userId).andSourceIn(list);
        if (userMapper.countByExample(example) == 0) {
            return null;
        }
        return getUserDTO(userId);
    }

    public UserDTO getUserDTOByEmail(String email, String... source) {
        UserExample example = new UserExample();
        UserExample.Criteria criteria = example.createCriteria();
        criteria.andEmailEqualTo(email);

        if (!CollectionUtils.isEmpty(Arrays.asList(source))) {
            criteria.andSourceIn(Arrays.asList(source));
        }

        List<User> users = userMapper.selectByExample(example);

        if (users == null || users.size() <= 0) {
            return null;
        }

        return getUserDTO(users.get(0).getId());
    }

    public UserDTO getUserDTO(String userId) {

        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return null;
        }
        if (StringUtils.equals(user.getStatus(), UserStatus.DISABLED)) {
            throw new RuntimeException(Translator.get("user_has_been_disabled"));
        }
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);

        UserGroupPermissionDTO dto = getUserGroupPermission(userId);
        userDTO.setUserGroups(dto.getUserGroups());
        userDTO.setGroups(dto.getGroups());
        userDTO.setGroupPermissions(dto.getList());
        return userDTO;
    }

    private UserGroupPermissionDTO getUserGroupPermission(String userId) {
        UserGroupPermissionDTO permissionDTO = new UserGroupPermissionDTO();
        List<GroupResourceDTO> list = new ArrayList<>();
        UserGroupExample userGroupExample = new UserGroupExample();
        userGroupExample.createCriteria().andUserIdEqualTo(userId);
        List<UserGroup> userGroups = userGroupMapper.selectByExample(userGroupExample);
        if (CollectionUtils.isEmpty(userGroups)) {
            return permissionDTO;
        }
        permissionDTO.setUserGroups(userGroups);
        List<String> groupList = userGroups.stream().map(UserGroup::getGroupId).collect(Collectors.toList());
        GroupExample groupExample = new GroupExample();
        groupExample.createCriteria().andIdIn(groupList);
        List<Group> groups = groupMapper.selectByExample(groupExample);
        permissionDTO.setGroups(groups);
        for (Group gp : groups) {
            GroupResourceDTO dto = new GroupResourceDTO();
            dto.setGroup(gp);
            UserGroupPermissionExample userGroupPermissionExample = new UserGroupPermissionExample();
            userGroupPermissionExample.createCriteria().andGroupIdEqualTo(gp.getId());
            List<UserGroupPermission> userGroupPermissions = userGroupPermissionMapper.selectByExample(userGroupPermissionExample);
            dto.setUserGroupPermissions(userGroupPermissions);
            list.add(dto);
        }
        permissionDTO.setList(list);
        return permissionDTO;
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

    public User selectUser(String userId, String email) {
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            if (StringUtils.isNotBlank(email)) {
                UserExample example = new UserExample();
                example.createCriteria().andEmailEqualTo(email);
                List<User> users = userMapper.selectByExample(example);
                if (!CollectionUtils.isEmpty(users)) {
                    return users.get(0);
                }
            }
        }
        return user;
    }

    public void createOssUser(User user) {
        user.setCreateTime(System.currentTimeMillis());
        user.setUpdateTime(System.currentTimeMillis());
        user.setStatus(UserStatus.NORMAL);
        if (StringUtils.isBlank(user.getEmail())) {
            user.setEmail(user.getId() + "@metershpere.io");
        }
        userMapper.insertSelective(user);
    }

    public void addLdapUser(User user) {
        user.setCreateTime(System.currentTimeMillis());
        user.setUpdateTime(System.currentTimeMillis());
        user.setStatus(UserStatus.NORMAL);
        checkEmailIsExist(user.getEmail());
        userMapper.insertSelective(user);
    }

    private void checkEmailIsExist(String email) {
        UserExample userExample = new UserExample();
        UserExample.Criteria criteria = userExample.createCriteria();
        criteria.andEmailEqualTo(email);
        List<User> userList = userMapper.selectByExample(userExample);
        if (!CollectionUtils.isEmpty(userList)) {
            MSException.throwException(Translator.get("user_email_already_exists"));
        }
    }

    public void updateUser(User user) {
        // todo 提取重复代码
        if (StringUtils.isNotBlank(user.getEmail())) {
            UserExample example = new UserExample();
            UserExample.Criteria criteria = example.createCriteria();
            criteria.andEmailEqualTo(user.getEmail());
            criteria.andIdNotEqualTo(user.getId());
            if (userMapper.countByExample(example) > 0) {
                MSException.throwException(Translator.get("user_email_already_exists"));
            }
        }
        user.setPassword(null);
        user.setUpdateTime(System.currentTimeMillis());
        // 变更前
        User userFromDB = userMapper.selectByPrimaryKey(user.getId());
        // last workspace id 变了
        if (user.getLastWorkspaceId() != null && !StringUtils.equals(user.getLastWorkspaceId(), userFromDB.getLastWorkspaceId())) {
            List<Project> projects = getProjectListByWsAndUserId(user.getId(), user.getLastWorkspaceId());
            if (projects.size() > 0) {
                // 如果传入的 last_project_id 是 last_workspace_id 下面的
                boolean present = projects.stream().anyMatch(p -> StringUtils.equals(p.getId(), user.getLastProjectId()));
                if (!present) {
                    user.setLastProjectId(projects.get(0).getId());
                }
            } else {
                user.setLastProjectId("");
            }
        }
        // 执行变更
        userMapper.updateByPrimaryKeySelective(user);
        if (StringUtils.equals(user.getStatus(), UserStatus.DISABLED)) {
            SessionUtils.kickOutUser(user.getId());
        }
    }

    private List<Project> getProjectListByWsAndUserId(String userId, String workspaceId) {
        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andWorkspaceIdEqualTo(workspaceId);
        List<Project> projects = projectMapper.selectByExample(projectExample);

        UserGroupExample userGroupExample = new UserGroupExample();
        userGroupExample.createCriteria().andUserIdEqualTo(userId);
        List<UserGroup> userGroups = userGroupMapper.selectByExample(userGroupExample);
        List<Project> projectList = new ArrayList<>();
        userGroups.forEach(userGroup -> projects.forEach(project -> {
            if (StringUtils.equals(userGroup.getSourceId(), project.getId())) {
                if (!projectList.contains(project)) {
                    projectList.add(project);
                }
            }
        }));
        return projectList;
    }
}
