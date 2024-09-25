package io.metersphere.system.config;

import io.metersphere.sdk.util.LogUtils;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketLifecycleArgs;
import io.minio.messages.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;

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

        // 设置临时目录下文件的过期时间
        setBucketLifecycle(minioClient);
        setBucketLifecycleByExcel(minioClient);

        boolean exist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKET).build());
        if (!exist) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET).build());
        }
        return minioClient;
    }

    /**
     * 设置生命周期规则-文件的过期时间
     * 将 system/temp/ 下的文件设置为 7 天后过期
     * 参考 minio 8.5.2 版本的示例代码
     * https://github.com/minio/minio-java/blob/8.5.2/examples/SetBucketLifecycle.java
     */
    private static void setBucketLifecycle(MinioClient minioClient) {
        List<LifecycleRule> rules = new LinkedList<>();
        rules.add(
                new LifecycleRule(
                        Status.ENABLED,
                        null,
                        new Expiration((ZonedDateTime) null, 7, null),
                        new RuleFilter("system/temp/"),
                        "temp-file",
                        null,
                        null,
                        null));
        LifecycleConfiguration config = new LifecycleConfiguration(rules);
        try {
            minioClient.setBucketLifecycle(
                    SetBucketLifecycleArgs.builder()
                            .bucket(BUCKET)
                            .config(config)
                            .build());
        } catch (Exception e) {
            LogUtils.error(e);
        }
    }



    /**
     * 设置生命周期规则-文件的过期时间
     * 将 system/export/excel/ 下的文件设置为 1 天后过期
     * 参考 minio 8.5.2 版本的示例代码
     * https://github.com/minio/minio-java/blob/8.5.2/examples/SetBucketLifecycle.java
     */
    private static void setBucketLifecycleByExcel(MinioClient minioClient) {
        List<LifecycleRule> rules = new LinkedList<>();
        rules.add(
                new LifecycleRule(
                        Status.ENABLED,
                        null,
                        new Expiration((ZonedDateTime) null, 1, null),
                        new RuleFilter("system/export/excel"),
                        "excel-file",
                        null,
                        null,
                        null));
        rules.add(
                new LifecycleRule(
                        Status.ENABLED,
                        null,
                        new Expiration((ZonedDateTime) null, 1, null),
                        new RuleFilter("system/export/api"),
                        "api-file",
                        null,
                        null,
                        null));
        LifecycleConfiguration config = new LifecycleConfiguration(rules);
        try {
            minioClient.setBucketLifecycle(
                    SetBucketLifecycleArgs.builder()
                            .bucket(BUCKET)
                            .config(config)
                            .build());
        } catch (Exception e) {
            LogUtils.error(e);
        }
    }

}