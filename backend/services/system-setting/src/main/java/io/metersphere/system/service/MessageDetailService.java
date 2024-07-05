package io.metersphere.system.service;

import io.metersphere.project.domain.*;
import io.metersphere.project.mapper.MessageTaskBlobMapper;
import io.metersphere.project.mapper.MessageTaskMapper;
import io.metersphere.project.mapper.ProjectRobotMapper;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.notice.MessageDetail;
import io.metersphere.system.notice.utils.MessageTemplateUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author guoyuqi
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class MessageDetailService {

    @Resource
    private MessageTaskMapper messageTaskMapper;
    @Resource
    private MessageTaskBlobMapper messageTaskBlobMapper;
    @Resource
    private ProjectRobotMapper projectRobotMapper;

    /**
     * 获取唯一的消息任务
     *
     * @param taskType  任务类型
     * @param projectId 项目ID
     * @return List<MessageDetail>list
     */
    public List<MessageDetail> searchMessageByTypeAndProjectId(String taskType, String projectId) {
        try {
            return getMessageDetails(taskType, projectId);
        } catch (Exception e) {
            LogUtils.error(e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    private List<MessageDetail> getMessageDetails(String type, String projectId) {
        List<MessageDetail> messageDetails = new ArrayList<>();
        //根据项目id找到所有开启的消息通知任务 以及模版配置，机器人配置
        MessageTaskExample example = new MessageTaskExample();
        example.createCriteria()
                .andTaskTypeEqualTo(type)
                .andProjectIdEqualTo(projectId).andEnableEqualTo(true);
        example.setOrderByClause("create_time asc");
        List<MessageTask> messageTaskLists = messageTaskMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(messageTaskLists)) {
            return new ArrayList<>();
        }
        getMessageDetails(messageDetails, messageTaskLists);
        return messageDetails.stream()
                .sorted(Comparator.comparing(MessageDetail::getCreateTime, Comparator.nullsLast(Long::compareTo)).reversed())
                .distinct()
                .collect(Collectors.toList());
    }

    private void getMessageDetails(List<MessageDetail> messageDetails, List<MessageTask> messageTaskLists) {
        List<String> messageTaskIds = messageTaskLists.stream().map(MessageTask::getId).collect(Collectors.toList());
        MessageTaskBlobExample blobExample = new MessageTaskBlobExample();
        blobExample.createCriteria()
                .andIdIn(messageTaskIds);
        List<MessageTaskBlob> messageTaskBlobs = messageTaskBlobMapper.selectByExampleWithBLOBs(blobExample);
        Map<String, MessageTaskBlob> messageTaskBlobMap = messageTaskBlobs.stream().collect(Collectors.toMap(MessageTaskBlob::getId, item -> item));

        List<String> robotIds = messageTaskLists.stream().map(MessageTask::getProjectRobotId).distinct().collect(Collectors.toList());
        ProjectRobotExample projectRobotExample = new ProjectRobotExample();
        projectRobotExample.createCriteria().andIdIn(robotIds).andEnableEqualTo(true);
        List<ProjectRobot> projectRobots = projectRobotMapper.selectByExample(projectRobotExample);
        Map<String, ProjectRobot> projectRobotMap = projectRobots.stream().collect(Collectors.toMap(ProjectRobot::getId, item -> item));

        //消息通知任务以消息类型事件机器人唯一进行分组
        Map<String, List<MessageTask>> messageTaskGroup = messageTaskLists.stream().collect(Collectors.groupingBy(t -> (t.getTaskType() + t.getEvent() + t.getProjectRobotId())));
        messageTaskGroup.forEach((messageTaskId, messageTaskList) -> {
            //获取同一任务所有的接收人
            MessageDetail messageDetail = new MessageDetail();
            MessageTask messageTask = messageTaskList.getFirst();
            messageDetail.setReceiverIds(messageTask.getReceivers());
            messageDetail.setTaskType(messageTask.getTaskType());
            messageDetail.setEvent(messageTask.getEvent());
            messageDetail.setCreateTime(messageTask.getCreateTime());
            messageDetail.setProjectId(messageTask.getProjectId());
            String projectRobotId = messageTask.getProjectRobotId();
            ProjectRobot projectRobot = projectRobotMap.get(projectRobotId);
            //如果当前机器人停止，那么当前任务也失效
            if (projectRobot == null) {
                return;
            }
            messageDetail.setType(projectRobot.getPlatform());
            if (StringUtils.isNotBlank(projectRobot.getType())) {
                messageDetail.setDingType(projectRobot.getType());
            }
            if (StringUtils.isNotBlank(projectRobot.getWebhook())) {
                messageDetail.setWebhook(projectRobot.getWebhook());
            }
            if (StringUtils.isNotBlank(messageTask.getTestId())) {
                messageDetail.setTestId(messageTask.getTestId());
            }
            if (StringUtils.isNotBlank(projectRobot.getAppKey())) {
                messageDetail.setAppKey(projectRobot.getAppKey());
            }
            if (StringUtils.isNotBlank(projectRobot.getAppSecret())) {
                messageDetail.setAppSecret(projectRobot.getAppSecret());
            }
            if (!messageTask.getUseDefaultSubject() && StringUtils.isNotBlank(messageTask.getSubject())) {
                messageDetail.setSubject(messageTask.getSubject());
            } else {
                if (StringUtils.equals(projectRobot.getPlatform(),"MAIL")) {
                    String subject = getMailSubject(messageTask.getTaskType(), messageTask.getEvent());
                    messageDetail.setSubject(subject);
                } else {
                    String subject = getSubject(messageTask.getTaskType(), messageTask.getEvent());
                    messageDetail.setSubject(subject);
                }
            }
            MessageTaskBlob messageTaskBlob = messageTaskBlobMap.get(messageTask.getId());
            if (!messageTask.getUseDefaultTemplate() && StringUtils.isNotBlank(messageTaskBlob.getTemplate())) {
                messageDetail.setTemplate(messageTaskBlob.getTemplate());
            } else {
                String template = getTemplate(messageTask.getTaskType(), messageTask.getEvent());
                messageDetail.setTemplate(template);
            }
            messageDetails.add(messageDetail);
        });
    }

    private String getTemplate(String taskType, String event) {
        Map<String, String> defaultTemplateMap = MessageTemplateUtils.getDefaultTemplateMap();
        return defaultTemplateMap.get(taskType + "_" + event);
    }

    private String getMailSubject(String taskType, String event) {
        Map<String, String> defaultTemplateTitleMap = MessageTemplateUtils.getDefaultTemplateSubjectMap();
        return "MeterSphere " + defaultTemplateTitleMap.get(taskType + "_" + event);
    }

    private String getSubject(String taskType, String event) {
        Map<String, String> defaultTemplateTitleMap = MessageTemplateUtils.getDefaultTemplateSubjectMap();
        return defaultTemplateTitleMap.get(taskType + "_" + event);
    }
    /**
     * 根据用例ID获取所有该用例的定时任务的任务通知
     *
     * @param testId 用例id
     * @return List<MessageDetail>
     */
    public List<MessageDetail> searchMessageByTestId(String testId) {
        MessageTaskExample example = new MessageTaskExample();
        example.createCriteria().andTestIdEqualTo(testId);
        List<MessageTask> messageTaskLists = messageTaskMapper.selectByExample(example);
        List<MessageDetail> scheduleMessageTask = new ArrayList<>();
        getMessageDetails(scheduleMessageTask, messageTaskLists);
        scheduleMessageTask.sort(Comparator.comparing(MessageDetail::getCreateTime, Comparator.nullsLast(Long::compareTo)).reversed());
        return scheduleMessageTask;
    }
}
