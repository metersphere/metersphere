package io.metersphere.project.dto.environment;

import io.metersphere.project.dto.environment.auth.AuthConfig;
import io.metersphere.project.dto.environment.common.CommonParams;
import io.metersphere.project.dto.environment.datasource.DataSource;
import io.metersphere.project.dto.environment.host.HostConfig;
import io.metersphere.project.dto.environment.http.HttpConfig;
import io.metersphere.project.dto.environment.processors.EnvProcessorConfig;
import io.metersphere.project.dto.environment.variables.CommonVariables;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class EnvironmentConfig implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Schema(description = "公共参数 请求超时时间、响应超时时间")
    private CommonParams commonParams;
    @Schema(description = "环境变量")
    private List<CommonVariables> commonVariables;
    @Schema(description = "HTTP配置")
    private List<HttpConfig> httpConfig;
    @Schema(description = "数据库配置")
    private List<DataSource> dataSources;
    @Schema(description = "Host配置")
    private HostConfig hostConfig;
    @Schema(description = "认证配置")
    private AuthConfig authConfig;
    @Schema(description = "全局前置脚本")
    private EnvProcessorConfig preProcessorConfig;
    @Schema(description = "全局后置脚本")
    private EnvProcessorConfig postProcessorConfig;
    @Schema(description = "全局断言")
    private MsEnvAssertionConfig assertionConfig;
    @Schema(description = "插件自定义的配置项，key为插件ID，value 为对应配置")
    private Map<String, Map<String, Object>> pluginConfigMap;


    public EnvironmentConfig() {
        this.commonParams = new CommonParams();
        this.commonVariables = List.of(new CommonVariables());
        this.httpConfig = List.of(new HttpConfig());
        this.dataSources = List.of(new DataSource());
        this.hostConfig = new HostConfig();
        this.authConfig = new AuthConfig();
        this.preProcessorConfig = new EnvProcessorConfig();
        this.postProcessorConfig = new EnvProcessorConfig();
        this.assertionConfig = new MsEnvAssertionConfig();
    }

}
