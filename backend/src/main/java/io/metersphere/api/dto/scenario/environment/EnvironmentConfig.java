package io.metersphere.api.dto.scenario.environment;

import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.definition.request.processors.MsJSR223Processor;
import io.metersphere.api.dto.definition.request.processors.post.MsJSR223PostProcessor;
import io.metersphere.api.dto.definition.request.processors.pre.MsJSR223PreProcessor;
import io.metersphere.api.dto.scenario.DatabaseConfig;
import io.metersphere.api.dto.scenario.HttpConfig;
import io.metersphere.api.dto.scenario.TCPConfig;
import io.metersphere.api.dto.ssl.KeyStoreConfig;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EnvironmentConfig {
    private String apiEnvironmentid;
    private CommonConfig commonConfig;
    private HttpConfig httpConfig;
    private List<DatabaseConfig> databaseConfigs;
    private TCPConfig tcpConfig;
    private KeyStoreConfig sslConfig;
    private MsJSR223Processor postProcessor;
    private MsJSR223Processor preProcessor;
    private JSONObject authManager;

    public EnvironmentConfig() {
        this.commonConfig = new CommonConfig();
        this.httpConfig = new HttpConfig();
        this.databaseConfigs = new ArrayList<>();
        this.tcpConfig = new TCPConfig();
        this.sslConfig = new KeyStoreConfig();
    }
}
