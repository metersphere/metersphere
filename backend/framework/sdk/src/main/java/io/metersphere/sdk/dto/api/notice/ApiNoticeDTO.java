package io.metersphere.sdk.dto.api.notice;

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
     * 环境ID
     */
    private String environmentId;
    /**
     * 报告ID
     */
    private String reportId;
    /**
     * 队列ID
     */
    private String queueId;
    /**
     * 是否是集成报告
     */
    private Boolean integratedReport;
}
