package io.metersphere.api.dto.debug;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ApiResourceRunRequest {
    private String id;
    private String projectId;
    private String testId;
    private String reportId;
    private String environmentId;
    /**
     * 执行模式
     */
    private String runMode;
    /**
     * 资源类型
     */
    private String resourceType;
    @Schema(description = "点击调试时尚未保存的文件ID列表")
    private List<String> tempFileIds;
    private String request;
}
