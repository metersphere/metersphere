package io.metersphere.api.dto.debug;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-06  10:22
 */
@Data
public class ApiDebugAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "接口名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_debug.name.not_blank}")
    @Size(min = 1, max = 255, message = "{api_debug.name.length_range}")
    private String name;

    @Schema(description = "接口协议", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_debug.protocol.not_blank}")
    @Size(min = 1, max = 20, message = "{api_debug.protocol.length_range}")
    private String protocol;

    @Schema(description = "http协议类型post/get/其它协议则是协议名(mqtt)")
    @Size(min = 1, max = 20, message = "{api_debug.method.length_range}")
    private String method;

    @Schema(description = "http协议url/其它协议则为空")
    @Size(max = 500, message = "{api_debug.path.length_range}")
    private String path;

    @Schema(description = "项目fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_debug.project_id.not_blank}")
    @Size(min = 1, max = 50, message = "{api_debug.project_id.length_range}")
    private String projectId;

    @Schema(description = "模块fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_debug.module_id.not_blank}")
    @Size(min = 1, max = 50, message = "{api_debug.module_id.length_range}")
    private String moduleId;

    @Schema(description = "请求内容")
    @NotNull
    private Object request;
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
}
