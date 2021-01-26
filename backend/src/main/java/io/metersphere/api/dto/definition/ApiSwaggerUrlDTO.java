package io.metersphere.api.dto.definition;

import io.metersphere.base.domain.Schedule;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiSwaggerUrlDTO extends Schedule {
    //序号
    private int index;
    private String id;
    private String projectId;
    private String swaggerUrl;
    private String scheduleId;
    //下次执行时间
    private Long nextExecutionTime;
}
