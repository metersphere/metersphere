package io.metersphere.project.dto.environment;

import io.metersphere.project.dto.environment.common.CommonParams;
import io.metersphere.project.dto.environment.datasource.DataSource;
import io.metersphere.project.dto.environment.host.HostConfig;
import io.metersphere.project.dto.environment.http.HttpConfig;
import io.metersphere.project.dto.environment.processors.EnvProcessorConfig;
import io.metersphere.project.dto.environment.variables.CommonVariables;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class EnvironmentConfig {
    @Schema(description = "公共参数 请求超时时间、响应超时时间")
    private CommonParams commonParams = new CommonParams();
    @Schema(description = "环境变量")
    private List<CommonVariables> commonVariables = new ArrayList<>(0);
    @Schema(description = "HTTP配置")
    private List<HttpConfig> httpConfig = new ArrayList<>(0);
    @Schema(description = "数据库配置")
    private List<DataSource> dataSources = new ArrayList<>(0);

    @Schema(description = "Host配置")
    private HostConfig hostConfig = new HostConfig();
    @Schema(description = "全局前置脚本")
    private EnvProcessorConfig preProcessorConfig = new EnvProcessorConfig();
    @Schema(description = "全局后置脚本")
    private EnvProcessorConfig postProcessorConfig = new EnvProcessorConfig();
    @Schema(description = "全局断言")
    private MsEnvAssertionConfig assertionConfig = new MsEnvAssertionConfig();
    @Schema(description = "插件自定义的配置项，key为插件ID，value 为对应配置")
    private Map<String, Map<String, Object>> pluginConfigMap = HashMap.newHashMap(0);
}
