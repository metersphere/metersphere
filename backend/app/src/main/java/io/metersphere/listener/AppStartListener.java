package io.metersphere.listener;

import io.metersphere.api.event.ApiEventSource;
import io.metersphere.plan.listener.ExecEventListener;
import io.metersphere.sdk.constants.StorageType;
import io.metersphere.sdk.file.FileCenter;
import io.metersphere.sdk.file.MinioRepository;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.service.PluginLoadService;
import io.minio.MinioClient;
import jakarta.annotation.Resource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AppStartListener implements ApplicationRunner {

    @Resource
    private PluginLoadService pluginLoadService;
    @Resource
    private MinioClient minioClient;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LogUtils.info("================= 应用启动 =================");
        // 初始化MinIO配置
        ((MinioRepository) FileCenter.getRepository(StorageType.MINIO)).init(minioClient);

        // 注册所有监听源
        LogUtils.info("初始化接口事件源");
        ApiEventSource apiEventSource = CommonBeanFactory.getBean(ApiEventSource.class);
        LogUtils.info("初始化用例管理事件源");
        LogUtils.info("初始化性能测试事件源");
        //LoadEventSource loadEventSource = CommonBeanFactory.getBean(LoadEventSource.class);
        //todo: 注册其他事件源

        // 创建监听器对象并注册到多个事件源
        ExecEventListener listener = CommonBeanFactory.getBean(ExecEventListener.class);
        apiEventSource.addListener(listener);

        //todo: 注册其他监听器
        //loadEventSource.addListener(listener);

        // 触发事件
        apiEventSource.fireEvent("API", "Event after removing the listener test.");
        //loadEventSource.fireEvent("LOAD","Event after removing the listener.");
        // 加载插件
        pluginLoadService.loadPlugins();
    }
}
