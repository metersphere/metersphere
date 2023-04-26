package io.metersphere.dto;

import io.metersphere.constants.RunModeConstants;
import io.metersphere.vo.BooleanPool;
import lombok.Data;
import org.apache.jorphan.collections.HashTree;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class JmeterRunRequestDTO {
    /**
     * 队列ID
     */
    private String queueId;
    /**
     * 执行模式/API/SCENARIO/PLAN_API等
     */
    private String runMode;

    /**
     * RunModeConstants.SET_REPORT : RunModeConstants.INDEPENDENCE
     */
    private String reportType;
    /**
     * 报告ID
     */
    private String reportId;
    /**
     * 测试计划报告ID
     */
    private String testPlanReportId;

    /**
     * 资源id：/场景id/用例id/接口id/测试计划场景id/测试计划用例id
     */
    private String testId;

    /**
     * 是否发送node节点执行
     */
    private BooleanPool pool;
    /**
     * 资源池Id
     */
    private String poolId;

    /**
     * 并行/串行
     */
    private String runType;
    /**
     * 调试
     */
    private boolean isDebug;
    /**
     * 执行脚本
     */
    private HashTree hashTree;

    /**
     * 并发数
     */
    private int corePoolSize;

    /**
     * 是否开启定时同步
     */
    private boolean enable;

    /**
     * KAFKA配置只用在node节点中会用到
     */
    private Map<String, Object> kafkaConfig;

    /**
     * 增加一个全局扩展的通传参数
     */
    private Map<String, Object> extendedParameters;

    /**
     * 平台服务地址只用在node节点中会用到
     */
    private String platformUrl;

    // 失败重试
    private boolean retryEnable;

    // 失败重试次数
    private long retryNum;

    //MinIO配置和系统插件信息
    private PluginConfigDTO pluginConfigDTO;

    //自定义jar信息
    private Map<String, List<ProjectJarConfig>> customJarInfo;

    // 自定义误报库
    private Map<String, List<MsRegexDTO>> fakeErrorMap;

    private String triggerMode;

    public JmeterRunRequestDTO() {
    }

    public JmeterRunRequestDTO(String testId, String reportId, String runMode, String triggerMode) {
        this.testId = testId;
        this.reportId = reportId;
        this.runMode = runMode;
        this.triggerMode = triggerMode;
        this.reportType = RunModeConstants.INDEPENDENCE.name();
        this.pool = new BooleanPool();
        this.extendedParameters = new LinkedHashMap<>();
    }

    public JmeterRunRequestDTO(String testId, String reportId, String runMode, String triggerMode , HashTree hashTree) {
        this.testId = testId;
        this.reportId = reportId;
        this.runMode = runMode;
        this.triggerMode = triggerMode;
        this.reportType = RunModeConstants.INDEPENDENCE.name();
        this.hashTree = hashTree;
        this.pool = new BooleanPool();
        this.extendedParameters = new LinkedHashMap<>();
    }
}
