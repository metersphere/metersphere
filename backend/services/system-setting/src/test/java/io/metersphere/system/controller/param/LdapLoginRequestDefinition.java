package io.metersphere.system.controller.param;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LdapLoginRequestDefinition {

    @NotBlank(message = "{ldap_url_is_null}")
    private String ldapUrl;

    @NotBlank(message = "{ldap_dn_is_null}")
    private String ldapDn;

    @NotBlank(message = "{ldap_password_is_null}")
    private String ldapPassword;

    @NotBlank(message = "{ldap_user_filter_is_null}")
    private String ldapUserFilter;

    @NotBlank(message = "{ldap_ou_is_null}")
    private String ldapUserOu;

    @NotBlank(message = "{ldap_user_mapping_is_null}")
    private String ldapUserMapping;
}
