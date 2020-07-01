package io.metersphere.ldap.domain;

import lombok.Data;

@Data
public class LdapInfo {

    private String url;
    private String dn;
    private String password;
    private String ou;
    private String filter;
    private String mapping;
    private String open;

}
