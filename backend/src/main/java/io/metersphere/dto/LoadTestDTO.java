package io.metersphere.dto;

import io.metersphere.base.domain.LoadTestWithBLOBs;
import io.metersphere.base.domain.Schedule;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LoadTestDTO extends LoadTestWithBLOBs {
    private String projectName;
    private String userName;
    private String versionName;
    private Schedule schedule;
    private Boolean isNeedUpdate;
    private List<String> follows;
}
