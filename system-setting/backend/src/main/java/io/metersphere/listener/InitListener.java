package io.metersphere.listener;

import io.metersphere.commons.utils.LogUtil;
import io.metersphere.service.PlatformPluginService;
import io.metersphere.service.PluginService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

@Component
public class InitListener implements ApplicationRunner {

    @Resource
    private PlatformPluginService platformPluginService;
    @Resource
    private PluginService pluginService;

    @Override
    public void run(ApplicationArguments applicationArguments) {
        LogUtil.info("================= SYSTEM-SETTING 应用启动 =================");
        pluginService.loadPlugins();
        platformPluginService.loadPlatFormPlugins();
    }


}
