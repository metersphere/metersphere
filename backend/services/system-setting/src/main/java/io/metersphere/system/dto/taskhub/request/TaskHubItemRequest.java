package io.metersphere.system.dto.taskhub.request;

import io.metersphere.system.dto.sdk.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author wx
 */
@Data
public class TaskHubItemRequest extends BasePageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "任务id")
    private String taskId;

    @Schema(description = "资源池id")
    private List<String> resourcePoolIds;

    @Schema(description = "资源池节点")
    private List<String> resourcePoolNodes;
}
