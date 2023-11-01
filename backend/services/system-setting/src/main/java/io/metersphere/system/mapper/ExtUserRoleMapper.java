package io.metersphere.system.mapper;

import io.metersphere.system.domain.User;
import io.metersphere.system.dto.request.OrganizationUserRoleMemberRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtUserRoleMapper {
    List<String> selectGlobalRoleList(@Param("roleIdList") List<String> roleIdList, @Param("isSystem") boolean isSystem);

    List<User> listOrganizationRoleMember(@Param("request") OrganizationUserRoleMemberRequest request);
}
