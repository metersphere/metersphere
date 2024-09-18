package io.metersphere.api.dto.definition;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ApiScheduleDTO {

    @Schema(description = "id")
    private String id;
    @Schema(description = "项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String projectId;
    @Schema(description = "定时任务名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
    @Schema(description = "模块ID")
    private String moduleId;
    @Schema(description = "swagger地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private String swaggerUrl;
    @Schema(description = "swagger token", requiredMode = Schema.RequiredMode.REQUIRED)
    private String swaggerToken;
    @Schema(description = "是否覆盖模块")
    private Boolean coverModule = false;
    @Schema(description = "是否同步导入用例")
    private Boolean syncCase = false;
    @Schema(description = "是否覆盖数据")
    private Boolean coverData = false;
    @Schema(description = "是否开启Basic Auth认证")
    private boolean authSwitch = false;
    @Schema(description = "Basic Auth认证用户名")
    private String authUsername;
    @Schema(description = "Basic Auth认证密码")
    private String authPassword;
    @Schema(description = "是否开启")
    private Boolean enable = true;
    @Schema(description = "cron 表达式", requiredMode = Schema.RequiredMode.REQUIRED)
    private String value;
}
