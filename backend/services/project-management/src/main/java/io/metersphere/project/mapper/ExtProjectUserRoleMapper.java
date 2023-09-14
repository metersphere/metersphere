package io.metersphere.project.mapper;

import io.metersphere.project.dto.ProjectUserRoleDTO;
import io.metersphere.project.request.ProjectUserRoleMemberRequest;
import io.metersphere.project.request.ProjectUserRoleRequest;
import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserRoleRelation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author song-cc-rock
 */
public interface ExtProjectUserRoleMapper {

    /**
     * 获取项目用户组列表
     *
     * @param request 请求参数
     * @return 项目用户组列表
     */
    List<ProjectUserRoleDTO> list(@Param("request") ProjectUserRoleRequest request);

    /**
     * 根据用户组ID获取用户组成员关系
     *
     * @param roleIds 用户组ID集合
     * @return 用户组成员关系
     */
    List<UserRoleRelation> getRelationByRoleIds(@Param("roleIds") List<String> roleIds);

    /**
     * 获取项目成员列表
     *
     * @param request 请求参数
     * @return 项目成员列表
     */
    List<User> listProjectRoleMember(@Param("request") ProjectUserRoleMemberRequest request);

    /**
     * 根据项目id获取 项目成员
     *
     * @param sourceId
     * @return
     */
    List<User> getProjectUserList(@Param("sourceId") String sourceId);
}
