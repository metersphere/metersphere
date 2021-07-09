package io.metersphere.base.mapper;

import io.metersphere.base.domain.UserGroupPermission;
import io.metersphere.base.domain.UserGroupPermissionExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserGroupPermissionMapper {
    long countByExample(UserGroupPermissionExample example);

    int deleteByExample(UserGroupPermissionExample example);

    int deleteByPrimaryKey(String id);

    int insert(UserGroupPermission record);

    int insertSelective(UserGroupPermission record);

    List<UserGroupPermission> selectByExample(UserGroupPermissionExample example);

    UserGroupPermission selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") UserGroupPermission record, @Param("example") UserGroupPermissionExample example);

    int updateByExample(@Param("record") UserGroupPermission record, @Param("example") UserGroupPermissionExample example);

    int updateByPrimaryKeySelective(UserGroupPermission record);

    int updateByPrimaryKey(UserGroupPermission record);
}