package io.metersphere.dto;

import io.metersphere.base.domain.Schedule;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScheduleDao extends Schedule {

    private String resourceName;
    private String userName;
}
