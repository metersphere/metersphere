package io.metersphere.reportstatistics.dto.response;

import io.metersphere.base.domain.EnterpriseTestReportWithBLOBs;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnterpriseTestReportDTO extends EnterpriseTestReportWithBLOBs {
    private boolean scheduleIsOpen;
    private String scheduleStatus;
    private String scheduleId;
    //定时任务下一次执行时间
    private Long scheduleExecuteTime;
}
