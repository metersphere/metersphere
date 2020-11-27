package io.metersphere.api.dto.scenario.environment;

import io.metersphere.api.dto.scenario.DatabaseConfig;
import io.metersphere.api.dto.scenario.HttpConfig;
import io.metersphere.api.dto.scenario.TCPConfig;
import lombok.Data;

import java.util.List;

@Data
public class EnvironmentConfig {
    private CommonConfig commonConfig;
    private HttpConfig httpConfig;
    private List<DatabaseConfig> databaseConfigs;
    private TCPConfig tcpConfig;
}
