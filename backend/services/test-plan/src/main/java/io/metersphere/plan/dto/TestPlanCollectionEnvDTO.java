package io.metersphere.plan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author wx
 */
@Data
public class TestPlanCollectionEnvDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private String Id;

    @Schema(description = "环境ID")
    private String environmentId;

    @Schema(description = "环境名称")
    private String environmentName;

    @Schema(description = "是否继承")
    private Boolean extended;

    @Schema(description = "父ID")
    private String parentId;
}
