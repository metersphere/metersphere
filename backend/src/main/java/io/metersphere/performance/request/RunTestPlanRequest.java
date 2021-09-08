package io.metersphere.performance.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RunTestPlanRequest extends TestPlanRequest {
    private String userId;
    private String triggerMode;
    /**
     * 测试计划用例跑性能测试时需要设置此值
     */
    private String testPlanLoadId;
    /**
     * 压力配置信息
     */
    private String ownLoadConfiguration;
    /**
     * 资源池ID
     */
    private String ownTestPoolId;
}
