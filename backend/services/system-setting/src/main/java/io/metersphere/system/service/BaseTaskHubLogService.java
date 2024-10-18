package io.metersphere.system.service;

import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.system.domain.ExecTask;
import io.metersphere.system.domain.ExecTaskExample;
import io.metersphere.system.domain.ExecTaskItem;
import io.metersphere.system.domain.ExecTaskItemExample;
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
     * 系统批量停止任务日志
     *
     * @param ids
     * @return
     */
    public void systemBatchStopLog(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        ExecTaskExample example = new ExecTaskExample();
        example.createCriteria().andIdIn(ids);
        List<ExecTask> execTasks = execTaskMapper.selectByExample(example);
        List<LogDTO> logDTOList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(execTasks)) {
            execTasks.forEach(item -> {
                LogDTO dto = new LogDTO(
                        OperationLogConstants.SYSTEM,
                        OperationLogConstants.SYSTEM,
                        item.getId(),
                        null,
                        OperationLogType.STOP.name(),
                        OperationLogModule.SETTING_SYSTEM_TASK_CENTER,
                        item.getTaskName());
                logDTOList.add(dto);
            });
        }
        operationLogService.batchAdd(logDTOList);
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
     * 组织批量停止任务日志
     *
     * @param ids
     * @return
     */
    public void orgBatchStopLog(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        ExecTaskExample example = new ExecTaskExample();
        example.createCriteria().andIdIn(ids);
        List<ExecTask> execTasks = execTaskMapper.selectByExample(example);
        List<LogDTO> logDTOList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(execTasks)) {
            execTasks.forEach(item -> {
                LogDTO dto = new LogDTO(
                        OperationLogConstants.ORGANIZATION,
                        null,
                        item.getId(),
                        null,
                        OperationLogType.STOP.name(),
                        OperationLogModule.SETTING_ORGANIZATION_TASK_CENTER,
                        item.getTaskName());
                logDTOList.add(dto);
            });
        }
        operationLogService.batchAdd(logDTOList);
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
     * 项目批量停止任务日志
     *
     * @param ids
     * @return
     */
    public void projectBatchStopLog(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        ExecTaskExample example = new ExecTaskExample();
        example.createCriteria().andIdIn(ids);
        List<ExecTask> execTasks = execTaskMapper.selectByExample(example);
        List<LogDTO> logDTOList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(execTasks)) {
            execTasks.forEach(item -> {
                LogDTO dto = new LogDTO(
                        null,
                        null,
                        item.getId(),
                        null,
                        OperationLogType.STOP.name(),
                        OperationLogModule.PROJECT_MANAGEMENT_TASK_CENTER,
                        item.getTaskName());
                logDTOList.add(dto);
            });
        }
        operationLogService.batchAdd(logDTOList);
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
     * 系统批量删除任务日志
     *
     * @param ids
     * @return
     */
    public void systemBatchDeleteLog(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        ExecTaskExample example = new ExecTaskExample();
        example.createCriteria().andIdIn(ids);
        List<ExecTask> execTasks = execTaskMapper.selectByExample(example);
        List<LogDTO> logDTOList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(execTasks)) {
            execTasks.forEach(item -> {
                LogDTO dto = new LogDTO(
                        OperationLogConstants.SYSTEM,
                        OperationLogConstants.SYSTEM,
                        item.getId(),
                        null,
                        OperationLogType.DELETE.name(),
                        OperationLogModule.SETTING_SYSTEM_TASK_CENTER,
                        item.getTaskName());
                logDTOList.add(dto);
            });
        }
        operationLogService.batchAdd(logDTOList);
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
     * 组织批量删除任务日志
     *
     * @param ids
     * @return
     */
    public void orgBatchDeleteLog(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        ExecTaskExample example = new ExecTaskExample();
        example.createCriteria().andIdIn(ids);
        List<ExecTask> execTasks = execTaskMapper.selectByExample(example);
        List<LogDTO> logDTOList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(execTasks)) {
            execTasks.forEach(item -> {
                LogDTO dto = new LogDTO(
                        OperationLogConstants.ORGANIZATION,
                        null,
                        item.getId(),
                        null,
                        OperationLogType.DELETE.name(),
                        OperationLogModule.SETTING_ORGANIZATION_TASK_CENTER,
                        item.getTaskName());
                logDTOList.add(dto);
            });
        }
        operationLogService.batchAdd(logDTOList);
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
     * 项目批量删除任务日志
     *
     * @param ids
     * @return
     */
    public void projectBatchDeleteLog(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        ExecTaskExample example = new ExecTaskExample();
        example.createCriteria().andIdIn(ids);
        List<ExecTask> execTasks = execTaskMapper.selectByExample(example);
        List<LogDTO> logDTOList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(execTasks)) {
            execTasks.forEach(item -> {
                LogDTO dto = new LogDTO(
                        null,
                        null,
                        item.getId(),
                        null,
                        OperationLogType.DELETE.name(),
                        OperationLogModule.PROJECT_MANAGEMENT_TASK_CENTER,
                        item.getTaskName());
                logDTOList.add(dto);
            });
        }
        operationLogService.batchAdd(logDTOList);
    }


    /**
     * 系统停止任务项日志
     *
     * @param id
     * @return
     */
    public LogDTO systemStopItemLog(String id) {
        ExecTaskItem execTaskItem = execTaskItemMapper.selectByPrimaryKey(id);
        LogDTO dto = null;
        if (execTaskItem != null) {
            dto = new LogDTO(
                    OperationLogConstants.SYSTEM,
                    OperationLogConstants.SYSTEM,
                    execTaskItem.getId(),
                    null,
                    OperationLogType.STOP.name(),
                    OperationLogModule.SETTING_SYSTEM_TASK_CENTER,
                    execTaskItem.getResourceName());
        }
        return dto;
    }


    /**
     * 系统批量停止任务项日志
     *
     * @param ids
     * @return
     */
    public void systemBatchStopItemLog(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        ExecTaskItemExample example = new ExecTaskItemExample();
        example.createCriteria().andIdIn(ids);
        List<ExecTaskItem> execTasks = execTaskItemMapper.selectByExample(example);
        List<LogDTO> logDTOList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(execTasks)) {
            execTasks.forEach(item -> {
                LogDTO dto = new LogDTO(
                        OperationLogConstants.SYSTEM,
                        OperationLogConstants.SYSTEM,
                        item.getId(),
                        null,
                        OperationLogType.STOP.name(),
                        OperationLogModule.SETTING_SYSTEM_TASK_CENTER,
                        item.getResourceName());
                logDTOList.add(dto);
            });
        }
        operationLogService.batchAdd(logDTOList);
    }

}
