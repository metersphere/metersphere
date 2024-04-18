package io.metersphere.project.mapper;

import io.metersphere.project.dto.ProjectUserRoleDTO;
import io.metersphere.project.request.ProjectUserRoleMemberRequest;
import io.metersphere.project.request.ProjectUserRoleRequest;
import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserRoleRelation;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.table.TableBatchProcessDTO;
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
     * @param projectId 项目ID
     * @return 用户组成员关系
     */
    List<UserRoleRelation> getRelationByRoleIds(@Param("projectId") String projectId, @Param("roleIds") List<String> roleIds);

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

    /**
     * 根据关键字获取下拉框当前项目的用户数据
     * @param projectId 项目ID
     * @param keyword 远程搜索时的关键字 （name）
     * @return List<User>
     */
    List<OptionDTO> getProjectUserSelectList(@Param("projectId") String projectId, @Param("keyword") String keyword);

    /**
     * 根据列表参数获取选中的用户ID集合
     * @param request 列表请求参数
     * @return 用户ID集合
     */
    List<String> getProjectRoleMemberIds(@Param("request") TableBatchProcessDTO request);
}
