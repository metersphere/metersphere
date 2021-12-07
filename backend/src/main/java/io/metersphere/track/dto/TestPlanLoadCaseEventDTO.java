package io.metersphere.track.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author song.tianyang
 * @Date 2021/1/20 4:07 下午
 * @Description    测试计划性能测试执行完毕后的回调参数
 */
@Getter
@Setter
public class TestPlanLoadCaseEventDTO {
    private String reportId;
    private String triggerMode;
    private String status;
    private String id;
}
