package io.metersphere.listener;

import io.metersphere.service.PlatformPluginService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class InitListener implements ApplicationRunner {

    @Resource
    private PlatformPluginService platformPluginService;

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        platformPluginService.loadPlatFormPlugins();
    }
}
