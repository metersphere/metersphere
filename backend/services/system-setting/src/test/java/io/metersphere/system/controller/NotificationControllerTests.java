package io.metersphere.system.controller;

import io.metersphere.project.domain.Notification;
import io.metersphere.project.domain.NotificationExample;
import io.metersphere.project.mapper.NotificationMapper;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.sdk.request.NotificationRequest;
import io.metersphere.system.notice.constants.NotificationConstants;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.List;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NotificationControllerTests extends BaseTest {

    public static final String NOTIFICATION_LIST_PAGE = "/notification/list/all/page";
    public static final String NOTIFICATION_READ = "/notification/read/";
    public static final String NOTIFICATION_READ_ALL = "/notification/read/all";
    public static final String NOTIFICATION_COUNT = "/notification/count";

    @Resource
    protected NotificationMapper notificationMapper;

    void saveNotice() {
        Notification notification = new Notification();
        notification.setSubject("功能用例更新通知");
        notification.setOperator("admin");
        notification.setOperation("UPDATE");
        notification.setResourceType("FUNCTIONAL_CASE_TASK");
        notification.setResourceName("功能用例导入测4");
        notification.setType("SYSTEM_NOTICE");
        notification.setStatus(NotificationConstants.Status.UNREAD.name());
        notification.setCreateTime(System.currentTimeMillis());
        notification.setReceiver("admin");
        notification.setResourceId("122334");
        notification.setContent("nihao");
        notificationMapper.insert(notification);
    }

    @Test
    @Order(1)
    void getNotificationSuccess() throws Exception {
        saveNotice();
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setPageSize(10);
        notificationRequest.setCurrent(1);
        notificationRequest.setReceiver("admin");
        notificationRequest.setType("SYSTEM_NOTICE");
        MvcResult mvcResult = this.requestPostWithOkAndReturn(NOTIFICATION_LIST_PAGE, notificationRequest);
        Pager<List<Notification>> tableData = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                Pager.class);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(tableData.getCurrent(), notificationRequest.getCurrent());
        Assertions.assertFalse(tableData.getList().isEmpty());

    }

    @Test
    @Order(2)
    void setNotificationReadSuccess() throws Exception {
        NotificationExample notificationExample = new NotificationExample();
        notificationExample.createCriteria().andStatusEqualTo(NotificationConstants.Status.UNREAD.name());
        List<Notification> notifications = notificationMapper.selectByExample(notificationExample);
        this.requestGetWithOkAndReturn(NOTIFICATION_READ+notifications.get(0).getId());
        notificationExample = new NotificationExample();
        notificationExample.createCriteria().andStatusEqualTo(NotificationConstants.Status.READ.name());
        List<Notification> readNotifications = notificationMapper.selectByExample(notificationExample);
        Assertions.assertFalse(readNotifications.isEmpty());

    }

    @Test
    @Order(3)
    void setNotificationReadAll() throws Exception {
        saveNotice();
        this.requestGetWithOk(NOTIFICATION_READ_ALL);
        NotificationExample notificationExample = new NotificationExample();
        notificationExample.createCriteria().andStatusEqualTo(NotificationConstants.Status.READ.name());
        List<Notification> notifications = notificationMapper.selectByExample(notificationExample);
        Assertions.assertFalse(notifications.isEmpty());

    }

    @Test
    @Order(4)
    void getNotificationCount() throws Exception {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setPageSize(10);
        notificationRequest.setCurrent(1);
        notificationRequest.setReceiver("admin");
        notificationRequest.setType("SYSTEM_NOTICE");
        MvcResult mvcResult = this.requestPostWithOkAndReturn(NOTIFICATION_COUNT, notificationRequest);
        String updateReturnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(updateReturnData, ResultHolder.class);
        List<OptionDTO> optionDTOS = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), OptionDTO.class);
        Assertions.assertFalse(optionDTOS.isEmpty());
    }



}
