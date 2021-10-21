package io.metersphere.notice.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.base.domain.MessageTask;
import io.metersphere.base.domain.MessageTaskExample;
import io.metersphere.base.domain.Project;
import io.metersphere.base.mapper.MessageTaskMapper;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.StatusReference;
import io.metersphere.log.vo.system.SystemReference;
import io.metersphere.notice.domain.MessageDetail;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class NoticeService {
    @Resource
    private MessageTaskMapper messageTaskMapper;
    @Resource
    private ProjectMapper projectMapper;

    public void saveMessageTask(MessageDetail messageDetail) {
        MessageTaskExample example = new MessageTaskExample();
        example.createCriteria().andIdentificationEqualTo(messageDetail.getIdentification());
        List<MessageTask> messageTaskLists = messageTaskMapper.selectByExample(example);
        if (messageTaskLists.size() > 0) {
            delMessage(messageDetail.getIdentification());
        }
        String workspaceId = SessionUtils.getCurrentWorkspaceId();
        long time = System.currentTimeMillis();
        String identification = messageDetail.getIdentification();
        if (StringUtils.isBlank(identification)) {
            identification = UUID.randomUUID().toString();
        }
        for (String userId : messageDetail.getUserIds()) {
            checkUserIdExist(userId, messageDetail, workspaceId);
            MessageTask messageTask = new MessageTask();
            messageTask.setId(UUID.randomUUID().toString());
            messageTask.setEvent(messageDetail.getEvent());
            messageTask.setTaskType(messageDetail.getTaskType());
            messageTask.setUserId(userId);
            messageTask.setType(messageDetail.getType());
            messageTask.setWebhook(messageDetail.getWebhook());
            messageTask.setIdentification(identification);
            messageTask.setIsSet(false);
            messageTask.setWorkspaceId(workspaceId);
            messageTask.setTestId(messageDetail.getTestId());
            messageTask.setCreateTime(time);
            setTemplate(messageDetail, messageTask);
            messageDetail.setId(messageTask.getId());
            messageTaskMapper.insert(messageTask);
        }
    }

    private void setTemplate(MessageDetail messageDetail, MessageTask messageTask) {
        if (StringUtils.isNotBlank(messageDetail.getTemplate())) {
            messageTask.setTemplate(messageDetail.getTemplate());
        }
    }

    private void checkUserIdExist(String userId, MessageDetail list, String workspaceId) {
        MessageTaskExample example = new MessageTaskExample();
        if (StringUtils.isBlank(list.getTestId())) {
            example.createCriteria()
                    .andUserIdEqualTo(userId)
                    .andEventEqualTo(list.getEvent())
                    .andTypeEqualTo(list.getType())
                    .andTaskTypeEqualTo(list.getTaskType())
                    .andWebhookEqualTo(list.getWebhook())
                    .andWorkspaceIdEqualTo(workspaceId);
        } else {
            example.createCriteria()
                    .andUserIdEqualTo(userId)
                    .andEventEqualTo(list.getEvent())
                    .andTypeEqualTo(list.getType())
                    .andTaskTypeEqualTo(list.getTaskType())
                    .andWebhookEqualTo(list.getWebhook())
                    .andTestIdEqualTo(list.getTestId())
                    .andWorkspaceIdEqualTo(workspaceId);
        }
        if (messageTaskMapper.countByExample(example) > 0) {
            MSException.throwException(Translator.get("message_task_already_exists"));
        }
    }

    public List<MessageDetail> searchMessageByTestId(String testId) {
        MessageTaskExample example = new MessageTaskExample();
        example.createCriteria().andTestIdEqualTo(testId);
        List<MessageTask> messageTaskLists = messageTaskMapper.selectByExampleWithBLOBs(example);
        List<MessageDetail> scheduleMessageTask = new ArrayList<>();
        Map<String, List<MessageTask>> MessageTaskMap = messageTaskLists.stream().collect(Collectors.groupingBy(MessageTask::getIdentification));
        MessageTaskMap.forEach((k, v) -> {
            MessageDetail messageDetail = getMessageDetail(v);
            scheduleMessageTask.add(messageDetail);
        });
        scheduleMessageTask.sort(Comparator.comparing(MessageDetail::getCreateTime, Comparator.nullsLast(Long::compareTo)).reversed());
        return scheduleMessageTask;
    }

    public List<MessageDetail> searchMessageByType(String type) {
        try {
            String workspaceId = SessionUtils.getCurrentWorkspaceId();
            return getMessageDetails(type, workspaceId);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            return new ArrayList<>();
        }
    }


    public List<MessageDetail> searchMessageByTypeAndWorkspaceId(String type, String workspaceId) {
        try {
            return getMessageDetails(type, workspaceId);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<MessageDetail> searchMessageByTypeBySend(String type, String projectId) {
        try {
            String workspaceId = "";
            if (null == SessionUtils.getUser()) {
                Project project = projectMapper.selectByPrimaryKey(projectId);
                workspaceId = project.getWorkspaceId();
            } else {
                workspaceId = SessionUtils.getCurrentWorkspaceId();
            }
            return getMessageDetails(type, workspaceId);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    private List<MessageDetail> getMessageDetails(String type, String workspaceId) {
        List<MessageDetail> messageDetails = new ArrayList<>();

        MessageTaskExample example = new MessageTaskExample();
        example.createCriteria()
                .andTaskTypeEqualTo(type)
                .andWorkspaceIdEqualTo(workspaceId);
        List<MessageTask> messageTaskLists = messageTaskMapper.selectByExampleWithBLOBs(example);

        Map<String, List<MessageTask>> messageTaskMap = messageTaskLists.stream()
                .collect(Collectors.groupingBy(NoticeService::fetchGroupKey));
        messageTaskMap.forEach((k, v) -> {
            MessageDetail messageDetail = getMessageDetail(v);
            messageDetails.add(messageDetail);
        });

        return messageDetails.stream()
                .sorted(Comparator.comparing(MessageDetail::getCreateTime, Comparator.nullsLast(Long::compareTo)).reversed())
                .collect(Collectors.toList())
                .stream()
                .distinct()
                .collect(Collectors.toList());
    }

    private MessageDetail getMessageDetail(List<MessageTask> messageTasks) {
        Set<String> userIds = new HashSet<>();

        MessageDetail messageDetail = new MessageDetail();
        for (MessageTask m : messageTasks) {
            userIds.add(m.getUserId());
            messageDetail.setEvent(m.getEvent());
            messageDetail.setTaskType(m.getTaskType());
            messageDetail.setWebhook(m.getWebhook());
            messageDetail.setIdentification(m.getIdentification());
            messageDetail.setType(m.getType());
            messageDetail.setIsSet(m.getIsSet());
            messageDetail.setCreateTime(m.getCreateTime());
            messageDetail.setTemplate(m.getTemplate());
        }
        if (CollectionUtils.isNotEmpty(userIds)) {
            messageDetail.setUserIds(new ArrayList<>(userIds));
        }
        return messageDetail;
    }

    private static String fetchGroupKey(MessageTask messageTask) {
        return messageTask.getTaskType() + "#" + messageTask.getIdentification();
    }

    public int delMessage(String identification) {
        MessageTaskExample example = new MessageTaskExample();
        example.createCriteria().andIdentificationEqualTo(identification);
        return messageTaskMapper.deleteByExample(example);
    }

    public String getLogDetails(String id) {
        MessageTask task = messageTaskMapper.selectByPrimaryKey(id);
        if (task == null) {
            try {
                MessageTaskExample example = new MessageTaskExample();
                example.createCriteria().andIdentificationEqualTo(id);
                List<MessageTask> tasks = messageTaskMapper.selectByExample(example);
                List<String> names = tasks.stream().map(MessageTask::getType).collect(Collectors.toList());
                OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(id), null, String.join(",", names), null, new LinkedList<>());
                return JSON.toJSONString(details);

            } catch (Exception e) {
                task = new MessageTask();
                List<DetailColumn> columns = ReflexObjectUtil.getColumns(task, SystemReference.messageColumns);
                OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(task.getId()), null,
                        StatusReference.statusMap.containsKey(task.getTaskType()) ? StatusReference.statusMap.get(task.getTaskType()) : task.getTaskType(), task.getUserId(), columns);
                return JSON.toJSONString(details);
            }
        }
        if (task != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(task, SystemReference.messageColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(task.getId()), null,
                    StatusReference.statusMap.containsKey(task.getTaskType()) ? StatusReference.statusMap.get(task.getTaskType()) : task.getTaskType(), task.getUserId(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }
}
