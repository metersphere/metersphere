package io.metersphere.system.service;

import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.sdk.dto.LogDTO;
import io.metersphere.sdk.dto.TableBatchProcessDTO;
import io.metersphere.sdk.dto.builder.LogDTOBuilder;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.*;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.service.OperationLogService;
import io.metersphere.system.mapper.OrganizationMapper;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.mapper.UserRoleMapper;
import io.metersphere.system.request.user.UserChangeEnableRequest;
import io.metersphere.system.request.user.UserEditRequest;
import io.metersphere.system.request.user.UserRoleBatchRelationRequest;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserLogService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserToolService userToolService;
    @Resource
    private OperationLogService operationLogService;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private OrganizationMapper organizationMapper;
    @Resource
    private UserRoleMapper userRoleMapper;

    //批量添加用户记录日志
    public List<LogDTO> getBatchAddLogs(@Valid List<User> userList) {
        List<LogDTO> logs = new ArrayList<>();
        userList.forEach(user -> {
            LogDTO log = LogDTOBuilder.builder()
                    .projectId(OperationLogConstants.SYSTEM)
                    .organizationId(OperationLogConstants.SYSTEM)
                    .type(OperationLogType.ADD.name())
                    .module(OperationLogModule.SETTING_SYSTEM_USER_SINGLE)
                    .method(HttpMethodConstants.POST.name())
                    .path("/system/user/addUser")
                    .sourceId(user.getId())
                    .content(user.getName() + "(" + user.getEmail() + ")")
                    .originalValue(JSON.toJSONBytes(user))
                    .createUser(user.getCreateUser())
                    .build().getLogDTO();
            logs.add(log);
        });
        return logs;
    }

    public LogDTO updateLog(UserEditRequest request) {
        User user = userMapper.selectByPrimaryKey(request.getId());
        if (user != null) {
            LogDTO dto = LogDTOBuilder.builder()
                    .projectId(OperationLogConstants.SYSTEM)
                    .organizationId(OperationLogConstants.SYSTEM)
                    .type(OperationLogType.UPDATE.name())
                    .module(OperationLogModule.SETTING_SYSTEM_USER_SINGLE)
                    .method(HttpMethodConstants.POST.name())
                    .path("/system/user/update")
                    .sourceId(request.getId())
                    .content(user.getName())
                    .originalValue(JSON.toJSONBytes(user))
                    .build().getLogDTO();
            return dto;
        }
        return null;
    }

    //批量开启和关闭
    public List<LogDTO> batchUpdateEnableLog(UserChangeEnableRequest request) {
        List<LogDTO> logDTOList = new ArrayList<>();
        request.setSelectIds(userToolService.getBatchUserIds(request));
        List<User> userList = userToolService.selectByIdList(request.getSelectIds());
        for (User user : userList) {
            LogDTO dto = LogDTOBuilder.builder()
                    .projectId(OperationLogConstants.SYSTEM)
                    .organizationId(OperationLogConstants.SYSTEM)
                    .type(OperationLogType.UPDATE.name())
                    .module(OperationLogModule.SETTING_SYSTEM_USER_SINGLE)
                    .method(HttpMethodConstants.POST.name())
                    .path("/system/user/update/enable")
                    .sourceId(user.getId())
                    .content((request.isEnable() ? Translator.get("user.enable") : Translator.get("user.disable")) + ":" + user.getName())
                    .originalValue(JSON.toJSONBytes(user))
                    .build().getLogDTO();
            logDTOList.add(dto);
        }
        return logDTOList;
    }

    //批量重置密码
    public List<LogDTO> resetPasswordLog(TableBatchProcessDTO request) {
        request.setSelectIds(userToolService.getBatchUserIds(request));
        List<LogDTO> returnList = new ArrayList<>();
        UserExample example = new UserExample();
        example.createCriteria().andIdIn(request.getSelectIds());
        List<User> userList = userMapper.selectByExample(example);
        for (User user : userList) {
            LogDTO dto = LogDTOBuilder.builder()
                    .projectId(OperationLogConstants.SYSTEM)
                    .organizationId(OperationLogConstants.SYSTEM)
                    .type(OperationLogType.UPDATE.name())
                    .module(OperationLogModule.SETTING_SYSTEM_USER_SINGLE)
                    .method(HttpMethodConstants.POST.name())
                    .path("/system/user/reset/password")
                    .sourceId(user.getId())
                    .content(Translator.get("user.reset.password") + " : " + user.getName())
                    .originalValue(JSON.toJSONBytes(user))
                    .build().getLogDTO();
            returnList.add(dto);
        }
        return returnList;
    }

    //删除日志
    public List<LogDTO> deleteLog(TableBatchProcessDTO request) {
        List<LogDTO> logDTOList = new ArrayList<>();
        request.getSelectIds().forEach(item -> {
            User user = userMapper.selectByPrimaryKey(item);
            if (user != null) {
                LogDTO dto = LogDTOBuilder.builder()
                        .projectId(OperationLogConstants.SYSTEM)
                        .organizationId(OperationLogConstants.SYSTEM)
                        .type(OperationLogType.DELETE.name())
                        .module(OperationLogModule.SETTING_SYSTEM_USER_SINGLE)
                        .method(HttpMethodConstants.POST.name())
                        .path("/system/user/delete")
                        .sourceId(user.getId())
                        .content(Translator.get("user.delete") + " : " + user.getName())
                        .originalValue(JSON.toJSONBytes(user))
                        .build().getLogDTO();
                logDTOList.add(dto);

            }
        });
        return logDTOList;
    }

    public void batchAddProjectLog(UserRoleBatchRelationRequest request, String operator) {
        List<LogDTO> logs = new ArrayList<>();
        List<String> userIds = userToolService.getBatchUserIds(request);
        List<User> userList = userToolService.selectByIdList(userIds);
        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andIdIn(request.getRoleIds());
        List<String> projectNameList = projectMapper.selectByExample(projectExample)
                .stream().map(Project::getName).collect(Collectors.toList());
        String projectNames = StringUtils.join(projectNameList, ",");
        for (User user : userList) {
            //用户管理处修改了用户的组织。
            LogDTO log = LogDTOBuilder.builder()
                    .projectId(OperationLogConstants.SYSTEM)
                    .createUser(operator)
                    .method(HttpMethodConstants.POST.name())
                    .organizationId(OperationLogConstants.SYSTEM)
                    .sourceId(user.getId())
                    .type(OperationLogType.UPDATE.name())
                    .module(OperationLogModule.SETTING_SYSTEM_USER_SINGLE)
                    .content(user.getName() + Translator.get("user.add.project") + ":" + projectNames)
                    .path("/system/user/add-project-member")
                    .modifiedValue(JSON.toJSONBytes(request.getRoleIds()))
                    .build().getLogDTO();
            logs.add(log);
        }
        operationLogService.batchAdd(logs);
    }

    public void batchAddUserRoleLog(UserRoleBatchRelationRequest request, String operator) {
        List<LogDTO> logs = new ArrayList<>();
        List<String> userIds = userToolService.getBatchUserIds(request);
        List<User> userList = userToolService.selectByIdList(userIds);

        UserRoleExample example = new UserRoleExample();
        example.createCriteria().andIdIn(request.getRoleIds());
        List<String> roleNameList = userRoleMapper.selectByExample(example)
                .stream().map(UserRole::getName).collect(Collectors.toList());
        String roleNames = StringUtils.join(roleNameList, ",");

        for (User user : userList) {
            //用户管理处修改了用户的组织。
            LogDTO log = LogDTOBuilder.builder()
                    .projectId(OperationLogConstants.SYSTEM)
                    .module(OperationLogModule.SETTING_SYSTEM_USER_SINGLE)
                    .createUser(operator)
                    .organizationId(OperationLogConstants.SYSTEM)
                    .sourceId(user.getId())
                    .type(OperationLogType.UPDATE.name())
                    .content(user.getName() + Translator.get("user.add.group") + ":" + roleNames)
                    .path("/system/user/add/batch/user-role")
                    .method(HttpMethodConstants.POST.name())
                    .modifiedValue(JSON.toJSONBytes(request.getRoleIds()))
                    .build().getLogDTO();
            logs.add(log);
        }
        operationLogService.batchAdd(logs);
    }
    public void batchAddOrgLog(UserRoleBatchRelationRequest request, String operator) {
        List<LogDTO> logs = new ArrayList<>();
        List<String> userIds = userToolService.getBatchUserIds(request);
        List<User> userList = userToolService.selectByIdList(userIds);

        OrganizationExample example = new OrganizationExample();
        example.createCriteria().andIdIn(request.getRoleIds());
        List<String> roleNameList = organizationMapper.selectByExample(example)
                .stream().map(Organization::getName).collect(Collectors.toList());
        String roleNames = StringUtils.join(roleNameList, ",");

        for (User user : userList) {
            //用户管理处修改了用户的组织。
            LogDTO log = LogDTOBuilder.builder()
                    .projectId(OperationLogConstants.SYSTEM)
                    .module(OperationLogModule.SETTING_SYSTEM_USER_SINGLE)
                    .createUser(operator)
                    .organizationId(OperationLogConstants.SYSTEM)
                    .sourceId(user.getId())
                    .type(OperationLogType.UPDATE.name())
                    .content(user.getName() + Translator.get("user.add.org") + ":" + roleNames)
                    .path("/system/user/add-org-member")
                    .method(HttpMethodConstants.POST.name())
                    .modifiedValue(JSON.toJSONBytes(request.getRoleIds()))
                    .build().getLogDTO();
            logs.add(log);
        }
        operationLogService.batchAdd(logs);
    }

    public void addEmailInviteLog(List<UserInvite> userInviteList, String inviteUserId) {
        User inviteUser = userMapper.selectByPrimaryKey(inviteUserId);
        List<LogDTO> saveLogs = new ArrayList<>();
        userInviteList.forEach(userInvite -> {
            LogDTO log = LogDTOBuilder.builder()
                    .projectId(OperationLogConstants.SYSTEM)
                    .module(OperationLogModule.SETTING_SYSTEM_USER_SINGLE)
                    .createUser(inviteUserId)
                    .organizationId(OperationLogConstants.SYSTEM)
                    .sourceId(inviteUserId)
                    .type(OperationLogType.ADD.name())
                    .content(inviteUser.getName() + Translator.get("user.invite.email") + ":" + userInvite.getEmail())
                    .path("/system/user/invite")
                    .method(HttpMethodConstants.POST.name())
                    .modifiedValue(JSON.toJSONBytes(userInvite))
                    .build().getLogDTO();
            saveLogs.add(log);
        });
        operationLogService.batchAdd(saveLogs);
    }

    public void addRegisterLog(User user, UserInvite userInvite) {
        User inviteUser = userMapper.selectByPrimaryKey(userInvite.getInviteUser());
        LogDTO log = LogDTOBuilder.builder()
                .projectId(OperationLogConstants.SYSTEM)
                .module(OperationLogModule.SETTING_SYSTEM_USER_SINGLE)
                .createUser(user.getName())
                .organizationId(OperationLogConstants.SYSTEM)
                .sourceId(user.getId())
                .type(OperationLogType.ADD.name())
                .content(user.getName() + Translator.get("register.by.invite") + inviteUser.getName())
                .path("/system/user/register-by-invite")
                .method(HttpMethodConstants.POST.name())
                .modifiedValue(JSON.toJSONBytes(userInvite))
                .build().getLogDTO();
        operationLogService.add(log);
    }
}
