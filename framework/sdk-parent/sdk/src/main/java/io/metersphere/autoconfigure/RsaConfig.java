package io.metersphere.autoconfigure;

import io.metersphere.commons.utils.RsaKey;
import io.metersphere.commons.utils.RsaUtil;
import io.metersphere.service.FileService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import javax.annotation.Resource;

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
