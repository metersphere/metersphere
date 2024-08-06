package io.metersphere.functional.service;

import io.metersphere.functional.domain.ExportTask;
import io.metersphere.functional.mapper.ExportTaskMapper;
import io.metersphere.sdk.constants.KafkaTopicConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
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

    static Map<String, Future<?>> map = new ConcurrentHashMap<>();
    static boolean isRunning = true;
    public static final String CASE_EXPORT_CONSUME = "case_export_consume";


    public <T> void exportAsyncTask(String userId, T t, Function<Object, Object> selectListFunc) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Future<?> future = executorService.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                // 线程任务逻辑
                LogUtils.info("Thread has been start.");
                selectListFunc.apply(t);
            }
            LogUtils.info("Thread has been interrupted.");
        });
        Thread.sleep(6000);
        ExportTask exportTask = buildExportTask(userId);
        map.put(exportTask.getId(), future);
    }

    private ExportTask buildExportTask(String userId) {
        ExportTask exportTask = new ExportTask();
        exportTask.setId(IDGenerator.nextStr());
        exportTask.setType("CASE");
        exportTask.setCreateUser(userId);
        exportTask.setCreateTime(System.currentTimeMillis());
        exportTask.setState("PREPARED");
        exportTaskMapper.insert(exportTask);
        return exportTask;
    }

    public void sendStopMessage(String id) {
        kafkaTemplate.send(KafkaTopicConstants.CASE_EXPORT, id);
    }

    @KafkaListener(id = CASE_EXPORT_CONSUME, topics = KafkaTopicConstants.CASE_EXPORT, groupId = CASE_EXPORT_CONSUME + "_" + "${random.uuid}")
    public void stop(ConsumerRecord<?, String> record) {
        LogUtils.info("Service consume platform_plugin message: " + record.value());
        String id = JSON.parseObject(record.value(), String.class);
        if (StringUtils.isNotBlank(id)) {
            map.get(id).cancel(true);
            map.remove(id);
            ExportTask exportTask = new ExportTask();
            exportTask.setId(id);
            exportTask.setState("STOP");
            exportTaskMapper.updateByPrimaryKey(exportTask);
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
