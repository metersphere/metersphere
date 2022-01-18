package io.metersphere.controller.request;

import io.metersphere.commons.utils.RsaKey;
import io.metersphere.commons.utils.RsaUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    private String username;
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
