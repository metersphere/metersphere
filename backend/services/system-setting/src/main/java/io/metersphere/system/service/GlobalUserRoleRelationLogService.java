package io.metersphere.system.service;

import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.sdk.dto.LogDTO;
import io.metersphere.sdk.dto.OptionDTO;
import io.metersphere.sdk.dto.UserDTO;
import io.metersphere.sdk.dto.request.GlobalUserRoleRelationUpdateRequest;
import io.metersphere.sdk.log.constants.OperationLogModule;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.mapper.BaseUserMapper;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.UserRole;
import io.metersphere.system.domain.UserRoleExample;
import io.metersphere.system.domain.UserRoleRelation;
import io.metersphere.system.dto.request.user.UserAndRoleBatchRequest;
import io.metersphere.system.mapper.UserRoleMapper;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jianxing
 * @date : 2023-6-12
 */
@Service
public class GlobalUserRoleRelationLogService {

    @Resource
    private UserRoleRelationMapper userRoleRelationMapper;
    @Resource
    private BaseUserMapper baseUserMapper;
    @Resource
    private UserRoleMapper userRoleMapper;

    /**
     * 添加接口日志
     *
     * @param request
     * @return
     */
    public LogDTO addLog(GlobalUserRoleRelationUpdateRequest request) {
        UserRole userRole = userRoleMapper.selectByPrimaryKey(request.getRoleId());
        List<String> userIds = request.getUserIds();
        List<OptionDTO> users = baseUserMapper.selectUserOptionByIds(userIds);
        LogDTO dto = new LogDTO(
                OperationLogConstants.SYSTEM,
                OperationLogConstants.SYSTEM,
                userRole.getId(),
                null,
                OperationLogType.ADD.name(),
                OperationLogModule.SYSTEM_USER_ROLE_RELATION,
                userRole.getName());

        dto.setOriginalValue(JSON.toJSONBytes(users));
        return dto;
    }

    public List<LogDTO> batchAddLog(UserAndRoleBatchRequest request) {
        UserRoleExample example = new UserRoleExample();
        example.createCriteria().andIdIn(request.getRoleIds());
        List<UserRole> userRoles = userRoleMapper.selectByExample(example);
        List<String> userIds = request.getUserIds();
        List<OptionDTO> users = baseUserMapper.selectUserOptionByIds(userIds);

        List<LogDTO> returnList = new ArrayList<>();
        for (UserRole userRole : userRoles) {
            LogDTO dto = new LogDTO(
                    OperationLogConstants.SYSTEM,
                    OperationLogConstants.SYSTEM,
                    userRole.getId(),
                    null,
                    OperationLogType.ADD.name(),
                    OperationLogModule.SYSTEM_USER_ROLE_RELATION,
                    userRole.getName());

            dto.setOriginalValue(JSON.toJSONBytes(users));
            returnList.add(dto);

        }
        return returnList;
    }

    /**
     * 删除接口日志
     *
     * @param id
     * @return
     */
    public LogDTO deleteLog(String id) {
        UserRoleRelation userRoleRelation = userRoleRelationMapper.selectByPrimaryKey(id);
        UserRole userRole = userRoleMapper.selectByPrimaryKey(userRoleRelation.getRoleId());
        LogDTO dto = new LogDTO(
                OperationLogConstants.SYSTEM,
                OperationLogConstants.SYSTEM,
                userRole.getId(),
                null,
                OperationLogType.DELETE.name(),
                OperationLogModule.SYSTEM_USER_ROLE_RELATION,
                userRole.getName());

        UserDTO userDTO = baseUserMapper.selectById(userRoleRelation.getUserId());
        OptionDTO optionDTO = new OptionDTO();
        optionDTO.setId(userDTO.getId());
        optionDTO.setName(userDTO.getName());
        // 记录用户id和name
        dto.setOriginalValue(JSON.toJSONBytes(optionDTO));
        return dto;
    }
}
