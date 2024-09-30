package io.metersphere.system.mapper;

import io.metersphere.system.dto.sdk.BasePageRequest;
import io.metersphere.system.dto.taskhub.TaskHubDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wx
 */
public interface ExtExecTaskMapper {
    List<TaskHubDTO> selectList(@Param("request") BasePageRequest request, @Param("orgId") String orgId, @Param("projectId") String projectId);
}
