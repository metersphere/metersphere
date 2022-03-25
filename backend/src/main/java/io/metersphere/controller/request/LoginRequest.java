package io.metersphere.controller.request;

import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.RsaKey;
import io.metersphere.commons.utils.RsaUtil;
import lombok.Getter;
import lombok.Setter;

import java.security.NoSuchAlgorithmException;

@Getter
@Setter
public class LoginRequest {
    private String username;
    private String password;
    private String authenticate;


    public String getUsername() {
        try {
            RsaKey rsaKey = CommonBeanFactory.getBean(RsaKey.class);
            return RsaUtil.privateDecrypt(username, rsaKey.getPrivateKey());
        } catch (Exception e) {
            return username;
        }
    }

    public String getPassword() {
        try {
            RsaKey rsaKey = CommonBeanFactory.getBean(RsaKey.class);
            return RsaUtil.privateDecrypt(password, rsaKey.getPrivateKey());
        } catch (Exception e) {
            return password;
        }
    }
}
