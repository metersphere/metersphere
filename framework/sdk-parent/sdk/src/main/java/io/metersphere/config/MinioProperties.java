package io.metersphere.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = MinioProperties.MINIO_PREFIX)
@Getter
@Setter
public class MinioProperties {
    public static final String MINIO_PREFIX = "minio";

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket = "metersphere";
}
