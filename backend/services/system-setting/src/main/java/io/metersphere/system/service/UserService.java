package io.metersphere.system.service;

import io.metersphere.sdk.dto.BasePageRequest;
import io.metersphere.sdk.dto.UserDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.log.constants.OperationLogModule;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.log.service.OperationLogService;
import io.metersphere.sdk.mapper.BaseUserMapper;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.CodingUtil;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.OperationLog;
import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserExample;
import io.metersphere.system.dto.UserBatchCreateDTO;
import io.metersphere.system.dto.UserCreateInfo;
import io.metersphere.system.dto.UserEditEnableRequest;
import io.metersphere.system.dto.UserEditRequest;
import io.metersphere.system.dto.response.UserTableResponse;
import io.metersphere.system.mapper.UserMapper;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserService {
    @Resource
    private BaseUserMapper baseUserMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserRoleRelationService userRoleRelationService;
    @Resource
    private OperationLogService operationLogService;
    @Resource
    private GlobalUserRoleService globalUserRoleService;
    @Resource
    private UserRoleService userRoleService;

    //批量添加用户记录日志
    public List<OperationLog> getBatchAddLogs(@Valid List<User> userList) {
        List<OperationLog> logs = new ArrayList<>();
        userList.forEach(user -> {
            OperationLog log = new OperationLog();
            log.setId(UUID.randomUUID().toString());
            log.setCreateUser(user.getCreateUser());
            log.setProjectId("system");
            log.setType(OperationLogType.ADD.name());
            log.setModule(OperationLogModule.SYSTEM_USER);
            log.setMethod("addUser");
            log.setCreateTime(user.getCreateTime());
            log.setSourceId(user.getId());
            log.setDetails(user.getName() + "(" + user.getEmail() + ")");
            logs.add(log);
        });
        return logs;
    }

    private void validateUserInfo(List<UserCreateInfo> userList) {
        //判断参数内是否含有重复邮箱
        List<String> emailList = new ArrayList<>();
        List<String> repeatEmailList = new ArrayList<>();
        var userInDbMap = baseUserMapper.selectUserIdByEmailList(
                        userList.stream().map(UserCreateInfo::getEmail).collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(User::getEmail, User::getId));
        for (UserCreateInfo user : userList) {
            if (emailList.contains(user.getEmail())) {
                repeatEmailList.add(user.getEmail());
            } else {
                //判断邮箱是否已存在数据库中
                if (userInDbMap.containsKey(user.getEmail())) {
                    repeatEmailList.add(user.getEmail());
                } else {
                    emailList.add(user.getEmail());
                }
            }
        }
        if (CollectionUtils.isNotEmpty(repeatEmailList)) {
            throw new MSException(Translator.get("user.email.repeat", repeatEmailList.toString()));
        }
    }

    public UserBatchCreateDTO addBatch(UserBatchCreateDTO userCreateDTO, String source, String operator) {
        this.validateUserInfo(userCreateDTO.getUserInfoList());
        globalUserRoleService.checkRoleIsGlobalAndHaveMember(userCreateDTO.getUserRoleIdList(), true);
        long createTime = System.currentTimeMillis();
        List<User> saveUserList = new ArrayList<>();
        //添加用户
        for (UserCreateInfo userInfo : userCreateDTO.getUserInfoList()) {
            userInfo.setId(UUID.randomUUID().toString());
            User user = new User();
            BeanUtils.copyBean(user, userInfo);
            user.setCreateUser(operator);
            user.setCreateTime(createTime);
            user.setUpdateUser(operator);
            user.setUpdateTime(createTime);
            user.setPassword(CodingUtil.md5(user.getEmail()));
            user.setSource(source);
            userMapper.insertSelective(user);
            saveUserList.add(user);
        }

        userRoleRelationService.batchSave(userCreateDTO.getUserRoleIdList(), saveUserList);

        //写入操作日志
        operationLogService.batchAdd(this.getBatchAddLogs(saveUserList));
        return userCreateDTO;
    }


    public UserDTO getUserDTOByEmail(String email) {
        UserDTO userDTO = baseUserMapper.selectByEmail(email);
        if (userDTO != null) {
            userDTO.setUserRoleRelations(
                    userRoleRelationService.selectByUserId(userDTO.getId())
            );
            userDTO.setUserRoles(
                    userRoleService.selectByUserRoleRelations(userDTO.getUserRoleRelations())
            );
        }
        return userDTO;
    }

    public List<UserTableResponse> list(BasePageRequest request) {
        List<UserTableResponse> returnList = new ArrayList<>();
        List<User> userList = baseUserMapper.selectByKeyword(request.getKeyword());
        List<String> userIdList = userList.stream().map(User::getId).collect(Collectors.toList());
        Map<String, UserTableResponse> roleAndOrganizationMap = userRoleRelationService.selectGlobalUserRoleAndOrganization(userIdList);
        for (User user : userList) {
            UserTableResponse userInfo = new UserTableResponse();
            BeanUtils.copyBean(userInfo, user);
            UserTableResponse roleOrgModel = roleAndOrganizationMap.get(user.getId());
            if (roleOrgModel != null) {
                userInfo.setUserRoleList(roleOrgModel.getUserRoleList());
                userInfo.setOrganizationList(roleOrgModel.getOrganizationList());
            }
            returnList.add(userInfo);
        }
        return returnList;
    }

    public UserEditRequest updateUser(UserEditRequest userEditRequest, String operator) {
        //检查用户组合法性
        globalUserRoleService.checkRoleIsGlobalAndHaveMember(userEditRequest.getUserRoleIdList(), true);
        User user = new User();
        BeanUtils.copyBean(user, userEditRequest);
        user.setUpdateUser(operator);
        user.setUpdateTime(System.currentTimeMillis());
        user.setCreateUser(null);
        user.setCreateTime(null);
        userMapper.updateByPrimaryKeySelective(user);
        userRoleRelationService.updateUserSystemGlobalRole(user, user.getUpdateUser(), userEditRequest.getUserRoleIdList());
        return userEditRequest;
    }

    public UserEditEnableRequest updateUserEnable(UserEditEnableRequest request, String operator) {
        this.checkUserInDb(request.getUserIdList());
        UserExample userExample = new UserExample();
        userExample.createCriteria().andIdIn(
                request.getUserIdList()
        );
        User updateUser = new User();
        updateUser.setEnable(request.isEnable());
        updateUser.setUpdateUser(operator);
        updateUser.setUpdateTime(System.currentTimeMillis());
        userMapper.updateByExampleSelective(updateUser, userExample);
        return request;
    }

    private void checkUserInDb(@Valid @NotEmpty List<String> userIdList) {
        List<String> userInDb = baseUserMapper.selectUserIdByIdList(userIdList);
        if (userIdList.size() != userInDb.size()) {
            throw new MSException(Translator.get("user.not.exist"));
        }
    }
}
