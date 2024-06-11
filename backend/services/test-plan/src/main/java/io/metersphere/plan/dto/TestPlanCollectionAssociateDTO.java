package io.metersphere.plan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author guoyuqi
 */
@Data
public class TestPlanCollectionAssociateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "关联关系的ids", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> ids;

    @Schema(description = "关联关系的type(功能：FUNCTIONAL_CASE/接口定义：API/接口用例：API_CASE/场景：SCENARIO_CASE)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String associateType;


}
