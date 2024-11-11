package io.metersphere.project.mapper;

import io.metersphere.project.request.ProjectMemberRequest;
import io.metersphere.system.dto.CommentUserInfo;
import io.metersphere.system.dto.user.ProjectUserMemberDTO;
import io.metersphere.system.dto.user.UserExtendDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author song-cc-rock
 */
public interface ExtProjectMemberMapper {

    /**
     * 获取项目成员列表
     *
     * @param request 请求参数
     * @return 成员列表及用户组关联信息
     */
    List<String> listMember(@Param("request") ProjectMemberRequest request);

    /**
     * 获取所有组织成员
     * @param organizationId 组织ID
     * @param keyword 搜索关键字
     * @return 成员
     */
    List<UserExtendDTO> getMemberByOrg(@Param("organizationId") String organizationId, @Param("keyword") String keyword);

    /**
     * 获取项目评论下拉成员选项
     * @param projectId 项目ID
     * @param keyword 搜索关键字
     * @return 用户集合信息
     */
    List<CommentUserInfo> getCommentAtUserInfoByParam(@Param("projectId") String projectId, @Param("keyword") String keyword);

    /**
     * 获取组织下所有有项目权限的成员
     * @param organizationId 组织ID
     * @param userIds 用户过滤
     * @return  List<ProjectUserMemberDTO>
     */
    List<ProjectUserMemberDTO> getOrgProjectMemberList(@Param("organizationId") String organizationId, @Param("userIds") List<String>userIds );

    /**
     * 获取项目下所有有项目权限的成员
     * @param projectId 项目ID
     * @param userIds 用户过滤
     * @return  List<ProjectUserMemberDTO>
     */
    List<ProjectUserMemberDTO> getProjectMemberList(@Param("projectId") String projectId, @Param("userIds") List<String>userIds );
}
