package io.metersphere.base.mapper.ext;

import io.metersphere.api.dto.ApiProjectRequest;
import io.metersphere.dto.ProjectDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiProjectMapper {
    List<ProjectDTO> getUserProject(@Param("proRequest") ApiProjectRequest request);

}
