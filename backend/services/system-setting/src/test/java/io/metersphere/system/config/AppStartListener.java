package io.metersphere.system.config;

import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.uid.impl.DefaultUidGenerator;
import jakarta.annotation.Resource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AppStartListener implements ApplicationRunner {

    @Resource
    private DefaultUidGenerator defaultUidGenerator;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LogUtils.info("================= 应用启动 =================");
        defaultUidGenerator.init();
    }
}
