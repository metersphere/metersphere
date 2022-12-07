package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.Project;
import io.metersphere.base.domain.Workspace;
import io.metersphere.dto.ProjectDTO;
import io.metersphere.request.ProjectRequest;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ExtProjectMapper {
    List<String> getProjectIdByWorkspaceId(String workspaceId);
}
