package io.metersphere.autoconfigure;

import io.metersphere.config.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;

public class MinIOConfig {

    @Bean
    public MinioClient minioClient(MinioProperties minioProperties) throws Exception {

        // 创建 MinioClient 客户端
        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();

        String bucket = minioProperties.getBucket();
        boolean exist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exist) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
        return minioClient;
    }

}