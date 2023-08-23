package io.metersphere.sdk.ldap.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class LdapRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "LDAP地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ldap_url_is_null}")
    private String ldapUrl;

    @Schema(description = "LDAP绑定DN", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ldap_dn_is_null}")
    private String ldapDn;

    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ldap_password_is_null}")
    private String ldapPassword;
}
