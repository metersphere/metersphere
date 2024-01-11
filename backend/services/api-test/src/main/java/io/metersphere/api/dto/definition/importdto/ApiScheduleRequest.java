package io.metersphere.api.dto.definition.importdto;


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
    @NotBlank(message = "id不能为空", groups = {Updated.class})
    private String id;
    @Schema(description = "项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "项目id不能为空", groups = {Created.class, Updated.class})
    private String projectId;
    @Schema(description = "定时任务名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "定时任务名称不能为空", groups = {Created.class, Updated.class})
    private String name;
    @Schema(description = "模块ID")
    private String moduleId;
    @Schema(description = "swagger地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "swagger地址不能为空", groups = {Created.class, Updated.class})
    private String swaggerUrl;
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
