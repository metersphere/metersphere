package io.metersphere.api.dto.debug;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-06  10:22
 */
@Data
public class ApiDebugUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "接口pk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_debug.id.not_blank}")
    @Size(max = 50, message = "{api_debug.id.length_range}")
    private String id;

    @Schema(description = "接口名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 1, max = 255, message = "{api_debug.name.length_range}")
    private String name;

    @Schema(description = "http协议类型post/get/其它协议则是协议名(mqtt)")
    @Size(min = 1, max = 20, message = "{api_debug.method.length_range}")
    private String method;

    @Schema(description = "http协议路径/其它协议则为空")
    @Size(max = 500, message = "{api_debug.path.length_range}")
    private String path;

    @Schema(description = "模块fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 1, max = 50, message = "{api_debug.module_id.length_range}")
    private String moduleId;

    @Schema(description = "请求内容")
    private Object request;

    /**
     * 新上传的文件ID
     * 创建时先按ID创建目录，再把文件放入目录
     */
    @Schema(description = "新上传的文件ID")
    private List<String> uploadFileIds;

    /**
     * 新关联文件管理的文件ID
     */
    @Schema(description = "关联文件ID")
    private List<String> linkFileIds;

    /**
     * 删除本地上传的文件ID
     */
    @Schema(description = "删除的文件ID")
    private List<String> deleteFileIds;

    /**
     * 删除关联的文件ID
     */
    @Schema(description = "取消关联文件ID")
    private List<String> unLinkFileIds;
}
