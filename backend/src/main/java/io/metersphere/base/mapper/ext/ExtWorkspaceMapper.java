package io.metersphere.base.mapper.ext;

import io.metersphere.dto.WorkspaceDTO;

import java.util.List;

public interface ExtWorkspaceMapper {

    List<WorkspaceDTO> getWorkspaceWithOrg();
}
