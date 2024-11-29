package io.metersphere.api.service;

import io.metersphere.sdk.constants.ProjectApplicationType;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.domain.ExecTask;
import io.metersphere.system.domain.ExecTaskExample;
import io.metersphere.system.domain.ExecTaskItem;
import io.metersphere.system.domain.ExecTaskItemExample;
import io.metersphere.system.mapper.ExecTaskItemMapper;
import io.metersphere.system.mapper.ExecTaskMapper;
import io.metersphere.system.mapper.ExtExecTaskMapper;
import io.metersphere.system.service.BaseCleanUpReport;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.metersphere.sdk.util.ShareUtil.getCleanDate;

/**
 * @author song-cc-rock
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class CleanupTaskServiceImpl implements BaseCleanUpReport {

    @Resource
    private ExtExecTaskMapper extExecTaskMapper;
    @Resource
    private ExecTaskMapper execTaskMapper;
    @Resource
    private ExecTaskItemMapper execTaskItemMapper;


    @Override
    public void cleanReport(Map<String, String> map, String projectId) {
        LogUtils.info("清理当前项目[" + projectId + "]即时任务");
        String expr = map.get(ProjectApplicationType.TASK.TASK_RECORD.name());
        long timeMills = getCleanDate(expr);
        List<String> cleanTaskIds = extExecTaskMapper.getTaskIdsByTime(timeMills, projectId);
        List<ExecTaskItem> execTaskItems = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(cleanTaskIds)) {
            ExecTaskItemExample itemExample = new ExecTaskItemExample();
            itemExample.createCriteria().andTaskIdIn(cleanTaskIds);
            execTaskItems = execTaskItemMapper.selectByExample(itemExample);
        }
        List<String> cleanTaskItemIds = execTaskItems.stream().map(ExecTaskItem::getId).toList();
        LogUtils.info("清理当前项目[" + projectId + "]即时任务, 共[" + (cleanTaskIds.size() + cleanTaskItemIds.size()) + "]条");
        if (CollectionUtils.isNotEmpty(cleanTaskIds)) {
            ExecTaskExample example = new ExecTaskExample();
            example.createCriteria().andIdIn(cleanTaskIds);
            ExecTask execTask = new ExecTask();
            execTask.setDeleted(true);
            execTaskMapper.updateByExampleSelective(execTask, example);
        }
        if (CollectionUtils.isNotEmpty(cleanTaskItemIds)) {
            ExecTaskItemExample example = new ExecTaskItemExample();
            example.createCriteria().andIdIn(cleanTaskItemIds);
            ExecTaskItem execTaskItem = new ExecTaskItem();
            execTaskItem.setDeleted(true);
            execTaskItemMapper.updateByExampleSelective(execTaskItem, example);
        }
        LogUtils.info("清理当前项目[" + projectId + "]即时任务结束!");
    }
}
