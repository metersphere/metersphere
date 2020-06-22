package io.metersphere.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScheduleDTO {
    private Boolean enable;
    private String cronExpression;
}
