package io.metersphere.track.dto;

import io.metersphere.base.domain.TestPlan;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestPlanDTO extends TestPlan {
    private String projectName;
    private String userName;
    private List<String> projectIds;

    /**
     * 定时任务ID
     */
    private String scheduleId;
    /**
     * 定时任务是否开启
     */
    private boolean scheduleOpen;
}
