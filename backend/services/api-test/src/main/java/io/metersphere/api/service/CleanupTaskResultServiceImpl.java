package io.metersphere.api.service;

import io.metersphere.api.domain.ApiReportRelateTaskExample;
import io.metersphere.sdk.constants.ProjectApplicationType;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.system.domain.ExecTask;
import io.metersphere.system.domain.ExecTaskExample;
import io.metersphere.system.domain.ExecTaskItem;
import io.metersphere.system.domain.ExecTaskItemExample;
import io.metersphere.system.mapper.ExecTaskItemMapper;
import io.metersphere.system.mapper.ExecTaskMapper;
import io.metersphere.system.mapper.ExtExecTaskItemMapper;
import io.metersphere.system.mapper.ExtExecTaskMapper;
import io.metersphere.system.service.BaseCleanUpReport;
import org.apache.commons.collections4.ListUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import io.metersphere.api.mapper.ApiReportRelateTaskMapper;

import java.util.List;
import java.util.Map;

import static io.metersphere.sdk.util.ShareUtil.getCleanDate;

/**
 * @author song-cc-rock
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class CleanupTaskResultServiceImpl implements BaseCleanUpReport {

    @Resource
    private ExtExecTaskMapper extExecTaskMapper;
    @Resource
    private ExtExecTaskItemMapper extExecTaskItemMapper;
    @Resource
    private ExecTaskMapper execTaskMapper;
    @Resource
    private ApiReportRelateTaskMapper apiReportRelateTaskMapper;
    @Resource
    private ExecTaskItemMapper execTaskItemMapper;


    @Override
    public void cleanReport(Map<String, String> map, String projectId) {
        LogUtils.info("清理当前项目[" + projectId + "]任务中心执行结果");
        String expr = map.get(ProjectApplicationType.TASK.TASK_CLEAN_REPORT.name());
        long timeMills = getCleanDate(expr);
        List<String> cleanTaskIds = extExecTaskMapper.getTaskIdsByTime(timeMills, projectId);
        List<String> cleanTaskItemIds = extExecTaskItemMapper.getTaskItemIdsByTime(timeMills, projectId);
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
        List<String> cleanIds = ListUtils.union(cleanTaskIds, cleanTaskItemIds);
        LogUtils.info("清理当前项目[" + projectId + "]任务中心执行结果, 共[" + cleanIds.size() + "]条");
        if (CollectionUtils.isNotEmpty(cleanIds)) {
            // 清理任务-报告关系表
            SubListUtils.dealForSubList(cleanIds, 100, ids -> {
                ApiReportRelateTaskExample example = new ApiReportRelateTaskExample();
                example.createCriteria().andTaskResourceIdIn(cleanIds);
                apiReportRelateTaskMapper.deleteByExample(example);
            });
        }
        LogUtils.info("清理当前项目[" + projectId + "]任务中心执行结果结束！");
    }
}
