package io.metersphere.gateway.config;

import io.metersphere.commons.utils.RsaKey;
import io.metersphere.commons.utils.RsaUtil;
import io.metersphere.gateway.service.FileService;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import javax.annotation.Resource;

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
