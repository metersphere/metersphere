package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.EnvironmentGroup;
import io.metersphere.environment.dto.EnvironmentGroupRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BaseEnvironmentGroupMapper {

    List<EnvironmentGroup> getList(@Param("request") EnvironmentGroupRequest request);

    List<EnvironmentGroup> getRelateProject(@Param("wsId") String workspaceId, @Param("pId") String projectId);
}
