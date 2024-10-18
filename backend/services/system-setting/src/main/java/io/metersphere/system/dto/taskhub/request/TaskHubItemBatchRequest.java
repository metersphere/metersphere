package io.metersphere.system.dto.taskhub.request;

import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author wx
 */
@Data
public class TaskHubItemBatchRequest extends TableBatchProcessDTO {

    @Schema(description = "任务id")
    private String taskId;

    @Schema(description = "资源池id")
    private List<String> resourcePoolIds;

    @Schema(description = "资源池节点")
    private List<String> resourcePoolNodes;

}
