package io.metersphere.api.dto.definition;

import io.metersphere.sdk.constants.ModuleConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * @author lan
 */
@Data
public class ApiDefinitionAddRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(description = "接口名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 1, max = 255, message = "{api_definition.name.length_range}")
    private String name;

    @Schema(description = "接口协议", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.protocol.not_blank}")
    @Size(min = 1, max = 20, message = "{api_definition.protocol.length_range}")
    private String protocol = ModuleConstants.NODE_PROTOCOL_HTTP;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.project_id.not_blank}")
    @Size(min = 1, max = 50, message = "{api_definition.project_id.length_range}")
    private String projectId;

    @Schema(description = "http协议类型post/get/其它协议则是协议名(mqtt)")
    private String method;

    @Schema(description = "http协议路径/其它协议则为空")
    private String path;

    @Schema(description = "接口状态/进行中/已完成", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.status.not_blank}")
    @Size(min = 1, max = 50, message = "{api_definition.status.length_range}")
    private String status;

    @Schema(description = "模块fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.module_id.not_blank}")
    @Size(min = 1, max = 50, message = "{api_definition.module_id.length_range}")
    private String moduleId;

    @Schema(description = "版本fk")
    @Size(min = 1, max = 50, message = "{api_definition.version_id.length_range}")
    private String versionId;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "标签")
    private LinkedHashSet<@NotBlank String> tags;

    @Schema(description = "请求内容")
    @NotNull
    private Object request;

    @Schema(description = "请求内容")
    @NotNull
    private Object response;

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

    @Schema(description = "自定义字段集合")
    private Map<String, String> customFields;

    public void setPath(String path) {
        this.path = StringUtils.trim(path);
    }

    public List<String> getTags() {
        if (tags == null) {
            return new ArrayList<>(0);
        } else {
            return new ArrayList<>(tags);
        }
    }
}
