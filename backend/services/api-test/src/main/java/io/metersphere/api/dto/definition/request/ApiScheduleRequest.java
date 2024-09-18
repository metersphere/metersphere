package io.metersphere.api.dto.definition.request;


import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ApiScheduleRequest {

    @Schema(description = "id")
    @NotBlank(message = "{api_definition_swagger.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_definition_swagger.id.length_range}", groups = {Updated.class})
    private String id;
    @Schema(description = "项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_swagger.project_id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{api_definition_swagger.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;
    @Schema(description = "定时任务名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_swagger.name.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 255, message = "{api_definition_swagger.name.length_range}", groups = {Created.class, Updated.class})
    private String name;
    @Schema(description = "模块ID")
    @Size(max = 50, message = "{api_definition_swagger.module_id.length_range}", groups = {Created.class, Updated.class})
    private String moduleId;
    @Schema(description = "swagger地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_swagger.swagger_url.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 500, message = "{api_definition_swagger.swagger_url.length_range}", groups = {Created.class, Updated.class})
    private String swaggerUrl;
    private String swaggerToken;
    private String taskId;
    @Schema(description = "是否覆盖模块")
    private Boolean coverModule = false;
    @Schema(description = "是否同步导入用例")
    private Boolean syncCase = false;
    @Schema(description = "是否覆盖数据")
    private Boolean coverData = false;
    @Schema(description = "协议")
    private String protocol = ModuleConstants.NODE_PROTOCOL_HTTP;
    @Schema(description = "是否开启Basic Auth认证")
    private boolean authSwitch = false;
    @Schema(description = "Basic Auth认证用户名")
    private String authUsername;
    @Schema(description = "Basic Auth认证密码")
    private String authPassword;
    @Schema(description = "用户id")
    private String userId;
    @Schema(description = "是否开启")
    private Boolean enable = true;
    @Schema(description = "cron 表达式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{schedule.value.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{schedule.value.length_range}", groups = {Created.class, Updated.class})
    private String value;
    @Schema(description = "配置信息")
    private String config;
}
