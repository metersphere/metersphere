package io.metersphere.listener;

import io.metersphere.api.event.ApiEventSource;
import io.metersphere.functional.domain.ExportTask;
import io.metersphere.functional.domain.ExportTaskExample;
import io.metersphere.functional.mapper.ExportTaskMapper;
import io.metersphere.plan.listener.ExecEventListener;
import io.metersphere.sdk.constants.StorageType;
import io.metersphere.sdk.file.FileCenter;
import io.metersphere.sdk.file.MinioRepository;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.constants.ExportConstants;
import io.metersphere.system.service.BaseScheduleService;
import io.metersphere.system.service.PluginLoadService;
import io.metersphere.system.uid.impl.DefaultUidGenerator;
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

    @Resource
    private BaseScheduleService baseScheduleService;

    @Resource
    private DefaultUidGenerator defaultUidGenerator;

    @Resource
    private ExportTaskMapper exportTaskMapper;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LogUtils.info("================= 应用启动 =================");
        defaultUidGenerator.init();
        // 初始化MinIO配置
        ((MinioRepository) FileCenter.getRepository(StorageType.MINIO)).init(minioClient);

        LogUtils.info("初始化定时任务");
        baseScheduleService.startEnableSchedules();

        LogUtils.info("初始化导出未完成任务的状态");
        ExportTaskExample exportTaskExample = new ExportTaskExample();
        exportTaskExample.createCriteria().andStateEqualTo(ExportConstants.ExportState.PREPARED.name());
        ExportTask exportTask = new ExportTask();
        exportTask.setState(ExportConstants.ExportState.STOP.name());
        exportTaskMapper.updateByExampleSelective(exportTask, exportTaskExample);

        // 注册所有监听源
        LogUtils.info("初始化接口事件源");
        ApiEventSource apiEventSource = CommonBeanFactory.getBean(ApiEventSource.class);
        LogUtils.info("初始化用例管理事件源");
        //todo: 注册其他事件源

        // 创建监听器对象并注册到多个事件源
        ExecEventListener listener = CommonBeanFactory.getBean(ExecEventListener.class);
        apiEventSource.addListener(listener);

        //todo: 注册其他监听器

        // 触发事件
        apiEventSource.fireEvent("API", "Event after removing the listener test.");
        // 加载插件
        pluginLoadService.loadPlugins();
    }
}
