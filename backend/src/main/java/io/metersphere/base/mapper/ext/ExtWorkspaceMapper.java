package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.Workspace;
import io.metersphere.controller.request.WorkspaceRequest;
import io.metersphere.dto.WorkspaceDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtWorkspaceMapper {

    List<WorkspaceDTO> getWorkspaces(@Param("request") WorkspaceRequest request);

    List<Workspace> getWorkspaceByUserId(@Param("userId")String userId);

    List<String> getWorkspaceIds();
}
