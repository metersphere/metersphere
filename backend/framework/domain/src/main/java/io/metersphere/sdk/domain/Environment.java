package io.metersphere.sdk.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class Environment implements Serializable {
    @Schema(description =  "环境ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{environment.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{environment.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "环境名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{environment.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{environment.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description =  "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{environment.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{environment.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description =  "协议")
    private String protocol;

    @Schema(description =  "")
    private String socket;

    @Schema(description =  "域名/IP")
    private String domain;

    @Schema(description =  "端口")
    private Integer port;

    @Schema(description =  "创建人")
    private String createUser;

    @Schema(description =  "创建时间")
    private Long createTime;

    @Schema(description =  "更新时间")
    private Long updateTime;

    @Schema(description =  "全局变量")
    private String variables;

    @Schema(description =  "请求头")
    private String headers;

    @Schema(description =  "Config Data (JSON format)")
    private String config;

    @Schema(description =  "hosts")
    private String hosts;

    private static final long serialVersionUID = 1L;
}