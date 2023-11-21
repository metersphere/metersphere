package io.metersphere.system.config;


import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.util.RsaKey;
import io.metersphere.sdk.util.RsaUtils;
import io.metersphere.system.file.FileCenter;
import io.metersphere.system.file.FileRepository;
import io.metersphere.system.file.FileRequest;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RsaConfig implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        FileRequest request = new FileRequest();
        request.setFileName("rsa.key");
        request.setFolder(DefaultRepositoryDir.getSystemRootDir());
        FileRepository fileRepository = FileCenter.getDefaultRepository();

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
