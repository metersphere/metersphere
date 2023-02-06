package io.metersphere.commons.config;

import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.config.MinioProperties;
import io.metersphere.enums.MinIOConfigEnum;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class MinioConfig {

    public static Map<String, Object> getMinio() {
        MinioProperties minioProperties = CommonBeanFactory.getBean(MinioProperties.class);
        Map<String, Object> minioPros = new HashMap<>();
        minioPros.put(MinIOConfigEnum.ENDPOINT, minioProperties.getEndpoint());
        minioPros.put(MinIOConfigEnum.ACCESS_KEY, minioProperties.getAccessKey());
        minioPros.put(MinIOConfigEnum.SECRET_KEY, minioProperties.getSecretKey());
        minioPros.put(MinIOConfigEnum.BUCKET, minioProperties.getBucket());
        return minioPros;
    }
}
