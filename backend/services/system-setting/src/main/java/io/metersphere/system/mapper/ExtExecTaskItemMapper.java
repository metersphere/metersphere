package io.metersphere.system.mapper;

import io.metersphere.system.domain.ExecTaskItem;
import io.metersphere.system.dto.taskhub.TaskHubItemDTO;
import io.metersphere.system.dto.taskhub.request.TaskHubItemRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wx
 */
public interface ExtExecTaskItemMapper {
    List<TaskHubItemDTO> selectList(@Param("request") TaskHubItemRequest request, @Param("orgId") String orgId, @Param("projectId") String projectId);

    List<ExecTaskItem> selectItemByTaskIds(@Param("taskIds") List<String> taskIds, @Param("orgId") String orgId, @Param("projectId") String projectId);

    List<ExecTaskItem> selectPoolNodeByIds(@Param("ids") List<String> ids);

    List<ExecTaskItem> selectExecInfoByResourceIds(@Param("resourceIds") List<String> resourceIds);

    List<ExecTaskItem> getResourcePoolsByTaskIds(@Param("taskIds") List<String> taskIds);

    void batchUpdateTaskItemStatus(@Param("taskIds") List<String> taskIds, @Param("userId") String userId, @Param("organizationId") String organizationId, @Param("projectId") String projectId, @Param("status") String status);
}
