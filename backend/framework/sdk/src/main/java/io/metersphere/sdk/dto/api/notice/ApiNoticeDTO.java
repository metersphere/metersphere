package io.metersphere.sdk.dto.api.notice;

import io.metersphere.sdk.dto.api.task.ApiRunModeConfigDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiNoticeDTO implements java.io.Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 资源类型
     */
    private String resourceType;
    /**
     * 资源ID
     */
    private String resourceId;
    /**
     * 报告状态
     */
    private String reportStatus;
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 项目ID
     */
    private String projectId;
    /**
     * 报告ID
     */
    private String reportId;
    /**
     * 队列ID
     */
    private String queueId;
    /**
     * 集合ID
     */
    private String setId;
    /**
     * 父队列 ID，即测试集队列 ID
     */
    private String parentQueueId;
    /**
     * 父集合ID，测试集集合ID
     */
    private String parentSetId;
    /**
     * 是否批量执行结束
     * 这里主要给测试计划使用
     * 当测试集执行完成时标记，触发下个测试集执行
     */
    private Boolean childCollectionExecuteOver;
    /**
     * 运行配置
     */
    private ApiRunModeConfigDTO runModeConfig = new ApiRunModeConfigDTO();
    /**
     * 是否是重新执行
     */
    private Boolean rerun = false;
    /**
     * 任务ID
     */
    private String taskId;
    /**
     * 任务项ID
     */
    private String taskItemId;
}
