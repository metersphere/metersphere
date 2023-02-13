package io.metersphere.commons.config;

import io.metersphere.utils.LocalPathUtil;
import io.metersphere.utils.TemporaryFileUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UtilsConfig {
    
    static {
        LocalPathUtil.JAR_PATH += LocalPathUtil.MS;
        LocalPathUtil.PLUGIN_PATH += LocalPathUtil.MS;
    }

    @Bean
    public TemporaryFileUtil temporaryFileUtil() {
        return new TemporaryFileUtil(TemporaryFileUtil.MS_FILE_FOLDER);
    }
}
