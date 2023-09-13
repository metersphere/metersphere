package io.metersphere.sdk.dto.environment.datasource;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class DataSource implements Serializable {
    @Schema(description = "id")
    private String id;
    @Schema(description = "数据源名称")
    private String dataSource;
    @Schema(description = "数据驱动")
    private String driver;
    @Schema(description = "数据驱动id")
    private String driverId;
    @Schema(description = "数据库连接url")
    private String dbUrl;
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "密码")
    private String password;
    @Schema(description = "最大连接数")
    private Long poolMax;
    @Schema(description = "超时时间")
    private Long timeout;
    @Serial
    private static final long serialVersionUID = 1L;
}
