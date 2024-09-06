package io.metersphere.system.dto.taskcenter.request;

import io.metersphere.system.dto.sdk.BasePageRequest;
import io.metersphere.system.dto.taskcenter.enums.ScheduleTagType;
import io.metersphere.sdk.valid.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author: LAN
 * @date: 2024/1/22 16:38
 * @version: 1.0
 */
@Data
public class TaskCenterSchedulePageRequest extends BasePageRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description =  "定时任务所属类别", requiredMode = Schema.RequiredMode.REQUIRED)
    @EnumValue(enumClass = ScheduleTagType.class)
    private String scheduleTagType = ScheduleTagType.API_IMPORT.toString();
}
