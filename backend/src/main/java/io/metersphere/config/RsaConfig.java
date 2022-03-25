package io.metersphere.config;

import io.metersphere.commons.utils.RsaKey;
import io.metersphere.commons.utils.RsaUtil;
import io.metersphere.service.FileService;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import javax.annotation.Resource;

@Configuration
public class RsaConfig {
    @Resource
    private FileService fileService;

    @EventListener
    public void rsaKey(ContextRefreshedEvent event) throws Exception {
        RsaKey value = fileService.checkRsaKey();
        // 保存到缓存中
        RsaUtil.setRsaKey(value);
    }
}
