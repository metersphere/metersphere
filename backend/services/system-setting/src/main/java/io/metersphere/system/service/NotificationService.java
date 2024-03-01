package io.metersphere.system.service;



import io.metersphere.project.domain.Notification;
import io.metersphere.project.domain.NotificationExample;
import io.metersphere.project.mapper.NotificationMapper;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.sdk.request.NotificationRequest;
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

    public List<Notification> listNotification(NotificationRequest notificationRequest, String userId) {
        if (StringUtils.isBlank(notificationRequest.getReceiver())) {
            notificationRequest.setReceiver(userId);
        }
        return baseNotificationMapper.listNotification(notificationRequest);
    }

    public int read(long id, String userId) {
        Notification record = new Notification();
        record.setStatus(NotificationConstants.Status.READ.name());
        NotificationExample example = new NotificationExample();
        example.createCriteria().andIdEqualTo(id).andReceiverEqualTo(userId);
        return notificationMapper.updateByExampleSelective(record, example);
    }

    public int readAll(String userId) {
        Notification record = new Notification();
        record.setStatus(NotificationConstants.Status.READ.name());
        NotificationExample example = new NotificationExample();
        example.createCriteria().andReceiverEqualTo(userId);
        return notificationMapper.updateByExampleSelective(record, example);
    }

    public List<OptionDTO> countNotification(NotificationRequest notificationRequest, String userId) {
        List<OptionDTO>optionDTOS = new ArrayList<>();
        if (StringUtils.isBlank(notificationRequest.getReceiver())) {
            notificationRequest.setReceiver(userId);
        }
        List<Notification> notifications = baseNotificationMapper.listNotification(notificationRequest);
        OptionDTO totalOptionDTO = new OptionDTO();
        totalOptionDTO.setId("total");
        totalOptionDTO.setName(String.valueOf(notifications.size()));
        optionDTOS.add(totalOptionDTO);
        buildSourceCount(notifications, optionDTOS);
        return optionDTOS;
    }

    private static void buildSourceCount(List<Notification> notifications, List<OptionDTO> optionDTOS) {
        Map<String,Integer>countMap = new HashMap<>();
        Map<String, List<Notification>> resourceMap = notifications.stream().collect(Collectors.groupingBy(Notification::getResourceType));
        resourceMap.forEach((k,v)->{
            if (k.contains("TEST_PLAN")) {
                countMap.merge("TEST_PLAN", v.size(), Integer::sum);
            }
            if (k.contains("BUG")) {
                countMap.merge("BUG", v.size(), Integer::sum);
            }
            if (k.contains("CASE")) {
                countMap.merge("CASE", v.size(), Integer::sum);
            }
            if (k.contains("API")) {
                countMap.merge("API", v.size(), Integer::sum);
            }
            if (k.contains("UI")) {
                countMap.merge("UI", v.size(), Integer::sum);
            }
            if (k.contains("LOAD")) {
                countMap.merge("LOAD", v.size(), Integer::sum);
            }
            if (k.contains("JENKINS")) {
                countMap.merge("JENKINS", v.size(), Integer::sum);
            }
        });
        countMap.forEach((k,v)->{
            OptionDTO optionDTO = new OptionDTO();
            optionDTO.setId("total");
            optionDTO.setName(String.valueOf(notifications.size()));
            optionDTOS.add(optionDTO);
        });
    }

    public void sendAnnouncement(Notification notification) {
        notificationMapper.insert(notification);
    }



}
