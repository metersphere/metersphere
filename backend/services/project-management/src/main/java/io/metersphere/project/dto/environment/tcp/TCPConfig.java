package io.metersphere.project.dto.environment.tcp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class TCPConfig implements Serializable {
    @Schema(description = "TCPClient  选项为TCPClientImpl、BinaryTCPClientImpl、LengthPrefixedBinaryTCPClientImpl")
    private String className;
    @Schema(description = "服务器名或IP")
    private String server;
    @Schema(description = "端口")
    private int port = 0;
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "密码")
    private String password;
    @Schema(description = "连接超时")
    private String connectTimeout;
    @Schema(description = "响应超时")
    private String timeout;
    @Schema(description = "So Linger")
    private String soLinger;
    @Schema(description = "Re-use connection")
    private Boolean reUseConnection;
    @Schema(description = "设置无延迟")
    private Boolean nodelay;
    @Schema(description = "Close Connection")
    private Boolean closeConnection;
    @Schema(description = "行尾(EOL)字节值")
    private String eolByte;

    @Serial
    private static final long serialVersionUID = 1L;
}
