package io.metersphere.system.service;

import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.sdk.dto.LogDTO;
import io.metersphere.sdk.dto.TableBatchProcessDTO;
import io.metersphere.sdk.dto.builder.LogDTOBuilder;
import io.metersphere.sdk.log.constants.OperationLogModule;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.log.service.OperationLogService;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserExample;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.request.user.UserEditRequest;
import io.metersphere.system.request.user.UserRoleBatchRelationRequest;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserLogService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserService userService;
    @Resource
    private OperationLogService operationLogService;

    //批量添加用户记录日志
    public List<LogDTO> getBatchAddLogs(@Valid List<User> userList) {
        List<LogDTO> logs = new ArrayList<>();
        userList.forEach(user -> {
            LogDTO log = new LogDTO();
            log.setId(UUID.randomUUID().toString());
            log.setCreateUser(user.getCreateUser());
            log.setProjectId(OperationLogConstants.SYSTEM);
            log.setOrganizationId(OperationLogConstants.SYSTEM);
            log.setType(OperationLogType.ADD.name());
            log.setModule(OperationLogModule.SYSTEM_USER);
            log.setMethod("addUser");
            log.setCreateTime(user.getCreateTime());
            log.setSourceId(user.getId());
            log.setContent(user.getName() + "(" + user.getEmail() + ")");
            log.setOriginalValue(JSON.toJSONBytes(user));
            logs.add(log);
        });
        return logs;
    }

    public LogDTO updateLog(UserEditRequest request) {
        User user = userMapper.selectByPrimaryKey(request.getId());
        if (user != null) {
            LogDTO dto = new LogDTO(
                    OperationLogConstants.SYSTEM,
                    OperationLogConstants.SYSTEM,
                    request.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.SYSTEM_USER,
                    JSON.toJSONString(user));
            dto.setPath("/update");
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(user));
            return dto;
        }
        return null;
    }

    public List<LogDTO> batchUpdateLog(TableBatchProcessDTO request) {
        List<LogDTO> logDTOList = new ArrayList<>();
        request.setSelectIds(userService.getBatchUserIds(request));
        List<User> userList = userService.selectByIdList(request.getSelectIds());
        for (User user : userList) {
            LogDTO dto = new LogDTO(
                    OperationLogConstants.SYSTEM,
                    OperationLogConstants.SYSTEM,
                    user.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.SYSTEM_USER,
                    JSON.toJSONString(user));
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(user));
            logDTOList.add(dto);
        }
        return logDTOList;
    }

    /**
     * @param request 批量重置密码  用于记录Log使用
     */
    public List<LogDTO> resetPasswordLog(TableBatchProcessDTO request) {
        request.setSelectIds(userService.getBatchUserIds(request));
        List<LogDTO> returnList = new ArrayList<>();
        UserExample example = new UserExample();
        example.createCriteria().andIdIn(request.getSelectIds());
        List<User> userList = userMapper.selectByExample(example);
        for (User user : userList) {
            LogDTO dto = new LogDTO(
                    OperationLogConstants.SYSTEM,
                    OperationLogConstants.SYSTEM,
                    user.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.SYSTEM_USER,
                    user.getName());
            dto.setPath("/reset/password");
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(user));
            returnList.add(dto);
        }
        return returnList;
    }

    public List<LogDTO> deleteLog(TableBatchProcessDTO request) {
        List<LogDTO> logDTOList = new ArrayList<>();
        request.getSelectIds().forEach(item -> {
            User user = userMapper.selectByPrimaryKey(item);
            if (user != null) {

                LogDTO dto = new LogDTO(
                        OperationLogConstants.SYSTEM,
                        OperationLogConstants.SYSTEM,
                        user.getId(),
                        user.getCreateUser(),
                        OperationLogType.DELETE.name(),
                        OperationLogModule.SYSTEM_PROJECT,
                        user.getName());

                dto.setPath("/delete");
                dto.setMethod(HttpMethodConstants.POST.name());
                dto.setOriginalValue(JSON.toJSONBytes(user));
                logDTOList.add(dto);
            }
        });
        return logDTOList;
    }

    public void batchAddProjectLog(UserRoleBatchRelationRequest request, String operator) {
        List<LogDTO> logs = new ArrayList<>();
        List<String> userIds = userService.getBatchUserIds(request);
        List<User> userList = userService.selectByIdList(userIds);
        for (User user : userList) {
            //用户管理处修改了用户的组织。
            LogDTO log = LogDTOBuilder.builder()
                    .projectId(OperationLogConstants.SYSTEM)
                    .createUser(operator)
                    .organizationId(OperationLogConstants.SYSTEM)
                    .sourceId(user.getId())
                    .type(OperationLogType.UPDATE.name())
                    .module(OperationLogModule.SYSTEM_USER)
                    .content(user.getName())
                    .path("/system/user/add-project-member")
                    .modifiedValue(JSON.toJSONBytes(request.getRoleIds()))
                    .build().getLogDTO();
            logs.add(log);
        }
        operationLogService.batchAdd(logs);
    }

    public void batchAddOrgLog(UserRoleBatchRelationRequest request, String operator) {
        List<LogDTO> logs = new ArrayList<>();
        List<String> userIds = userService.getBatchUserIds(request);
        List<User> userList = userService.selectByIdList(userIds);
        for (User user : userList) {
            //用户管理处修改了用户的组织。
            LogDTO log = LogDTOBuilder.builder()
                    .projectId(OperationLogConstants.SYSTEM)
                    .module(OperationLogModule.SYSTEM_USER)
                    .createUser(operator)
                    .organizationId(OperationLogConstants.SYSTEM)
                    .sourceId(user.getId())
                    .type(OperationLogType.UPDATE.name())
                    .content(user.getName())
                    .path("/system/user/add-org-member")
                    .method(HttpMethodConstants.POST.name())
                    .modifiedValue(JSON.toJSONBytes(request.getRoleIds()))
                    .build().getLogDTO();
            logs.add(log);
        }
        operationLogService.batchAdd(logs);
    }

}
