package io.metersphere.base.mapper.ext;

import io.metersphere.dto.EnvironmentGroupProjectDTO;

import java.util.List;

public interface ExtEnvGroupProjectMapper {

    List<EnvironmentGroupProjectDTO> getList(String groupId);
}
