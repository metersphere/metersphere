package io.metersphere.api.dto.automation;

import io.metersphere.api.dto.JvmInfoDTO;
import io.metersphere.dto.BaseSystemConfigDTO;
import lombok.Data;

import java.util.Map;
import java.util.List;

@Data
public class RunModeConfig {
    private String mode;
    private String reportType;
    private String reportName;
    private String reportId;
    private String amassReport;
    private boolean onSampleError;
    private String resourcePoolId;
    private BaseSystemConfigDTO baseInfo;
    private List<JvmInfoDTO> testResources;
    /**
     * 运行环境
     */
    private Map<String, String> envMap;
    private String environmentType;
    private String environmentGroupId;
}
