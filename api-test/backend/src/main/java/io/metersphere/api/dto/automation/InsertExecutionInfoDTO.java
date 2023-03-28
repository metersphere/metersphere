package io.metersphere.api.dto.automation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class InsertExecutionInfoDTO {
    public String sourceId;
    public String execResult;
    public String triggerMode;
    public String projectId;
    public String executeType;
    public String version;
    public String execReportId;
}
