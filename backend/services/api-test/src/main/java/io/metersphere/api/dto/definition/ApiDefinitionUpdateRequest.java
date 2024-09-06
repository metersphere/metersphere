package io.metersphere.api.dto.definition;

import io.metersphere.api.constants.ApiDefinitionStatus;
import io.metersphere.api.domain.ApiDefinitionCustomField;
import io.metersphere.sdk.valid.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * @author lan
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ApiDefinitionUpdateRequest {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "接口pk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.id.not_blank}")
    @Size(min = 1, max = 50, message = "{api_definition.id.length_range}")
    private String id;


    @Schema(description = "接口名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 1, max = 255, message = "{api_definition.name.length_range}")
    private String name;

    @Schema(description = "http协议类型post/get/其它协议则是协议名(mqtt)")
    @Size(max = 20, message = "{api_debug.method.length_range}")
    private String method;

    @Schema(description = "http协议路径/其它协议则为空")
    @Size(max = 500, message = "{api_debug.path.length_range}")
    private String path;

    @Schema(description = "接口状态/进行中/已完成", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 1, max = 50, message = "{api_definition.status.length_range}")
    @EnumValue(enumClass = ApiDefinitionStatus.class)
    private String status;

    @Schema(description = "模块fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 1, max = 50, message = "{api_definition.module_id.length_range}")
    private String moduleId;

    @Schema(description = "描述")
    @Size(max = 1000, message = "{api_definition.description.length_range}")
    private String description;

    @Schema(description = "标签")
    private LinkedHashSet<
            @NotBlank
            @Size(min = 1, max = 64, message = "{api_test_case.tag.length_range}")
                    String> tags;

    @Schema(description = "请求内容")
    private Object request;

    @Schema(description = "响应内容")
    @Valid
    private List<HttpResponse> response;

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
    private List<ApiDefinitionCustomField> customFields;

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
