package io.metersphere.api.dto.debug;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;


@Data
public class ApiDebugRunRequest {
    @Schema(description = "接口ID")
    @NotNull
    private String id;
    @Schema(description = "报告ID")
    @NotNull
    private String reportId;
    @Schema(description = "环境ID")
    private String environmentId;
    @Schema(description = "点击调试时尚未保存的文件ID列表")
    private List<String> tempFileIds;
    @NotNull
    @Schema(description = "请求内容")
    private Object request;
    @Schema(description = "项目ID")
    private String projectId;
}
