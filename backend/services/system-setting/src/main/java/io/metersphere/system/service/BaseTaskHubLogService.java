package io.metersphere.system.service;

import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.system.domain.ExecTask;
import io.metersphere.system.domain.ExecTaskExample;
import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
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
    private BaseTaskHubService baseTaskHubService;

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
     * @param request
     * @return
     */
    public List<LogDTO> systemBatchStopLog(TableBatchProcessDTO request) {
        List<String> ids = baseTaskHubService.getTaskIds(request);
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
        return logDTOList;
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
     * @param request
     * @return
     */
    public List<LogDTO> orgBatchStopLog(TableBatchProcessDTO request) {
        List<String> ids = baseTaskHubService.getTaskIds(request);
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
        return logDTOList;
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
     * @param request
     * @return
     */
    public List<LogDTO> projectBatchStopLog(TableBatchProcessDTO request) {
        List<String> ids = baseTaskHubService.getTaskIds(request);
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
        return logDTOList;
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
     * @param request
     * @return
     */
    public List<LogDTO> systemBatchDeleteLog(TableBatchProcessDTO request) {
        List<String> ids = baseTaskHubService.getTaskIds(request);
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
        return logDTOList;
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
     * @param request
     * @return
     */
    public List<LogDTO> orgBatchDeleteLog(TableBatchProcessDTO request) {
        List<String> ids = baseTaskHubService.getTaskIds(request);
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
        return logDTOList;
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
     * @param request
     * @return
     */
    public List<LogDTO> projectBatchDeleteLog(TableBatchProcessDTO request) {
        List<String> ids = baseTaskHubService.getTaskIds(request);
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
        return logDTOList;
    }
}
