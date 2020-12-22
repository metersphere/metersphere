package io.metersphere.api.dto.datacount.request;

import lombok.Getter;
import lombok.Setter;

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
}
