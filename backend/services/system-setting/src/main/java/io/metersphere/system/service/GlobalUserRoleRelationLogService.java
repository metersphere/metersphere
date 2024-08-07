package io.metersphere.system.service;

import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.UserRole;
import io.metersphere.system.domain.UserRoleRelation;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.sdk.request.GlobalUserRoleRelationUpdateRequest;
import io.metersphere.system.dto.user.UserDTO;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.mapper.BaseUserMapper;
import io.metersphere.system.mapper.UserRoleMapper;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

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
                OperationLogType.UPDATE.name(),
                OperationLogModule.SETTING_SYSTEM_USER_GROUP,
                userRole.getName());

        dto.setOriginalValue(JSON.toJSONBytes(users));
        return dto;
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
                OperationLogType.UPDATE.name(),
                OperationLogModule.SETTING_SYSTEM_USER_GROUP,
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
