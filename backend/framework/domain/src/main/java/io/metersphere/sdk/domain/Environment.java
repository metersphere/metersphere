package io.metersphere.sdk.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class Environment implements Serializable {
    @Schema(title = "Api Test Environment ID", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    @NotBlank(message = "{environment.id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{environment.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "Api Test Environment Name", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 64]")
    @NotBlank(message = "{environment.name.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 64, message = "{environment.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "Project ID", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    @NotBlank(message = "{environment.project_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{environment.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(title = "Api Test Protocol")
    private String protocol;

    @Schema(title = "Api Test Socket")
    private String socket;

    @Schema(title = "Api Test Domain")
    private String domain;

    @Schema(title = "Api Test Port")
    private Integer port;

    @Schema(title = "")
    private String createUser;

    @Schema(title = "")
    private Long createTime;

    @Schema(title = "")
    private Long updateTime;

    @Schema(title = "Global ariables")
    private String variables;

    @Schema(title = "Global Heards")
    private String headers;

    @Schema(title = "Config Data (JSON format)")
    private String config;

    @Schema(title = "hosts")
    private String hosts;

    private static final long serialVersionUID = 1L;
}