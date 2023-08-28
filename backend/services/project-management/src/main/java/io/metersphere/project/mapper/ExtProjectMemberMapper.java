package io.metersphere.project.mapper;

import io.metersphere.project.request.ProjectMemberRequest;
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
}
