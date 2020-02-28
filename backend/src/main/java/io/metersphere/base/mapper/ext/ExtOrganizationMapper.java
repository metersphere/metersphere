package io.metersphere.base.mapper.ext;

import org.apache.ibatis.annotations.Param;

public interface ExtOrganizationMapper {

    int checkOrgRole(@Param("orgId") String orgId,@Param("userId") String userId,@Param("roleId") String roleId);
}
