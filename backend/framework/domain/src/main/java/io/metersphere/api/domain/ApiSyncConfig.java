package io.metersphere.api.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Schema(title = "接口同步用例配置")
@Table("api_sync_config")
@Data
public class ApiSyncConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{api_sync_config.id.not_blank}", groups = {Updated.class})
    @Schema(title = "", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 50, message = "{api_sync_config.resource_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_sync_config.resource_id.not_blank}", groups = {Created.class})
    @Schema(title = "API/CASE 来源fk", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String resourceId;

    @Size(min = 1, max = 50, message = "{api_sync_config.resource_type.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_sync_config.resource_type.not_blank}", groups = {Created.class})
    @Schema(title = "来源类型/API/CASE", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String resourceType;


    @Schema(title = "是否隐藏", requiredMode = Schema.RequiredMode.NOT_REQUIRED, allowableValues = "range[1, 1]")
    private Boolean hide;


    @Schema(title = "同步规则", requiredMode = Schema.RequiredMode.NOT_REQUIRED, allowableValues = "range[1, ]")
    private String ruleConfig;

    @Size(min = 1, max = 1, message = "{api_sync_config.notify_case_creator.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_sync_config.notify_case_creator.not_blank}", groups = {Created.class})
    @Schema(title = "是否通知用例创建人", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 1]")
    private Boolean notifyCaseCreator;

    @Size(min = 1, max = 1, message = "{api_sync_config.notify_scenario_creator.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_sync_config.notify_scenario_creator.not_blank}", groups = {Created.class})
    @Schema(title = "是否通知场景创建人", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 1]")
    private Boolean notifyScenarioCreator;

    @Size(min = 1, max = 1, message = "{api_sync_config.sync_enable.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_sync_config.sync_enable.not_blank}", groups = {Created.class})
    @Schema(title = "是否同步用例", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 1]")
    private Boolean syncEnable;

    @Schema(title = "是否发送通知", requiredMode = Schema.RequiredMode.NOT_REQUIRED, allowableValues = "range[1, 1]")
    private Boolean noticeEnable;

}