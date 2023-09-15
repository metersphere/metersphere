package io.metersphere.system.ldap.vo;

import io.metersphere.sdk.dto.LoginRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class LdapLoginRequest extends LoginRequest implements Serializable {

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

    @Schema(description = "用户过滤器", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ldap_user_filter_is_null}")
    private String ldapUserFilter;

    @Schema(description = "用户OU", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ldap_ou_is_null}")
    private String ldapUserOu;

    @Schema(description = "LDAP属性映射", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ldap_user_mapping_is_null}")
    private String ldapUserMapping;
}
