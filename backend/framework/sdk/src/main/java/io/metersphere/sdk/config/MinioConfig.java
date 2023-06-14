package io.metersphere.sdk.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    public static final String BUCKET = "metersphere";

    @Bean
    public MinioClient minioClient(MinioProperties minioProperties) throws Exception {
        // 创建 MinioClient 客户端
        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();

        boolean exist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKET).build());
        if (!exist) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET).build());
        }
        return minioClient;
    }

}