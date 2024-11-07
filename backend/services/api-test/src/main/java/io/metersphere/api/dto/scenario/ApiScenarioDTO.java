package io.metersphere.api.dto.scenario;

import io.metersphere.api.domain.ApiScenario;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ApiScenarioDTO extends ApiScenario {
    @Schema(description = "项目名称")
    private String projectName;
    @Schema(description = "创建人名称")
    private String createUserName;
    @Schema(description = "更新人名称")
    private String updateUserName;
    @Schema(description = "删除人名称")
    private String deleteUserName;
    @Schema(description = "版本名称")
    private String versionName;
    @Schema(description = "所属模块")
    private String modulePath;
    @Schema(description = "环境名称")
    private String environmentName;
    @Schema(description = "定时任务配置")
    private ApiScenarioScheduleConfigRequest scheduleConfig;
    @Schema(description = "定时任务下一次执行时间")
    private Long nextTriggerTime;
    @Schema(description = "脚本错误标识")
    private String scriptIdentifier;
    @Schema(description = "执行通过率", requiredMode = Schema.RequiredMode.REQUIRED)
    private String execPassRate = "NONE";
}
