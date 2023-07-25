package io.metersphere.system.service;

import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.dto.LogDTO;
import io.metersphere.sdk.dto.request.GlobalUserRoleRelationUpdateRequest;
import io.metersphere.sdk.log.constants.OperationLogModule;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.service.BaseUserRoleRelationService;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.UserRoleRelation;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * @author jianxing
 * @date : 2023-6-12
 */
@Service
public class GlobalUserRoleRelationLogService extends BaseUserRoleRelationService {

    @Resource
    private UserRoleRelationMapper userRoleRelationMapper;

    private static final String PRE_URI = "/user/role/relation/global";

    /**
     * 添加接口日志
     *
     * @param request
     * @return
     */
    public LogDTO addLog(GlobalUserRoleRelationUpdateRequest request) {
        LogDTO dto = new LogDTO(
                "system",
                "",
                null,
                null,
                OperationLogType.ADD.name(),
                OperationLogModule.SYSTEM_USER_ROLE_RELATION,
                request.getUserId());

        dto.setPath(PRE_URI + "/add");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(request));
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
        if (userRoleRelation != null) {
            LogDTO dto = new LogDTO(
                    "system",
                    "",
                    id,
                    userRoleRelation.getCreateUser(),
                    OperationLogType.DELETE.name(),
                    OperationLogModule.SYSTEM_USER_ROLE_RELATION,
                    userRoleRelation.getUserId());

            dto.setPath("/delete");
            dto.setMethod(HttpMethodConstants.POST.name());

            dto.setOriginalValue(JSON.toJSONBytes(userRoleRelation));
            return dto;
        }
        return null;
    }
}
