package io.metersphere.api.dto.debug;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
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
    @NotBlank(message = "{api_debug.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_debug.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "接口名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_debug.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{api_debug.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description = "http协议类型post/get/其它协议则是协议名(mqtt)")
    private String method;

    @Schema(description = "http协议路径/其它协议则为空")
    private String path;

    @Schema(description = "模块fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_debug.module_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_debug.module_id.length_range}", groups = {Created.class, Updated.class})
    private String moduleId;

    @Schema(description = "请求内容")
    @NotBlank
    private String request;

    @Schema(description = "接口所需的所有文件资源ID")
    private List<String> fileIds;

    /**
     * 新上传的文件ID
     * 为了解决文件名称重复的问题，需要把文件和ID一一对应
     * 创建时先按ID创建目录，再把文件放入目录
     */
    @Schema(description = "新上传的文件ID，与上传的文件顺序保持一致")
    private List<String> addFileIds;
}
