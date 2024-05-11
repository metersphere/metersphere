package io.metersphere.plan.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author wx
 */
@Data
public class BaseAssociateCaseRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "功能用例选中的ids")
    private List<String> functionalSelectIds;

    @Schema(description = "接口API用例选中的ids")
    private List<String> apiSelectIds;

    @Schema(description = "接口CASE选中的ids")
    private List<String> apiCaseSelectIds;

    @Schema(description = "接口场景用例选中的ids")
    private List<String> apiScenarioSelectIds;
}
