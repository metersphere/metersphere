package io.metersphere.system.mapper;

import io.metersphere.system.domain.ExecTask;
import io.metersphere.system.dto.sdk.BasePageRequest;
import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.metersphere.system.dto.taskhub.TaskHubDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wx
 */
public interface ExtExecTaskMapper {
    List<TaskHubDTO> selectList(@Param("request") BasePageRequest request, @Param("orgId") String orgId, @Param("projectId") String projectId);

    void deleteTaskById(@Param("id") String id, @Param("orgId") String orgId, @Param("projectId") String projectId);

    List<String> getIds(@Param("request") TableBatchProcessDTO request);

    void batchUpdateTaskStatus(@Param("ids") List<String> ids, @Param("userId") String userId, @Param("organizationId") String organizationId, @Param("projectId") String projectId, @Param("status") String status);
}
