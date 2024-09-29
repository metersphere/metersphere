package io.metersphere.plan.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author wx
 */
@Data
public class TestPlanCaseMinderRequest extends BasePlanCaseBatchRequest {

    @Schema(description = "脑图选中的模块id集合")
    private List<String> minderModuleIds;

    @Schema(description = "脑图选中的用例id集合")
    private List<String> minderCaseIds;

    @Schema(description = "脑图选中的项目id集合")
    private List<String> minderProjectIds;

    @Schema(description = "脑图选中的测试集id集合")
    private List<String> minderCollectionIds;
}
