package io.metersphere.commons.config;

import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.config.MinioProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class MinioConfig {

    public static Map<String, Object> getMinio() {
        MinioProperties minioProperties = CommonBeanFactory.getBean(MinioProperties.class);
        Map<String, Object> minioPros = new HashMap<>();
        minioPros.put("endpoint", minioProperties.getEndpoint());
        minioPros.put("accessKey", minioProperties.getAccessKey());
        minioPros.put("secretKey", minioProperties.getSecretKey());
        minioPros.put("bucket", minioProperties.getBucket());
        return minioPros;
    }
}
