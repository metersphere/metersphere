package io.metersphere.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.OrganizationMapper;
import io.metersphere.base.mapper.RoleMapper;
import io.metersphere.base.mapper.UserMapper;
import io.metersphere.base.mapper.UserRoleMapper;
import io.metersphere.base.mapper.ext.ExtUserRoleMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.dto.UserDTO;
import io.metersphere.dto.UserOperateDTO;
import io.metersphere.dto.UserRoleDTO;
import io.metersphere.dto.UserRoleHelpDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
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

    public UserDTO insert(User user) {
        checkUserParam(user);
        createUser(user);
        return getUserDTO(user.getId());
    }

    private void checkUserParam(User user) {
        if (StringUtils.isBlank(user.getName())) {
            MSException.throwException("user_name_empty");
        }

        if (StringUtils.isBlank(user.getEmail())) {
            MSException.throwException("user_email_empty");
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

        UserExample userExample = new UserExample();
        UserExample.Criteria criteria = userExample.createCriteria();
        criteria.andEmailEqualTo(user.getEmail());
        List<User> userList = userMapper.selectByExample(userExample);
        if (!CollectionUtils.isEmpty(userList)) {
            MSException.throwException("user_email_is_exist");
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

    public void deleteUser(String userId) {
        userMapper.deleteByPrimaryKey(userId);
    }

    public void updateUser(User user) {
        user.setUpdateTime(System.currentTimeMillis());
        userMapper.updateByPrimaryKeySelective(user);
    }

    public List<Role> getUserRolesList(String userId) {
        UserRoleExample userRoleExample = new UserRoleExample();
        userRoleExample.createCriteria().andUserIdEqualTo(userId);
        List<UserRole> userRolesList = userRoleMapper.selectByExample(userRoleExample);
        List<String> roleIds = userRolesList.stream().map(UserRole::getRoleId).collect(Collectors.toList());
        RoleExample roleExample = new RoleExample();
        roleExample.createCriteria().andIdIn(roleIds);
        return roleMapper.selectByExample(roleExample);
    }

    public List<UserRoleDTO> getUserRoleList(String userId) {
        if (StringUtils.isEmpty(userId)) {
            return new ArrayList<>();
        }
        return convertUserRoleDTO(extUserRoleMapper.getUserRoleHelpList(userId));
    }

    private List<UserRoleDTO> convertUserRoleDTO(List<UserRoleHelpDTO> helpDTOList) {
        StringBuilder buffer = new StringBuilder();

        Map<String, UserRoleDTO> roleMap = new HashMap<>();

        List<UserRoleDTO> resultList = new ArrayList<>();

        List<UserRoleDTO> otherList = new ArrayList<>();

        Set<String> orgSet = new HashSet<>();

        Set<String> workspaceSet = new HashSet<>();

        for (UserRoleHelpDTO helpDTO : helpDTOList) {
            UserRoleDTO userRoleDTO = roleMap.get(helpDTO.getSourceId());

            if (userRoleDTO == null) {
                userRoleDTO = new UserRoleDTO();

                if (!StringUtils.isEmpty(helpDTO.getParentId())) {
                    workspaceSet.add(helpDTO.getParentId());
                    userRoleDTO.setType("workspace");
                } else {
                    orgSet.add(helpDTO.getSourceId());
                    userRoleDTO.setType("organization");
                }

                userRoleDTO.setId(helpDTO.getSourceId());
                userRoleDTO.setRoleId(helpDTO.getRoleId());
                userRoleDTO.setName(helpDTO.getSourceName());
                userRoleDTO.setParentId(helpDTO.getParentId());
                userRoleDTO.setDesc(helpDTO.getRoleName());

            } else {
                userRoleDTO.setDesc(userRoleDTO.getDesc() + "," + helpDTO.getRoleName());
            }
            roleMap.put(helpDTO.getSourceId(), userRoleDTO);
        }

        if (!StringUtils.isEmpty(buffer.toString())) {
            UserRoleDTO dto = new UserRoleDTO();
            dto.setId("admin");
            dto.setType("admin");
            dto.setDesc(buffer.toString());
            resultList.add(dto);
        }

        for (String org : orgSet) {
            workspaceSet.remove(org);
        }

        List<UserRoleDTO> orgWorkSpace = new ArrayList<>(roleMap.values());

        if (!CollectionUtils.isEmpty(workspaceSet)) {
            for (String orgId : workspaceSet) {
                Organization organization = organizationMapper.selectByPrimaryKey(orgId);
                if (organization != null) {
                    UserRoleDTO dto = new UserRoleDTO();
                    dto.setId(orgId);
                    dto.setName(organization.getName());
                    dto.setSwitchable(false);
                    dto.setType("organization");
                    orgWorkSpace.add(dto);
                }
            }
        }

        orgWorkSpace.sort((o1, o2) -> {
            if (o1.getParentId() == null) {
                return -1;
            }

            if (o2.getParentId() == null) {
                return 1;
            }

            return o1.getParentId().compareTo(o2.getParentId());
        });
        resultList.addAll(orgWorkSpace);
        resultList.addAll(otherList);

        return resultList;
    }

    public void switchUserRole(UserDTO user, String sourceId) {
        User newUser = new User();
        BeanUtils.copyProperties(user, newUser);
        newUser.setLastSourceId(sourceId);
        userMapper.updateByPrimaryKeySelective(newUser);
    }

    public User getUserInfo(String userId) {
        return userMapper.selectByPrimaryKey(userId);
    }
}
