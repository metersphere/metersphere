package io.metersphere.system.dto.sdk;

import io.metersphere.sdk.util.RsaKey;
import io.metersphere.sdk.util.RsaUtils;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    @NotBlank(message = "{user_name_is_null}")
    @Size(max = 256, message = "{user_name_length_too_long}")
    private String username;
    @NotBlank(message = "{password_is_null}")
    @Size(max = 256, message = "{password_length_too_long}")
    private String password;
    private String authenticate;


    public String getUsername() {
        try {
            RsaKey rsaKey = RsaUtils.getRsaKey();
            return RsaUtils.privateDecrypt(username, rsaKey.getPrivateKey());
        } catch (Exception e) {
            return username;
        }
    }

    public String getPassword() {
        try {
            RsaKey rsaKey = RsaUtils.getRsaKey();
            return RsaUtils.privateDecrypt(password, rsaKey.getPrivateKey());
        } catch (Exception e) {
            return password;
        }
    }
}
