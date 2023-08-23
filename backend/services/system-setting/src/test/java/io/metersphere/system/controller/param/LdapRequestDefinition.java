package io.metersphere.system.controller.param;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LdapRequestDefinition {

    @NotBlank(message = "{ldap_url_is_null}")
    private String ldapUrl;

    @NotBlank(message = "{ldap_dn_is_null}")
    private String ldapDn;

    @NotBlank(message = "{ldap_password_is_null}")
    private String ldapPassword;
}
