package io.metersphere.system.mapper;

import io.metersphere.sdk.dto.ProjectDTO;
import io.metersphere.system.domain.User;
import io.metersphere.system.request.ProjectRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtSystemProjectMapper {

    List<User> getProjectMemberList(@Param("request") ProjectRequest request);

    List<ProjectDTO> getProjectList(@Param("request") ProjectRequest request);
}
