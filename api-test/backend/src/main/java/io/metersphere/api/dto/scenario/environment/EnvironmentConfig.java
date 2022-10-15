package io.metersphere.api.dto.scenario.environment;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.metersphere.api.dto.scenario.DatabaseConfig;
import io.metersphere.api.dto.scenario.HttpConfig;
import io.metersphere.api.dto.scenario.TCPConfig;
import io.metersphere.api.dto.scenario.environment.item.EnvAssertions;
import io.metersphere.api.dto.scenario.environment.item.EnvJSR223PostProcessor;
import io.metersphere.api.dto.scenario.environment.item.EnvJSR223PreProcessor;
import io.metersphere.api.dto.scenario.environment.item.EnvJSR223Processor;
import io.metersphere.environment.ssl.KeyStoreConfig;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EnvironmentConfig {
    private String environmentId;
    private CommonConfig commonConfig;
    private HttpConfig httpConfig;
    private List<DatabaseConfig> databaseConfigs;
    private TCPConfig tcpConfig;
    private KeyStoreConfig sslConfig;
    //全局前后置脚本（每个请求都跑一遍）
    private EnvJSR223PreProcessor preProcessor;
    private EnvJSR223PostProcessor postProcessor;
    //全局前后置脚本步骤（只在全部步骤都前后做处理）

    private EnvJSR223Processor preStepProcessor;
    private EnvJSR223Processor postStepProcessor;
    //全局前后置脚本都配置
    private GlobalScriptConfig globalScriptConfig;
    private EnvAuthManager authManager;
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS,include = JsonTypeInfo.As.WRAPPER_ARRAY,defaultImpl = EnvAssertions.class)
    private List<EnvAssertions> assertions;
    private boolean useErrorCode;
    private boolean higherThanSuccess;
    private boolean higherThanError;

    public EnvironmentConfig() {
        this.commonConfig = new CommonConfig();
        this.httpConfig = new HttpConfig();
        this.databaseConfigs = new ArrayList<>();
        this.tcpConfig = new TCPConfig();
        this.sslConfig = new KeyStoreConfig();
    }
}
