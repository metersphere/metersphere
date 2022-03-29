package io.metersphere.notice.domain;

import lombok.Data;

@Data
public class MailInfo {
    private String host;
    private String port;
    private String account;
    private String from;
    private String password;
    private String ssl;
    private String tls;
    private String recipient;

}
