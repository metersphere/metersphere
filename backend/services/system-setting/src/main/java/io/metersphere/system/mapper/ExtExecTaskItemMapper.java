package io.metersphere.system.mapper;

import io.metersphere.system.domain.ExecTaskItem;
import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.metersphere.system.dto.BatchExecTaskReportDTO;
import io.metersphere.system.dto.request.BatchExecTaskPageRequest;
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

    /**
     * 查询时间范围内的任务项ID集合
     *
     * @param timeMills 时间戳
     * @param projectId 项目ID
     * @return 任务项ID列表
     */
    List<String> getTaskItemIdsByTime(@Param("timeMills") long timeMills, @Param("projectId") String projectId);

    List<ExecTaskItem> selectExecInfoByTaskIdAndResourceIds(@Param("taskId") String taskId, @Param("resourceIds") List<String> resourceIds);

    Boolean hasErrorItem(@Param("taskId") String taskId);

    Boolean hasFakeErrorItem(@Param("taskId") String taskId);

    List<String> getItemIdByTaskIds(@Param("taskIds") List<String> taskIds);

    void batchUpdateTaskItemStatusByIds(@Param("ids") List<String> ids, @Param("userId") String userId, @Param("organizationId") String organizationId, @Param("projectId") String projectId, @Param("status") String status);

    List<ExecTaskItem> getResourcePoolsByItemIds(@Param("ids") List<String> ids);

    List<String> getIds(@Param("request") TableBatchProcessDTO request, @Param("organizationId") String organizationId, @Param("projectId") String projectId);

    /**
     * 查询批量执行任务报告
     * @param request 请求参数
     * @param tableName 表名
     * @return 执行任务报告集合
     */
    List<BatchExecTaskReportDTO> list(@Param("request") BatchExecTaskPageRequest request, @Param("tableName") String tableName);
}
