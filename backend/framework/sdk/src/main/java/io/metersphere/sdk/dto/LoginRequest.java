package io.metersphere.sdk.dto;

import io.metersphere.sdk.util.RsaKey;
import io.metersphere.sdk.util.RsaUtil;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    @NotBlank(message = "{user_name_is_null}")
    private String username;
    @NotBlank(message = "{password_is_null}")
    private String password;
    private String authenticate;


    public String getUsername() {
        try {
            RsaKey rsaKey = RsaUtil.getRsaKey();
            return RsaUtil.privateDecrypt(username, rsaKey.getPrivateKey());
        } catch (Exception e) {
            return username;
        }
    }

    public String getPassword() {
        try {
            RsaKey rsaKey = RsaUtil.getRsaKey();
            return RsaUtil.privateDecrypt(password, rsaKey.getPrivateKey());
        } catch (Exception e) {
            return password;
        }
    }
}
