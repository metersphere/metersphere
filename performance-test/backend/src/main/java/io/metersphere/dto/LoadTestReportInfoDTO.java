package io.metersphere.dto;

import io.metersphere.base.domain.LoadTestReportWithBLOBs;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class LoadTestReportInfoDTO extends LoadTestReportWithBLOBs {
    //项目环境
    private Map<String, List<String>> projectEnvMap;
}
