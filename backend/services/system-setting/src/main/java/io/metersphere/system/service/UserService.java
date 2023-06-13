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
import io.metersphere.system.dto.UserBatchCreateDTO;
import io.metersphere.system.dto.UserEditRequest;
import io.metersphere.system.dto.response.UserInfo;
import io.metersphere.system.mapper.UserMapper;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
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
    OperationLogService operationLogService;

    //批量添加用户记录日志
    public List<OperationLog> getBatchAddLogs(@Valid UserBatchCreateDTO userCreateDTO) {
        List<OperationLog> logs = new ArrayList<>();
        userCreateDTO.getUserInfoList().forEach(user -> {
            OperationLog log = new OperationLog();
            log.setId(UUID.randomUUID().toString());
            log.setCreateUser(user.getCreateUser());
            log.setType(OperationLogType.ADD.name());
            log.setModule(OperationLogModule.SYSTEM_USER);
            log.setSourceId(user.getId());
            log.setDetails(user.getName() + "(" + user.getEmail() + ")");
            logs.add(log);
        });
        return logs;
    }

    private void validateUserInfo(List<User> userList) {
        //判断参数内是否含有重复邮箱
        List<String> emailList = new ArrayList<>();
        List<String> repeatEmailList = new ArrayList<>();
        var userInDbMap = baseUserMapper.selectUserIdByEmailList(
                        userList.stream().map(User::getEmail).collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(User::getEmail, User::getId));
        for (User user : userList) {
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

    private void validateOrgAndProject(List<String> orgIdList, List<String> userRoleIdList) {
        // todo 判断组织和用户组是否合法
    }

    public UserBatchCreateDTO addBatch(UserBatchCreateDTO userCreateDTO) {
        this.validateUserInfo(userCreateDTO.getUserInfoList());
        this.validateOrgAndProject(userCreateDTO.getOrganizationIdList(), userCreateDTO.getUserRoleIdList());
        long createTime = System.currentTimeMillis();
        //添加用户
        for (User user : userCreateDTO.getUserInfoList()) {
            user.setId(UUID.randomUUID().toString());
            user.setPassword(CodingUtil.md5(user.getEmail()));
            user.setCreateTime(createTime);
            user.setUpdateTime(createTime);
            userMapper.insertSelective(user);
        }

        userRoleRelationService.batchSave(
                userCreateDTO.getOrganizationIdList(), userCreateDTO.getUserRoleIdList(), userCreateDTO.getUserInfoList());

        //写入操作日志
        operationLogService.batchAdd(this.getBatchAddLogs(userCreateDTO));
        return userCreateDTO;
    }


    public UserDTO getByEmail(String email) {
        return baseUserMapper.selectByEmail(email);
    }

    public List<UserInfo> list(BasePageRequest request) {
        List<UserInfo> returnList = new ArrayList<>();
        List<User> userList = baseUserMapper.selectByKeyword(request.getKeyword());
        List<String> userIdList = userList.stream().map(User::getId).collect(Collectors.toList());
        Map<String, UserInfo> roleAndOrganizationMap = userRoleRelationService.selectGlobalUserRoleAndOrganization(userIdList);
        for (User user : userList) {
            UserInfo userInfo = new UserInfo();
            BeanUtils.copyBean(userInfo, user);
            UserInfo roleOrgModel = roleAndOrganizationMap.get(user.getId());
            if (roleOrgModel != null) {
                userInfo.setUserRoleList(roleOrgModel.getUserRoleList());
                userInfo.setOrganizationList(roleOrgModel.getOrganizationList());
            }
            returnList.add(userInfo);
        }
        return returnList;
    }

    public UserEditRequest update(UserEditRequest userEditRequest) {
        User user = new User();
        BeanUtils.copyBean(user, userEditRequest);
        user.setUpdateTime(System.currentTimeMillis());
        userMapper.updateByPrimaryKeySelective(user);
        return userEditRequest;
    }
}
