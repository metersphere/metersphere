package io.metersphere.system.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtUserRoleMapper {
    List<String> selectGlobalRoleList(@Param("roleIdList") List<String> roleIdList, @Param("isSystem") boolean isSystem);
}
