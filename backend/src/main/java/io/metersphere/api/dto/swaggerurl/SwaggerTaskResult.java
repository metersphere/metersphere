package io.metersphere.api.dto.swaggerurl;

import io.metersphere.base.domain.SwaggerUrlProject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SwaggerTaskResult extends SwaggerUrlProject {
    //序号
    private int index;
    //定时任务号
    private String taskId;
    //同步规则
    private String rule;
    //下次同步时间
    private Long nextExecutionTime;
    //同步开关
    private boolean enable;
    //定时任务类型  swaggerUrlType
    private String taskType;
}
