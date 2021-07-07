package io.metersphere.controller.request;

import io.metersphere.base.domain.Schedule;
import lombok.Getter;
import lombok.Setter;

/**
 * @author song.tianyang
 * @Date 2021/3/12 12:57 下午
 * @Description
 */
@Getter
@Setter
public class ScheduleRequest extends Schedule {

    //定时任务来源： 测试计划/测试场景
    private String scheduleFrom;

    private String projectId;

    private String moduleId;

    private String modulePath;

    private String modeId;

    private String swaggerUrl;

    private String taskId;

}
