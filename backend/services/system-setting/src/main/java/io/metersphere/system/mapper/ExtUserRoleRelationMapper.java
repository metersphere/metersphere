package io.metersphere.system.mapper;

import io.metersphere.sdk.dto.UserRoleRelationUserDTO;
import io.metersphere.system.domain.UserRoleRelation;
import io.metersphere.system.dto.request.GlobalUserRoleRelationQueryRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtUserRoleRelationMapper {
    List<UserRoleRelation> selectGlobalRoleByUserIdList(@Param("userIds") List<String> userIdList);

    List<UserRoleRelation> selectGlobalRoleByUserId(String userId);

    List<UserRoleRelationUserDTO> listGlobal(@Param("request") GlobalUserRoleRelationQueryRequest request);
}
