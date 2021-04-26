package io.metersphere.track.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author song.tianyang
 * @Date 2021/4/26 5:06 下午
 * @Description
 */
@Getter
@Setter
public class TestPlanScenarioCaseDTO {
    private String id;
    private String projectId;
    private String testPlanId;
    private String apiScenarioId;
}
