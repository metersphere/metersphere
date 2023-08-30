package io.metersphere.system.mapper;

import io.metersphere.sdk.dto.ProjectDTO;
import io.metersphere.system.domain.User;
import io.metersphere.system.dto.OrganizationProjectOptionsDTO;
import io.metersphere.system.dto.UserExtend;
import io.metersphere.system.request.ProjectMemberRequest;
import io.metersphere.system.request.ProjectRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtSystemProjectMapper {

    List<UserExtend> getProjectMemberList(@Param("request") ProjectMemberRequest request);

    List<ProjectDTO> getProjectList(@Param("request") ProjectRequest request);

    List<User> getProjectAdminList(String projectId);

    List<OrganizationProjectOptionsDTO> selectProjectOptions(@Param("organizationId") String organizationId);

    List<UserExtend> getUserAdminList(@Param("organizationId") String organizationId);

    List<UserExtend> getUserMemberList(@Param("userIds") List<String> userIds, @Param("projectId") String projectId);
}
