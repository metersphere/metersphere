package io.metersphere.api.dto.scenario;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class TCPConfig {
    private String classname = StringUtils.EMPTY;
    private String server = StringUtils.EMPTY;
    private int port = 0;
    private String ctimeout = StringUtils.EMPTY;
    private String timeout = StringUtils.EMPTY;
    private boolean reUseConnection = true;
    private boolean nodelay;
    private boolean closeConnection;
    private String soLinger = StringUtils.EMPTY;
    private String eolByte = StringUtils.EMPTY;
    private String username = StringUtils.EMPTY;
    private String password = StringUtils.EMPTY;
    private String description = StringUtils.EMPTY;
}
