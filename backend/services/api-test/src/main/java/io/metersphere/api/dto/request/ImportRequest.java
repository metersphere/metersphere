package io.metersphere.api.dto.request;

import io.metersphere.api.dto.request.http.Header;
import io.metersphere.api.dto.request.http.QueryParam;
import io.metersphere.api.dto.request.http.auth.HTTPAuth;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ImportRequest {
    private String id;
    private String name;
    private String moduleId;
    private String projectId;
    @Schema(description = "导入的swagger地址")
    private String swaggerUrl;
    @Schema(description = "如果是定时任务的时候 需要传入创建人id")
    private String userId;
    private String versionId; // 新导入选择的版本
    private String updateVersionId; // 覆盖导入已存在的接口选择的版本
    private String defaultVersion;
    private String platform;
    //调用类型
    private String type;
    @Schema(description = "是否覆盖模块")
    private Boolean coverModule;
    @Schema(description = "是否同步导入用例")
    private Boolean syncCase;
    @Schema(description = "是否覆盖数据")
    private Boolean coverData;
    // 当前协议
    private String protocol;
    //上传文件来源，目前用于辨别是否是idea插件
    private String origin;
    @Schema(description = "swagger的请求头参数")
    private List<Header> headers;
    @Schema(description = "swagger的请求参数")
    private List<QueryParam> arguments;
    @Schema(description = "swagger的认证参数")
    private HTTPAuth authManager;
    @Schema(description = "唯一标识  默认是Method & Path  后续估计会补充")
    private String uniquelyIdentifies = "Method & Path";
}
