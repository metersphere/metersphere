package io.metersphere.api.dto.scenario;

import io.metersphere.sdk.dto.api.task.ApiRunModeConfigDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class ApiScenarioBatchScheduleConfigRequest extends ApiScenarioBatchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "启用/禁用")
    private boolean enable;

    @Schema(description = "Cron表达式")
    private String cron;

    @Schema(description = "定时任务配置")
    private ApiRunModeConfigDTO config;
}
