package io.metersphere.project.mapper;

import io.metersphere.project.request.ProjectUserRoleMemberRequest;
import io.metersphere.system.domain.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author song-cc-rock
 */
public interface ExtProjectUserRoleMapper {

    /**
     * 获取项目成员列表
     * @param request 请求参数
     * @return 项目成员列表
     */
    List<User> listProjectRoleMember(@Param("request") ProjectUserRoleMemberRequest request);
}
