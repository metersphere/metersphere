package io.metersphere.dto;

import io.metersphere.base.domain.LoadTest;
import io.metersphere.base.domain.Schedule;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoadTestDTO extends LoadTest {
    private String projectName;
    private String userName;
    private Schedule schedule;
    private Boolean isNeedUpdate;
}
