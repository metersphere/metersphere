package io.metersphere.listener;

import io.metersphere.functional.domain.ExportTask;
import io.metersphere.functional.domain.ExportTaskExample;
import io.metersphere.functional.mapper.ExportTaskMapper;
import io.metersphere.sdk.constants.StorageType;
import io.metersphere.sdk.file.FileCenter;
import io.metersphere.sdk.file.MinioRepository;
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
        // 加载插件
        pluginLoadService.loadPlugins();
    }
}
