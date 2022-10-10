package io.metersphere.base.mapper.ext;

import io.metersphere.environment.dto.EnvironmentGroupProjectDTO;

import java.util.List;

public interface BaseEnvGroupProjectMapper {

    List<EnvironmentGroupProjectDTO> getList(String groupId);
}
