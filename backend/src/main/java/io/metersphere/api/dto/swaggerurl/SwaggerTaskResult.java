package io.metersphere.api.dto.swaggerurl;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SwaggerTaskResult {
    //序号
    private int index;
    //定时任务号
    private String taskId;
    //SwaggerUrlId
    private String SwaggerUrlId;
    //SwaggerUrl
    private String swaggerUrl;
    //导入模块
    private String modulePath;
    //同步规则
    private String rule;
    //下次同步时间
    private Long nextExecutionTime;
    //同步开关
    private boolean taskStatus;
    //定时任务类型  swaggerUrlType
    private String taskType;
}
