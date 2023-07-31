package io.metersphere.system.mapper;

import io.metersphere.system.domain.UserRolePermission;
import io.metersphere.system.domain.UserRolePermissionExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserRolePermissionMapper {
    long countByExample(UserRolePermissionExample example);

    int deleteByExample(UserRolePermissionExample example);

    int deleteByPrimaryKey(String id);

    int insert(UserRolePermission record);

    int insertSelective(UserRolePermission record);

    List<UserRolePermission> selectByExample(UserRolePermissionExample example);

    UserRolePermission selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") UserRolePermission record, @Param("example") UserRolePermissionExample example);

    int updateByExample(@Param("record") UserRolePermission record, @Param("example") UserRolePermissionExample example);

    int updateByPrimaryKeySelective(UserRolePermission record);

    int updateByPrimaryKey(UserRolePermission record);

    int batchInsert(@Param("list") List<UserRolePermission> list);

    int batchInsertSelective(@Param("list") List<UserRolePermission> list, @Param("selective") UserRolePermission.Column ... selective);
}