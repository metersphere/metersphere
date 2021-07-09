package io.metersphere.api.dto.definition.request;

import io.metersphere.api.dto.definition.request.variable.ScenarioVariable;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.dto.ssl.MsKeyStore;
import io.metersphere.jmeter.utils.ScriptEngineUtils;
import lombok.Data;
import org.apache.jmeter.config.Arguments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ParameterConfig {
    /**
     * 环境配置
     */
    private Map<String, EnvironmentConfig> config;
    /**
     * 缓存同一批请求的认证信息
     */
    private Map<String, MsKeyStore> keyStoreMap = new HashMap<>();
    /**
     * 公共场景参数
     */
    private List<ScenarioVariable> variables;
    /**
     * 公共Cookie
     */
    private boolean enableCookieShare;
    /**
     * 是否停止继续
     */
    private Boolean onSampleError;

    /**
     * 是否是导入/导出操作
     */
    private boolean isOperating;
    /**
     * 项目ID，支持单接口执行
     */
    private String projectId;

    private List<String> csvFilePaths = new ArrayList<>();


    public boolean isEffective(String projectId) {
        if (this.config != null && this.config.get(projectId) != null) {
            return true;
        }
        return false;
    }

    static public Arguments valueSupposeMock(Arguments arguments) {
        for(int i = 0; i < arguments.getArguments().size(); ++i) {
            String argValue = arguments.getArgument(i).getValue();
            arguments.getArgument(i).setValue(ScriptEngineUtils.buildFunctionCallString(argValue));
        }
        return arguments;
    }

}
