package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.Role;
import io.metersphere.base.domain.User;
import io.metersphere.controller.request.UserRequest;
import io.metersphere.controller.request.member.QueryMemberRequest;
import io.metersphere.controller.request.organization.QueryOrgMemberRequest;
import io.metersphere.dto.OrganizationMemberDTO;
import io.metersphere.dto.UserRoleHelpDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtUserRoleMapper {

    List<UserRoleHelpDTO> getUserRoleHelpList(@Param("userId") String userId);

    List<User> getMemberList(@Param("member") QueryMemberRequest request);

    List<User> getOrgMemberList(@Param("orgMember") QueryOrgMemberRequest request);

    List<OrganizationMemberDTO> getOrganizationMemberDTO(@Param("orgMember") QueryOrgMemberRequest request);

    List<Role> getOrganizationMemberRoles(@Param("orgId") String orgId, @Param("userId") String userId);

    List<Role> getWorkspaceMemberRoles(@Param("workspaceId") String workspaceId, @Param("userId") String userId);

    List<User> getBesideOrgMemberList(@Param("orgId") String orgId);


    List<User> getTestManagerAndTestUserList(@Param("request") QueryMemberRequest request);

    List<String> selectIdsByQuery(@Param("organizationId") String organizationId, @Param("orgMember")UserRequest condition);
}
