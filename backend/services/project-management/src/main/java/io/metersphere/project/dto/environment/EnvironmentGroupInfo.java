package io.metersphere.project.dto.environment;

import io.metersphere.project.dto.environment.http.HttpConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class EnvironmentGroupInfo extends EnvironmentGroupProjectDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description = "描述")
    private String description;
    @Schema(description = "域名")
    private List<HttpConfig> domain;
    @Schema(description = "环境名称")
    private String envName;

}