package io.metersphere.gateway.config;

import io.metersphere.commons.utils.RsaKey;
import io.metersphere.commons.utils.RsaUtil;
import io.metersphere.gateway.service.FileService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
public class RsaConfig implements ApplicationRunner {
    @Resource
    private FileService fileService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        RsaKey value = fileService.checkRsaKey();
        // 保存到缓存中
        RsaUtil.setRsaKey(value);
    }
}
