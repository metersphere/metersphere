package io.metersphere.plan.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author wx
 */
@Data
public class TestPlanBatchEditRequest extends TestPlanBatchProcessRequest {

    @Schema(description = "是否追加")
    private boolean append;

    @Schema(description = "标签")
    private List<String> tags;

}
