package io.metersphere.plan.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class TestPlanScheduleBatchConfigRequest extends TestPlanBatchProcessRequest {

    @Schema(description = "启用/禁用")
    private boolean enable;

    @Schema(description = "Cron表达式")
    @Size(max = 255, message = "{length.too.large}")
    private String cron;

    @Schema(description = "运行配置")
    private Map<String, String> runConfig = new HashMap<>();

}
