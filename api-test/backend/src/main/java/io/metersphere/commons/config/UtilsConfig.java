package io.metersphere.commons.config;

import io.metersphere.utils.TemporaryFileUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UtilsConfig {
    @Bean
    public TemporaryFileUtil temporaryFileUtil() {
        return new TemporaryFileUtil(TemporaryFileUtil.MS_FILE_FOLDER);
    }
}
