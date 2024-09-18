package io.metersphere.api.dto.request;

import io.metersphere.sdk.constants.ModuleConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ImportRequest {
    private String id;
    private String name;
    @Schema(description = "导入的模块id")
    private String moduleId;
    @Schema(description = "导入的项目id")
    private String projectId;
    @Schema(description = "导入的swagger地址")
    private String swaggerUrl;
    @Schema(description = "导入的swagger token")
    private String swaggerToken;
    @Schema(description = "如果是定时任务的时候 需要传入创建人id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userId;
    private String versionId; // 新导入选择的版本
    private String updateVersionId; // 覆盖导入已存在的接口选择的版本
    private String defaultVersion;
    @Schema(description = "三方平台  暂定 Metersphere  Swagger3  Postman Har")
    private String platform;
    @Schema(description = "导入的类型  暂定  API  Schedule")
    private String type;

    @Schema(description = "协议")
    private String protocol = ModuleConstants.NODE_PROTOCOL_HTTP;
    @Schema(description = "Basic Auth认证用户名")
    private String authUsername;
    @Schema(description = "Basic Auth认证密码")
    private String authPassword;
    @Schema(description = "唯一标识  默认是Method & Path  后续估计会补充")
    private String uniquelyIdentifies = "Method & Path";
    @Schema(description = "定时任务的资源id")
    private String resourceId;

    @Schema(description = "是否覆盖模块")
    private boolean coverModule;
    @Schema(description = "是否同步导入用例")
    private boolean syncCase;
    @Schema(description = "是否同步导入Mock")
    private boolean syncMock;
    @Schema(description = "是否覆盖数据")
    private boolean coverData;
    @Schema(description = "是否开启Basic Auth认证")
    private boolean authSwitch;
    
    @Schema(description = "操作人")
    private String operator;
}
