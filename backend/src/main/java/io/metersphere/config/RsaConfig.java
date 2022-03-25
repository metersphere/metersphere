package io.metersphere.config;

import io.metersphere.commons.utils.RsaKey;
import io.metersphere.commons.utils.RsaUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.NoSuchAlgorithmException;

@Configuration
public class RsaConfig {
    @Bean
    public RsaKey rsaKey() throws NoSuchAlgorithmException {
        return RsaUtil.createKeys();
    }
}
