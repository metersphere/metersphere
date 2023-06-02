package io.metersphere.sdk.dto;

import io.metersphere.sdk.util.CodingUtil;
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
            return CodingUtil.base64Decoding(username);
        } catch (Exception e) {
            return username;
        }
    }

    public String getPassword() {
        try {
            return CodingUtil.base64Decoding(password);
        } catch (Exception e) {
            return password;
        }
    }
}
