package io.metersphere.system.service;


import io.metersphere.project.domain.Notification;
import io.metersphere.project.domain.NotificationExample;
import io.metersphere.project.mapper.NotificationMapper;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.sdk.request.NotificationRequest;
import io.metersphere.system.log.dto.NotificationDTO;
import io.metersphere.system.mapper.BaseNotificationMapper;
import io.metersphere.system.notice.constants.NotificationConstants;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class NotificationService {

    @Resource
    private NotificationMapper notificationMapper;
    @Resource
    private BaseNotificationMapper baseNotificationMapper;

    public List<NotificationDTO> listNotification(NotificationRequest notificationRequest, String userId) {
        buildParam(notificationRequest, userId);
        return baseNotificationMapper.listNotification(notificationRequest);
    }

    public int read(long id, String userId) {
        Notification record = new Notification();
        record.setStatus(NotificationConstants.Status.READ.name());
        NotificationExample example = new NotificationExample();
        example.createCriteria().andIdEqualTo(id).andReceiverEqualTo(userId);
        return notificationMapper.updateByExampleSelective(record, example);
    }

    public int readAll(String resourceType, String userId) {
        Notification record = new Notification();
        record.setStatus(NotificationConstants.Status.READ.name());
        NotificationExample example = new NotificationExample();
        if (StringUtils.isNotBlank(resourceType)) {
            example.createCriteria().andResourceTypeLike("%" + resourceType + "%");
        }
        example.createCriteria().andReceiverEqualTo(userId);
        return notificationMapper.updateByExampleSelective(record, example);
    }

    public List<OptionDTO> countNotification(NotificationRequest notificationRequest, String userId) {
        List<OptionDTO> optionDTOS = new ArrayList<>();
        buildParam(notificationRequest, userId);
        notificationRequest.setResourceType(StringUtils.EMPTY);
        notificationRequest.setStatus(NotificationConstants.Status.UNREAD.name());
        List<NotificationDTO> notifications = baseNotificationMapper.listNotification(notificationRequest);
        OptionDTO totalOptionDTO = new OptionDTO();
        totalOptionDTO.setId("total");
        totalOptionDTO.setName(String.valueOf(notifications.size()));
        optionDTOS.add(totalOptionDTO);
        buildSourceCount(notifications, optionDTOS);
        return optionDTOS;
    }

    private static void buildParam(NotificationRequest notificationRequest, String userId) {
        if (StringUtils.isNotBlank(notificationRequest.getTitle())) {
            notificationRequest.setTitle("%" + notificationRequest.getTitle() + "%");
        }
        if (StringUtils.isNotBlank(notificationRequest.getResourceType())) {
            notificationRequest.setResourceType("%" + notificationRequest.getResourceType() + "%");
        }
        if (StringUtils.isBlank(notificationRequest.getReceiver())) {
            notificationRequest.setReceiver(userId);
        }
    }

    private static void buildSourceCount(List<NotificationDTO> notifications, List<OptionDTO> optionDTOS) {
        Map<String, Integer> countMap = new HashMap<>();
        Map<String, List<Notification>> resourceMap = notifications.stream().collect(Collectors.groupingBy(Notification::getResourceType));
        resourceMap.forEach((k, v) -> {
            if (k.contains("BUG")) {
                countMap.merge("BUG", v.size(), Integer::sum);
            }
            if (k.contains("CASE")) {
                countMap.merge("CASE", v.size(), Integer::sum);
            }
            if (k.contains("API")) {
                countMap.merge("API", v.size(), Integer::sum);
            }
            if (k.contains("SCHEDULE")) {
                countMap.merge("SCHEDULE", v.size(), Integer::sum);
            }
            if (k.contains("TEST_PLAN")) {
                countMap.merge("TEST_PLAN", v.size(), Integer::sum);
            }
            if (k.contains("JENKINS")) {
                countMap.merge("JENKINS", v.size(), Integer::sum);
            }
        });
        countMap.forEach((k, v) -> {
            OptionDTO optionDTO = new OptionDTO();
            optionDTO.setId(k);
            optionDTO.setName(String.valueOf(v));
            optionDTOS.add(optionDTO);
        });
    }

    public void sendAnnouncement(Notification notification) {
        notificationMapper.insert(notification);
    }


    public Integer getUnRead(String projectId, String userId) {
        NotificationExample example = new NotificationExample();
        if (StringUtils.isBlank(projectId)) {
            return 0;
        }
        example.createCriteria().andProjectIdEqualTo(projectId).andStatusEqualTo(NotificationConstants.Status.UNREAD.name()).andReceiverEqualTo(userId);
        return (int) notificationMapper.countByExample(example);
    }
}
