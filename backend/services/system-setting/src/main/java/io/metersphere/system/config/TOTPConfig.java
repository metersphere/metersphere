package io.metersphere.system.config;

import com.bastiaanjansen.otp.HMACAlgorithm;
import com.bastiaanjansen.otp.TOTPGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;

@Configuration
public class TOTPConfig {
    @Value("${totp.secret:secret}")
    private String secret;

    @Bean
    public TOTPGenerator totpGenerator() {
        return new TOTPGenerator.Builder(secret.getBytes(StandardCharsets.UTF_8))
                .withHOTPGenerator(builder -> {
                    builder.withPasswordLength(6);
                    builder.withAlgorithm(HMACAlgorithm.SHA256); // SHA256 and SHA512 are also supported
                })
                .build();
    }
}
