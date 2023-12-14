package io.metersphere.system.config;


import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.file.FileCenter;
import io.metersphere.sdk.file.FileRepository;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.file.MinioRepository;
import io.metersphere.sdk.util.RsaKey;
import io.metersphere.sdk.util.RsaUtils;
import io.minio.MinioClient;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RsaConfig implements ApplicationRunner {
    @Resource
    private MinioClient client;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        FileRequest request = new FileRequest();
        request.setFileName("rsa.key");
        request.setFolder(DefaultRepositoryDir.getSystemRootDir());
        FileRepository fileRepository = FileCenter.getDefaultRepository();
        // 初始化MinIO配置
        ((MinioRepository) fileRepository).init(client);

        try {
            byte[] file = fileRepository.getFile(request);
            if (file != null) {
                RsaKey rsaKey = SerializationUtils.deserialize(file);
                RsaUtils.setRsaKey(rsaKey);
                return;
            }
        } catch (Exception ignored) {
        }
        // 保存到minio
        RsaKey rsaKey = RsaUtils.getRsaKey();
        byte[] bytes = SerializationUtils.serialize(rsaKey);
        fileRepository.saveFile(bytes, request);
        RsaUtils.setRsaKey(rsaKey);
    }
}
