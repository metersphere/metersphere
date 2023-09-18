package io.metersphere.project.service;

import io.metersphere.project.domain.MessageTask;
import io.metersphere.project.domain.MessageTaskExample;
import io.metersphere.project.domain.ProjectRobot;
import io.metersphere.project.domain.ProjectRobotExample;
import io.metersphere.project.enums.ProjectRobotPlatform;
import io.metersphere.project.mapper.MessageTaskMapper;
import io.metersphere.project.mapper.ProjectRobotMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.dto.LogDTO;
import io.metersphere.sdk.dto.request.MessageTaskRequest;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class MessageTaskLogService {

    @Resource
    private ProjectRobotMapper projectRobotMapper;

    @Resource
    private MessageTaskMapper messageTaskMapper;

    /**
     * 添加接口日志
     *
     * @param messageTaskRequest 消息配置参数
     * @return LogDTO
     */
    public LogDTO addLog(MessageTaskRequest messageTaskRequest) {
        MessageTaskExample messageTaskExample = new MessageTaskExample();
        //如果只选了用户，没有选机器人，默认机器人为站内信
        String robotId = setDefaultRobot(messageTaskRequest.getProjectId(), messageTaskRequest.getRobotId());
        messageTaskExample.createCriteria().andProjectIdEqualTo(messageTaskRequest.getProjectId())
                .andProjectRobotIdEqualTo(robotId).andTaskTypeEqualTo(messageTaskRequest.getTaskType()).andEventEqualTo(messageTaskRequest.getEvent());
        List<MessageTask> messageTasks = messageTaskMapper.selectByExample(messageTaskExample);
        LogDTO dto = new LogDTO(
                messageTaskRequest.getProjectId(),
                "",
                messageTaskRequest.getTaskType()+messageTaskRequest.getEvent(),
                null,
                OperationLogType.UPDATE.name(),
                OperationLogModule.PROJECT_MANAGEMENT_MESSAGE,
                messageTaskRequest.getTaskType());

        dto.setPath("/notice/message/task/save");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(messageTasks.get(0)));
        return dto;
    }

    /**
     * 查询默认机器人id
     *
     * @param projectId 项目id
     * @param robotId   机器人id
     * @return String
     */
    private String setDefaultRobot(String projectId, String robotId) {
        if (StringUtils.isBlank(robotId)) {
            ProjectRobotExample projectRobotExample = new ProjectRobotExample();
            projectRobotExample.createCriteria().andProjectIdEqualTo(projectId).andPlatformEqualTo(ProjectRobotPlatform.IN_SITE.toString());
            List<ProjectRobot> projectRobots = projectRobotMapper.selectByExample(projectRobotExample);
            robotId = projectRobots.get(0).getId();
        }
        return robotId;
    }
}
