package io.metersphere.system.dto.taskcenter.request;

import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.metersphere.system.dto.taskcenter.enums.ScheduleTagType;
import io.metersphere.sdk.valid.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class TaskCenterScheduleBatchRequest extends TableBatchProcessDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "所属模块", requiredMode = Schema.RequiredMode.REQUIRED)
    @EnumValue(enumClass = ScheduleTagType.class)
    private String scheduleTagType = ScheduleTagType.API_IMPORT.toString();

}
