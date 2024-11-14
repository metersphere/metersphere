package io.metersphere.system.service;

import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.system.domain.ExecTask;
import io.metersphere.system.domain.ExecTaskExample;
import io.metersphere.system.domain.ExecTaskItem;
import io.metersphere.system.domain.ExecTaskItemExample;
import io.metersphere.system.dto.builder.LogDTOBuilder;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import io.metersphere.system.mapper.ExecTaskItemMapper;
import io.metersphere.system.mapper.ExecTaskMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wx
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class BaseTaskHubLogService {
    @Resource
    private ExecTaskMapper execTaskMapper;
    @Resource
    private OperationLogService operationLogService;
    @Resource
    private ExecTaskItemMapper execTaskItemMapper;

    /**
     * 系统停止任务日志
     *
     * @param id
     * @return
     */
    public LogDTO systemStopLog(String id) {
        ExecTask execTask = execTaskMapper.selectByPrimaryKey(id);
        LogDTO dto = null;
        if (execTask != null) {
            dto = new LogDTO(
                    OperationLogConstants.SYSTEM,
                    OperationLogConstants.SYSTEM,
                    execTask.getId(),
                    null,
                    OperationLogType.STOP.name(),
                    OperationLogModule.SETTING_SYSTEM_TASK_CENTER,
                    execTask.getTaskName());
        }
        return dto;
    }


    /**
     * 组织停止任务日志
     *
     * @param id
     * @return
     */
    public LogDTO orgStopLog(String id) {
        ExecTask execTask = execTaskMapper.selectByPrimaryKey(id);
        LogDTO dto = null;
        if (execTask != null) {
            dto = new LogDTO(
                    OperationLogConstants.ORGANIZATION,
                    null,
                    execTask.getId(),
                    null,
                    OperationLogType.STOP.name(),
                    OperationLogModule.SETTING_ORGANIZATION_TASK_CENTER,
                    execTask.getTaskName());
        }
        return dto;
    }

    /**
     * 项目停止任务日志
     *
     * @param id
     * @return
     */
    public LogDTO projectStopLog(String id) {
        ExecTask execTask = execTaskMapper.selectByPrimaryKey(id);
        LogDTO dto = null;
        if (execTask != null) {
            dto = new LogDTO(
                    null,
                    null,
                    execTask.getId(),
                    null,
                    OperationLogType.STOP.name(),
                    OperationLogModule.PROJECT_MANAGEMENT_TASK_CENTER,
                    execTask.getTaskName());
        }
        return dto;
    }

    /**
     * 系统重跑任务日志
     *
     * @param id
     * @return
     */
    public LogDTO systemRerunLog(String id) {
        ExecTask execTask = execTaskMapper.selectByPrimaryKey(id);
        LogDTO dto = null;
        if (execTask != null) {
            dto = new LogDTO(
                    OperationLogConstants.SYSTEM,
                    OperationLogConstants.SYSTEM,
                    execTask.getId(),
                    null,
                    OperationLogType.RERUN.name(),
                    OperationLogModule.SETTING_SYSTEM_TASK_CENTER,
                    execTask.getTaskName());
        }
        return dto;
    }

    /**
     * 组织重跑任务日志
     *
     * @param id
     * @return
     */
    public LogDTO orgRerunLog(String id) {
        ExecTask execTask = execTaskMapper.selectByPrimaryKey(id);
        LogDTO dto = null;
        if (execTask != null) {
            dto = new LogDTO(
                    OperationLogConstants.ORGANIZATION,
                    null,
                    execTask.getId(),
                    null,
                    OperationLogType.RERUN.name(),
                    OperationLogModule.SETTING_ORGANIZATION_TASK_CENTER,
                    execTask.getTaskName());
        }
        return dto;
    }

    /**
     * 项目重跑任务日志
     *
     * @param id
     * @return
     */
    public LogDTO projectRerunLog(String id) {
        ExecTask execTask = execTaskMapper.selectByPrimaryKey(id);
        LogDTO dto = null;
        if (execTask != null) {
            dto = new LogDTO(
                    null,
                    null,
                    execTask.getId(),
                    null,
                    OperationLogType.RERUN.name(),
                    OperationLogModule.PROJECT_MANAGEMENT_TASK_CENTER,
                    execTask.getTaskName());
        }
        return dto;
    }

    /**
     * 系统删除任务日志
     *
     * @param id
     * @return
     */
    public LogDTO systemDeleteLog(String id) {
        ExecTask execTask = execTaskMapper.selectByPrimaryKey(id);
        LogDTO dto = null;
        if (execTask != null) {
            dto = new LogDTO(
                    OperationLogConstants.SYSTEM,
                    OperationLogConstants.SYSTEM,
                    execTask.getId(),
                    null,
                    OperationLogType.DELETE.name(),
                    OperationLogModule.SETTING_SYSTEM_TASK_CENTER,
                    execTask.getTaskName());
        }
        return dto;
    }


    /**
     * 组织删除任务日志
     *
     * @param id
     * @return
     */
    public LogDTO orgDeleteLog(String id) {
        ExecTask execTask = execTaskMapper.selectByPrimaryKey(id);
        LogDTO dto = null;
        if (execTask != null) {
            dto = new LogDTO(
                    OperationLogConstants.ORGANIZATION,
                    null,
                    execTask.getId(),
                    null,
                    OperationLogType.DELETE.name(),
                    OperationLogModule.SETTING_ORGANIZATION_TASK_CENTER,
                    execTask.getTaskName());
        }
        return dto;
    }

    /**
     * 项目删除任务日志
     *
     * @param id
     * @return
     */
    public LogDTO projectDeleteLog(String id) {
        ExecTask execTask = execTaskMapper.selectByPrimaryKey(id);
        LogDTO dto = null;
        if (execTask != null) {
            dto = new LogDTO(
                    null,
                    null,
                    execTask.getId(),
                    null,
                    OperationLogType.DELETE.name(),
                    OperationLogModule.PROJECT_MANAGEMENT_TASK_CENTER,
                    execTask.getTaskName());
        }
        return dto;
    }


    /**
     * 任务项 批量操作日志统一记录
     *
     * @param ids
     * @param userId
     * @param operationType
     * @param projectId
     * @param organizationId
     * @param url
     * @param module
     */
    public void taskItemBatchLog(List<String> ids, String userId, String operationType, String projectId, String organizationId, String url, String module) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        ExecTaskItemExample example = new ExecTaskItemExample();
        example.createCriteria().andIdIn(ids);
        List<ExecTaskItem> execTaskItems = execTaskItemMapper.selectByExample(example);
        List<LogDTO> logDTOList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(execTaskItems)) {
            execTaskItems.forEach(item -> {
                LogDTO dto = LogDTOBuilder.builder()
                        .projectId(projectId)
                        .organizationId(organizationId)
                        .type(operationType)
                        .module(module)
                        .method(HttpMethodConstants.POST.name())
                        .path(url)
                        .sourceId(item.getId())
                        .content(item.getResourceName())
                        .createUser(userId)
                        .build().getLogDTO();
                logDTOList.add(dto);
            });
        }
        operationLogService.batchAdd(logDTOList);
    }


    /**
     * 任务批量 操作日志统一处理
     *
     * @param ids
     * @param userId
     * @param operationType
     * @param projectId
     * @param organizationId
     * @param url
     * @param module
     */
    public void taskBatchLog(List<String> ids, String userId, String operationType, String projectId, String organizationId, String url, String module) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        ExecTaskExample example = new ExecTaskExample();
        example.createCriteria().andIdIn(ids);
        List<ExecTask> execTasks = execTaskMapper.selectByExample(example);
        List<LogDTO> logDTOList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(execTasks)) {
            execTasks.forEach(item -> {
                LogDTO dto = LogDTOBuilder.builder()
                        .projectId(projectId)
                        .organizationId(organizationId)
                        .type(operationType)
                        .module(module)
                        .method(HttpMethodConstants.POST.name())
                        .path(url)
                        .sourceId(item.getId())
                        .content(item.getTaskName())
                        .createUser(userId)
                        .build().getLogDTO();
                logDTOList.add(dto);
            });
        }
        operationLogService.batchAdd(logDTOList);
    }
}
