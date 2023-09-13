package io.metersphere.sdk.config;


import io.metersphere.sdk.file.FileCenter;
import io.metersphere.sdk.file.FileRepository;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.log.constants.OperationLogModule;
import io.metersphere.sdk.util.RsaKey;
import io.metersphere.sdk.util.RsaUtil;
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
        request.setProjectId("system");
        request.setResourceId(OperationLogModule.SYSTEM_PARAMETER_SETTING);
        FileRepository fileRepository = FileCenter.getDefaultRepository();

        try {
            byte[] file = fileRepository.getFile(request);
            if (file != null) {
                RsaKey rsaKey = SerializationUtils.deserialize(file);
                RsaUtil.setRsaKey(rsaKey);
                return;
            }
        } catch (Exception ignored) {
        }
        // 保存到minio
        RsaKey rsaKey = RsaUtil.getRsaKey();
        byte[] bytes = SerializationUtils.serialize(rsaKey);
        fileRepository.saveFile(bytes, request);
        RsaUtil.setRsaKey(rsaKey);
    }
}
