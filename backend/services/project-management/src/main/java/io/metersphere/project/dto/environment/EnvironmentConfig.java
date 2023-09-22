package io.metersphere.project.dto.environment;

import io.metersphere.project.dto.environment.assertions.EnvironmentAssertions;
import io.metersphere.project.dto.environment.auth.AuthConfig;
import io.metersphere.project.dto.environment.common.CommonParams;
import io.metersphere.project.dto.environment.datasource.DataSource;
import io.metersphere.project.dto.environment.host.HostConfig;
import io.metersphere.project.dto.environment.http.HttpConfig;
import io.metersphere.project.dto.environment.script.post.EnvironmentPostScript;
import io.metersphere.project.dto.environment.script.pre.EnvironmentPreScript;
import io.metersphere.project.dto.environment.tcp.TCPConfig;
import io.metersphere.project.dto.environment.variables.CommonVariables;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
    @Schema(description = "TCP配置")
    private TCPConfig tcpConfig;
    @Schema(description = "认证配置")
    private AuthConfig authConfig;
    @Schema(description = "全局前置脚本")
    private EnvironmentPreScript preScript;
    @Schema(description = "全局后置脚本")
    private EnvironmentPostScript postScript;
    @Schema(description = "全局断言")
    private EnvironmentAssertions assertions;


    public EnvironmentConfig() {
        this.commonParams = new CommonParams();
        this.commonVariables = new ArrayList<>();
        this.httpConfig = new ArrayList<>();
        this.dataSources = new ArrayList<>();
        this.hostConfig = new HostConfig();
        this.tcpConfig = new TCPConfig();
        this.authConfig = new AuthConfig();
        this.preScript = new EnvironmentPreScript();
        this.postScript = new EnvironmentPostScript();
        this.assertions = new EnvironmentAssertions();
    }

}
