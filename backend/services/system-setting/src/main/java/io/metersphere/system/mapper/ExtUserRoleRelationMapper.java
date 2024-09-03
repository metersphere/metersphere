package io.metersphere.system.mapper;

import io.metersphere.system.domain.UserRoleRelation;
import io.metersphere.system.dto.request.GlobalUserRoleRelationQueryRequest;
import io.metersphere.system.dto.user.UserRoleOptionDto;
import io.metersphere.system.dto.user.UserRoleRelationUserDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtUserRoleRelationMapper {
    List<UserRoleRelation> selectGlobalRoleByUserIdList(@Param("userIds") List<String> userIdList);

    List<UserRoleRelation> selectRoleByUserIdList(@Param("userIds") List<String> userIdList);

    List<UserRoleRelation> selectGlobalRoleByUserId(String userId);

    List<UserRoleRelationUserDTO> listGlobal(@Param("request") GlobalUserRoleRelationQueryRequest request);

    List<UserRoleOptionDto> selectUserRoleByUserIds(@Param("userIds") List<String> userIds, @Param("orgId") String orgId);

    List<UserRoleOptionDto> selectProjectUserRoleByUserIds(@Param("userIds") List<String> userIds, @Param("projectId") String projectId);
}
