package io.metersphere.system.dto.sdk;

import lombok.Data;

@Data
public class EMailInfoDto {
    private String host;
    private String port;
    private String account;
    private String from;
    private String password;
    private String ssl;
    private String tsl;
    private String recipient;

}
