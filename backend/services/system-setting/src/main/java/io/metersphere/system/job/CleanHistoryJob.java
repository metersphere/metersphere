package io.metersphere.system.job;


import com.fit2cloud.quartz.anno.QuartzScheduled;
import io.metersphere.sdk.constants.ParamConstants;
import io.metersphere.sdk.domain.OperationLogBlobExample;
import io.metersphere.sdk.mapper.OperationLogBlobMapper;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.domain.SystemParameter;
import io.metersphere.system.mapper.BaseOperationHistoryMapper;
import io.metersphere.system.mapper.BaseOperationLogMapper;
import io.metersphere.system.mapper.SystemParameterMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class CleanHistoryJob {

    @Resource
    private SystemParameterMapper systemParameterMapper;
    @Resource
    private BaseOperationHistoryMapper baseOperationHistoryMapper;
    @Resource
    private BaseOperationLogMapper baseOperationLogMapper;
    @Resource
    private OperationLogBlobMapper operationLogBlobMapper;

    private static final int DEFAULT_LIMIT = 10;

    /**
     * 清理变更历史 每天凌晨两点执行
     */
    @QuartzScheduled(cron = "0 0 2 * * ?")
    public void cleanupLog() {
        LogUtils.info("clean up history start.");
        SystemParameter parameter = systemParameterMapper.selectByPrimaryKey(ParamConstants.CleanConfig.OPERATION_HISTORY.getValue());
        Optional.ofNullable(parameter).ifPresentOrElse(
                p -> {
                    int limit = Integer.parseInt(p.getParamValue());
                    doCleanupHistory(limit);
                },
                () -> {
                    doCleanupHistory(DEFAULT_LIMIT);
                }
        );
        LogUtils.info("clean up log end.");
    }

    private void doCleanupHistory(int limit) {
        //变更历史处理
        List<String> sourceIds = baseOperationHistoryMapper.selectSourceIds();
        int size = 100;
        List<List<String>> batchList = splitList(sourceIds, size);

        batchList.forEach(batch -> cleanupHistory(batch, limit));
    }

    private List<List<String>> splitList(List<String> list, int size) {
        List<List<String>> batchList = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            batchList.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return batchList;
    }

    public void cleanupHistory(List<String> batch, int limit) {
        batch.forEach(sourceId -> {
            List<Long> ids = baseOperationHistoryMapper.selectIdsBySourceId(sourceId, limit);
            baseOperationHistoryMapper.deleteByIds(sourceId, ids);
            List<Long> logIds = baseOperationLogMapper.selectIdByHistoryIds(ids);
            ids.removeAll(logIds);
            if (CollectionUtils.isNotEmpty(ids)) {
                OperationLogBlobExample blobExample = new OperationLogBlobExample();
                blobExample.createCriteria().andIdIn(ids);
                operationLogBlobMapper.deleteByExample(blobExample);
            }
        });
    }
}