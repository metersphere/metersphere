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
    @Schema(description = "报告ID，传了可以实时获取结果，不传则不支持实时获取")
    private String reportId;
    /**
     * 新上传的文件ID
     * 创建时先按ID创建目录，再把文件放入目录
     */
    @Schema(description = "新上传的文件ID")
    private List<String> uploadFileIds;
    /**
     * 新关联的文件ID
     */
    @Schema(description = "关联文件ID")
    private List<String> linkFileIds;
    @NotNull
    @Schema(description = "请求内容")
    private Object request;
    @Schema(description = "项目ID")
    private String projectId;
    @Schema(description = "是否是本地执行")
    private Boolean frontendDebug = false;
}
