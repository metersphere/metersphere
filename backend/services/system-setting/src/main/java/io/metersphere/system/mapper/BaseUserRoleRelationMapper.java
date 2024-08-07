package io.metersphere.system.mapper;

import io.metersphere.system.domain.UserRoleRelation;
import io.metersphere.system.dto.sdk.ExcludeOptionDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BaseUserRoleRelationMapper {
    List<UserRoleRelation> getUserIdAndSourceIdByUserIds(@Param("userIds") List<String> userIds);

    List<String> getUserIdByRoleId(@Param("roleId") String roleId);

    List<ExcludeOptionDTO> getSelectOption(@Param("roleId") String roleId);
}
