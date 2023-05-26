package io.metersphere.api.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class ApiSyncConfig implements Serializable {
    @Schema(title = "", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_sync_config.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_sync_config.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "API/CASE 来源fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_sync_config.resource_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_sync_config.resource_id.length_range}", groups = {Created.class, Updated.class})
    private String resourceId;

    @Schema(title = "来源类型/API/CASE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_sync_config.resource_type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_sync_config.resource_type.length_range}", groups = {Created.class, Updated.class})
    private String resourceType;

    @Schema(title = "是否隐藏")
    private Boolean hide;

    @Schema(title = "是否通知用例创建人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_sync_config.notify_case_creator.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 1, message = "{api_sync_config.notify_case_creator.length_range}", groups = {Created.class, Updated.class})
    private Boolean notifyCaseCreator;

    @Schema(title = "是否通知场景创建人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_sync_config.notify_scenario_creator.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 1, message = "{api_sync_config.notify_scenario_creator.length_range}", groups = {Created.class, Updated.class})
    private Boolean notifyScenarioCreator;

    @Schema(title = "是否同步用例", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_sync_config.sync_enable.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 1, message = "{api_sync_config.sync_enable.length_range}", groups = {Created.class, Updated.class})
    private Boolean syncEnable;

    @Schema(title = "是否发送通知")
    private Boolean noticeEnable;

    @Schema(title = "同步规则")
    private String ruleConfig;

    private static final long serialVersionUID = 1L;
}