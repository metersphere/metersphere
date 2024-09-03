package io.metersphere.system.mapper;

import io.metersphere.system.domain.User;
import io.metersphere.system.dto.OrganizationProjectOptionsDTO;
import io.metersphere.system.dto.ProjectDTO;
import io.metersphere.system.dto.ProjectResourcePoolDTO;
import io.metersphere.system.dto.request.ProjectMemberRequest;
import io.metersphere.system.dto.request.ProjectRequest;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.user.UserExtendDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtSystemProjectMapper {

    List<UserExtendDTO> getProjectMemberList(@Param("request") ProjectMemberRequest request);

    List<ProjectDTO> getProjectList(@Param("request") ProjectRequest request);

    List<UserExtendDTO> getProjectAdminList(@Param("projectIds") List<String> projectIds);

    List<OrganizationProjectOptionsDTO> selectProjectOptions(@Param("organizationId") String organizationId);

    List<UserExtendDTO> getUserAdminList(@Param("organizationId") String organizationId, @Param("keyword") String keyword);

    List<UserExtendDTO> getUserMemberList(@Param("userIds") List<String> userIds, @Param("projectId") String projectId, @Param("keyword") String keyword);

    List<ProjectDTO> getProjectExtendDTOList(@Param("projectIds") List<String> projectIds);

    List<ProjectResourcePoolDTO> getProjectResourcePoolDTOList(@Param("projectIds") List<String> projectIds);

    String selectModuleSettingsByResourceIdAndTable(@Param("resourceId") String resourceId, @Param("resourceTable") String resourceTable);

    List<UserExtendDTO> getMemberByProjectId(@Param("projectId") String projectId, @Param("keyword") String keyword);

    List<User> getProjectMemberByUserId(@Param("projectId") String projectId, @Param("userIds") List<String> userIds);

    List<User> getEnableProjectMemberByUserId(@Param("projectId") String projectId, @Param("userIds") List<String> userIds);


    List<OptionDTO> getSystemProject(@Param("keyword") String keyword);

    List<UserExtendDTO> getUserList(@Param("userIds") List<String> userIds, @Param("projectId") String projectId, @Param("keyword") String keyword);
}
