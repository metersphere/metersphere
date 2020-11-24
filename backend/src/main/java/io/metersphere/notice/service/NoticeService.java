package io.metersphere.notice.service;

import io.metersphere.base.domain.MessageTask;
import io.metersphere.base.domain.MessageTaskExample;
import io.metersphere.base.mapper.MessageTaskMapper;
import io.metersphere.base.mapper.ext.ExtMessageMapper;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.i18n.Translator;
import io.metersphere.notice.controller.request.MessageRequest;
import io.metersphere.notice.domain.MessageDetail;
import io.metersphere.notice.domain.MessageSettingDetail;
import org.apache.commons.collections.CollectionUtils;
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
    private ExtMessageMapper extMessageMapper;


    public void saveMessageTask(MessageRequest messageRequest) {
        messageRequest.getMessageDetail().forEach(list -> {
            MessageTaskExample example = new MessageTaskExample();
            example.createCriteria().andIdentificationEqualTo(list.getIdentification());
            List<MessageTask> messageTaskLists = messageTaskMapper.selectByExample(example);
            if (messageTaskLists.size() > 0) {
                delMessage(list.getIdentification());
                getSaveMessageTask(list);
            }
            if (messageTaskLists.size() <= 0) {
                getSaveMessageTask(list);
            }
        });
    }

    public void getSaveMessageTask(MessageDetail list) {
        SessionUser user = SessionUtils.getUser();
        assert user != null;
        String orgId = user.getLastOrganizationId();
        long time = System.currentTimeMillis();
        String identification = UUID.randomUUID().toString();
        list.getUserIds().forEach(m -> {
            checkUserIdExist(m, list);
            MessageTask message = new MessageTask();
            message.setId(UUID.randomUUID().toString());
            message.setEvent(list.getEvent());
            message.setTaskType(list.getTaskType());
            message.setUserId(m);
            message.setType(list.getType());
            message.setWebhook(list.getWebhook());
            message.setIdentification(identification);
            message.setIsSet(false);
            message.setOrganizationId(orgId);
            message.setTestId(list.getTestId());
            message.setCreateTime(time);
            messageTaskMapper.insert(message);
        });
    }

    private void checkUserIdExist(String userId, MessageDetail list) {
        MessageTaskExample example = new MessageTaskExample();
        example.createCriteria().andUserIdEqualTo(userId).andEventEqualTo(list.getEvent()).andTypeEqualTo(list.getType()).andTaskTypeEqualTo(list.getTaskType()).andWebhookEqualTo(list.getWebhook());
        if (messageTaskMapper.countByExample(example) > 0) {
            MSException.throwException(Translator.get("message_task_already_exists"));
        }
    }

    public List<MessageDetail> searchMessageSchedule(String testId) {
        List<MessageTask> messageTaskLists = extMessageMapper.searchMessageByTestId(testId);
        List<MessageDetail> scheduleMessageTask = new ArrayList<>();
        Map<String, List<MessageTask>> MessageTaskMap = messageTaskLists.stream().collect(Collectors.groupingBy(MessageTask::getIdentification));
        MessageTaskMap.forEach((k, v) -> {
            Set<String> userIds = new HashSet<>();
            MessageDetail messageDetail = new MessageDetail();
            for (MessageTask m : v) {
                userIds.add(m.getUserId());
                messageDetail.setEvent(m.getEvent());
                messageDetail.setTaskType(m.getTaskType());
                messageDetail.setWebhook(m.getWebhook());
                messageDetail.setIdentification(m.getIdentification());
                messageDetail.setType(m.getType());
                messageDetail.setIsSet(m.getIsSet());
                messageDetail.setCreateTime(m.getCreateTime());
            }
            if (CollectionUtils.isNotEmpty(userIds)) {
                messageDetail.setUserIds(new ArrayList<>(userIds));
            }
            scheduleMessageTask.add(messageDetail);
        });
        scheduleMessageTask.sort(Comparator.comparing(MessageDetail::getCreateTime, Comparator.nullsLast(Long::compareTo)).reversed());
        return scheduleMessageTask;
    }

    public MessageSettingDetail searchMessage() {
        SessionUser user = SessionUtils.getUser();
        assert user != null;
        String orgId = user.getLastOrganizationId();
        List<MessageTask> messageTaskLists;
        MessageSettingDetail messageSettingDetail = new MessageSettingDetail();
        List<MessageDetail> MessageDetailList = new ArrayList<>();
        messageTaskLists = extMessageMapper.searchMessageByOrganizationId(orgId);
        Map<String, List<MessageTask>> MessageTaskMap = messageTaskLists.stream().collect(Collectors.groupingBy(NoticeService::fetchGroupKey));
        MessageTaskMap.forEach((k, v) -> {
            Set<String> userIds = new HashSet<>();
            MessageDetail messageDetail = new MessageDetail();
            for (MessageTask m : v) {
                userIds.add(m.getUserId());
                messageDetail.setEvent(m.getEvent());
                messageDetail.setTaskType(m.getTaskType());
                messageDetail.setWebhook(m.getWebhook());
                messageDetail.setIdentification(m.getIdentification());
                messageDetail.setType(m.getType());
                messageDetail.setIsSet(m.getIsSet());
                messageDetail.setCreateTime(m.getCreateTime());
            }
            messageDetail.setUserIds(new ArrayList<String>(userIds));
            MessageDetailList.add(messageDetail);
        });
        List<MessageDetail> jenkinsTask = (MessageDetailList.stream().filter(a -> a.getTaskType().equals(NoticeConstants.JENKINS_TASK)).sorted(Comparator.comparing(
                MessageDetail::getCreateTime, Comparator.nullsLast(Long::compareTo)).reversed()).collect(Collectors.toList())).stream().distinct().collect(Collectors.toList());
        List<MessageDetail> testCasePlanTask = (MessageDetailList.stream().filter(a -> a.getTaskType().equals(NoticeConstants.TEST_PLAN_TASK)).sorted(Comparator.comparing(
                MessageDetail::getCreateTime, Comparator.nullsLast(Long::compareTo)).reversed()).collect(Collectors.toList())).stream().distinct().collect(Collectors.toList());
        List<MessageDetail> reviewTask = (MessageDetailList.stream().filter(a -> a.getTaskType().equals(NoticeConstants.REVIEW_TASK)).sorted(Comparator.comparing(
                MessageDetail::getCreateTime, Comparator.nullsLast(Long::compareTo)).reversed()).collect(Collectors.toList())).stream().distinct().collect(Collectors.toList());
        List<MessageDetail> defectTask = (MessageDetailList.stream().filter(a -> a.getTaskType().equals(NoticeConstants.DEFECT_TASK)).sorted(Comparator.comparing(
                MessageDetail::getCreateTime, Comparator.nullsLast(Long::compareTo)).reversed()).collect(Collectors.toList())).stream().distinct().collect(Collectors.toList());
        messageSettingDetail.setJenkinsTask(jenkinsTask);
        messageSettingDetail.setTestCasePlanTask(testCasePlanTask);
        messageSettingDetail.setReviewTask(reviewTask);
        messageSettingDetail.setDefectTask(defectTask);
        return messageSettingDetail;
    }

    private static String fetchGroupKey(MessageTask user) {
        return user.getTaskType() + "#" + user.getIdentification();
    }

    public int delMessage(String identification) {
        MessageTaskExample example = new MessageTaskExample();
        example.createCriteria().andIdentificationEqualTo(identification);
        return messageTaskMapper.deleteByExample(example);
    }
}