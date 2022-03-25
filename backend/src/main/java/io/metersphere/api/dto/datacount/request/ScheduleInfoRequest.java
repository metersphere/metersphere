package io.metersphere.api.dto.datacount.request;

import io.metersphere.track.request.testcase.QueryTestPlanRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author song.tianyang
 * @Date 2020/12/17 5:04 下午
 * @Description
 */
@Getter
@Setter
public class ScheduleInfoRequest {
    private String taskID;
    private boolean enable;
    private List<String> taskIds;
    private boolean selectAll;
    private QueryTestPlanRequest queryTestPlanRequest;
}
