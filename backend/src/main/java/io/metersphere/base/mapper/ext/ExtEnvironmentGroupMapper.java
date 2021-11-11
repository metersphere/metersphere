package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.EnvironmentGroup;
import io.metersphere.controller.request.EnvironmentGroupRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtEnvironmentGroupMapper {

    List<EnvironmentGroup> getList(@Param("request") EnvironmentGroupRequest request);

    List<EnvironmentGroup> getRelateProject(@Param("wsId") String workspaceId, @Param("pId") String projectId);
}
