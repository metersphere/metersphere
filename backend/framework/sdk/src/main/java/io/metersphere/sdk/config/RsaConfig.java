package io.metersphere.sdk.config;


import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RsaConfig implements ApplicationRunner {
//    @Resource
//    private FileService fileService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
//        // todo 从数据库中获取 RSA 密钥对
//        RsaKey value = fileService.checkRsaKey();
//        RsaUtil.setRsaKey(value);
    }
}
