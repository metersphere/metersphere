package io.metersphere.config;

import io.metersphere.utils.LocalPathUtil;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UtilsConfig {

    static {
        LocalPathUtil.JAR_PATH += LocalPathUtil.MS;
        LocalPathUtil.PLUGIN_PATH += LocalPathUtil.MS;
    }
}
