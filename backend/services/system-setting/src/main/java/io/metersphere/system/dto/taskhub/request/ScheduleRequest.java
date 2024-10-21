package io.metersphere.system.dto.taskhub.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author wx
 */
@Data
public class ScheduleRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "列表id")
    private String id;

    @Schema(description = "cron表达式")
    private String cron;
}
