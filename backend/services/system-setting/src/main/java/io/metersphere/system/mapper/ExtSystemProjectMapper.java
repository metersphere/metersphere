package io.metersphere.system.mapper;

import io.metersphere.sdk.dto.ProjectDTO;
import io.metersphere.sdk.dto.UserExtend;
import io.metersphere.system.dto.OrganizationProjectOptionsDTO;
import io.metersphere.system.request.ProjectMemberRequest;
import io.metersphere.system.request.ProjectRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtSystemProjectMapper {

    List<UserExtend> getProjectMemberList(@Param("request") ProjectMemberRequest request);

    List<ProjectDTO> getProjectList(@Param("request") ProjectRequest request);

    List<UserExtend> getProjectAdminList(@Param("projectIds") List<String> projectIds);

    List<OrganizationProjectOptionsDTO> selectProjectOptions(@Param("organizationId") String organizationId);

    List<UserExtend> getUserAdminList(@Param("organizationId") String organizationId, @Param("keyword") String keyword);

    List<UserExtend> getUserMemberList(@Param("userIds") List<String> userIds, @Param("projectId") String projectId, @Param("keyword") String keyword);

    List<ProjectDTO> getProjectExtendDTOList(@Param("projectIds") List<String> projectIds);
}
