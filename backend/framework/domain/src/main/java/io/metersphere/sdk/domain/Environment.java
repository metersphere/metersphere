package io.metersphere.sdk.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class Environment implements Serializable {
    @Schema(title = "环境ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{environment.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{environment.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "环境名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{environment.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{environment.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{environment.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{environment.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(title = "协议")
    private String protocol;

    @Schema(title = "")
    private String socket;

    @Schema(title = "域名/IP")
    private String domain;

    @Schema(title = "端口")
    private Integer port;

    @Schema(title = "创建人")
    private String createUser;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "更新时间")
    private Long updateTime;

    @Schema(title = "全局变量")
    private String variables;

    @Schema(title = "请求头")
    private String headers;

    @Schema(title = "Config Data (JSON format)")
    private String config;

    @Schema(title = "hosts")
    private String hosts;

    private static final long serialVersionUID = 1L;
}