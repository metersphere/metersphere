package io.metersphere.system.dto.taskcenter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author: LAN
 * @date: 2024/1/17 11:20
 * @version: 1.0
 */
@Data
public class TaskCenterDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "所属组织")
    private String organizationName;

    @Schema(description = "所属项目")
    private String projectName;

    @Schema(description = "项目id")
    private String projectId;
    @Schema(description = "组织id")
    private String organizationId;

    @Schema(description = "报告id")
    private String id;

    @Schema(description = "资源Id 单独报告显示模块业务id 集合报告显示报告id")
    private String resourceId;

    @Schema(description = "资源编号 单独报告显示模块编号 集合报告显示报告编号")
    private String resourceNum;

    @Schema(description = "资源名称 单独报告显示模块名称 集合报告显示报告名称")
    private String resourceName;

    @Schema(description = "触发模式（手动，定时，批量，测试计划）")
    private String triggerMode;

    @Schema(description = "资源池名称")
    private String poolName;

    @Schema(description = "执行结果/SUCCESS/ERROR")
    private String status;

    @Schema(description = "执行状态/SUCCESS/ERROR")
    private String execStatus;

    @Schema(description = "脚本标识")
    private String scriptIdentifier;

    @Schema(description = "操作人")
    private String operationName;

    @Schema(description = "操作时间")
    private Long operationTime;

    @Schema(description = "是否为集合报告")
    private boolean integrated;

    @Schema(description = "执行历史是否被清理")
    private boolean historyDeleted = false;

    @Schema(description = "计划组ID")
    private String parent;

    @Schema(description = "计划组子任务")
    private List<TaskCenterDTO> children;
}
