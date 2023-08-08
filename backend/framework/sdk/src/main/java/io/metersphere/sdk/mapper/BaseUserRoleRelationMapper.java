package io.metersphere.sdk.mapper;

import io.metersphere.system.domain.UserRoleRelation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BaseUserRoleRelationMapper {
    List<UserRoleRelation> getUserIdAndSourceIdByUserIds(@Param("userIds") List<String> userIds);

    List<String> getUserIdRoleId(@Param("roleId") String roleId);
}
