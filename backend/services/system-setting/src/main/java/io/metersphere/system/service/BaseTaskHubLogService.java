package io.metersphere.system.service;

import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.ExecTask;
import io.metersphere.system.domain.UserRole;
import io.metersphere.system.dto.sdk.request.UserRoleUpdateRequest;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.mapper.ExecTaskMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author wx
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class BaseTaskHubLogService {
    @Resource
    private ExecTaskMapper execTaskMapper;


    /**
     * 系统停止任务日志
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
     * 系统删除任务日志
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

}
