package io.metersphere.api.dto.scenario;

import lombok.Data;

@Data
public class TCPConfig {
    private String classname = "";
    private String server = "";
    private String port = "";
    private String ctimeout = "";
    private String timeout = "";
    private boolean reUseConnection = true;
    private boolean nodelay;
    private boolean closeConnection;
    private String soLinger = "";
    private String eolByte = "";
    private String username = "";
    private String password = "";
    private String description = "";
}
