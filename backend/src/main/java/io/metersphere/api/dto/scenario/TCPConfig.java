package io.metersphere.api.dto.scenario;

import lombok.Data;

@Data
public class TCPConfig {
    private String classname;
    private String server;
    private Integer port;
    private Integer ctimeout;
    private Integer timeout;
    private Boolean reUseConnection;
    private Boolean nodelay;
    private Boolean closeConnection;
    private String soLinger;
    private String eolByte;
    private String username;
    private String password;
}
