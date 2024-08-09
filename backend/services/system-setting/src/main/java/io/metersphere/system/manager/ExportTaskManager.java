package io.metersphere.system.manager;

import io.metersphere.functional.domain.ExportTask;
import io.metersphere.functional.mapper.ExportTaskMapper;
import io.metersphere.sdk.constants.KafkaTopicConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.constants.ExportConstants;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

@Service
public class ExportTaskManager {

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;
    @Resource
    private ExportTaskMapper exportTaskMapper;

    public static Map<String, Future<?>> map = new ConcurrentHashMap<>();
    public static final String EXPORT_CONSUME = "export_consume";


    public <T> ExportTask exportAsyncTask(String projectId, String fileId, String userId, String type, T t, Function<Object, Object> selectListFunc) throws InterruptedException {
        ExportTask exportTask = buildExportTask(projectId, fileId, userId, type);
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Future<?> future = executorService.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                // 线程任务逻辑
                LogUtils.info("Thread has been start.");
                selectListFunc.apply(t);
            }
            LogUtils.info("Thread has been interrupted.");
        });
        map.put(exportTask.getId(), future);
        return exportTask;
    }

    public ExportTask buildExportTask(String projectId, String fileId, String userId, String type) {
        ExportTask exportTask = new ExportTask();
        exportTask.setId(IDGenerator.nextStr());
        exportTask.setType(type);
        exportTask.setCreateUser(userId);
        exportTask.setCreateTime(System.currentTimeMillis());
        exportTask.setState(ExportConstants.ExportState.PREPARED.toString());
        exportTask.setUpdateUser(userId);
        exportTask.setUpdateTime(System.currentTimeMillis());
        exportTask.setProjectId(projectId);
        exportTask.setFileId(fileId);
        exportTaskMapper.insertSelective(exportTask);
        return exportTask;
    }

    public void sendStopMessage(String id, String userId) {
        ExportTask exportTask = new ExportTask();
        exportTask.setId(id);
        exportTask.setState(ExportConstants.ExportState.STOP.toString());
        exportTask.setUpdateUser(userId);
        exportTask.setUpdateTime(System.currentTimeMillis());
        kafkaTemplate.send(KafkaTopicConstants.EXPORT, JSON.toJSONString(exportTask));
    }

    @KafkaListener(id = EXPORT_CONSUME, topics = KafkaTopicConstants.EXPORT, groupId = EXPORT_CONSUME + "_" + "${random.uuid}")
    public void stop(ConsumerRecord<?, String> record) {
        LogUtils.info("Service consume platform_plugin message: " + record.value());
        ExportTask exportTask = JSON.parseObject(record.value(), ExportTask.class);
        if (exportTask != null && StringUtils.isNotBlank(exportTask.getId())) {
            String id = exportTask.getId();
            if (map.containsKey(id)) {
                map.get(id).cancel(true);
                map.remove(id);
            }
            exportTaskMapper.updateByPrimaryKeySelective(exportTask);
        }
    }

    @Scheduled(fixedDelay = 10000)
    public void checkStop() {
        for (String next : map.keySet()) {
            if (map.get(next) != null && map.get(next).isDone()) {
                map.remove(next);
            }
        }
    }
}
