package io.metersphere.system.mapper;

import io.metersphere.system.domain.UserRoleRelation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtUserRoleRelationMapper {
    List<UserRoleRelation> listByUserIdAndScope(@Param("userIds") List<String> userIdList);
}
