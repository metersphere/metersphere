package io.metersphere.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class RunModeConfigDTO {
    private String mode;
    private String reportType;
    private String reportName;
    private String reportId;
    private String testId;
    private String amassReport;
    private boolean onSampleError;
    private Boolean defaultEnv;
    // 失败重试
    private boolean retryEnable;
    // 失败重试次数
    private long retryNum;
    // 资源池
    private String resourcePoolId;
    private BaseSystemConfigDTO baseInfo;
    private List<JvmInfoDTO> testResources;
    /**
     * 运行环境
     */
    private Map<String, String> envMap;

    //测试计划整体执行时的默认环境
    private Map<String, List<String>> testPlanDefaultEnvMap;
    private String environmentType;
    private String environmentGroupId;
    //ui 测试
    private String browser;
    private boolean headlessEnabled;
}
