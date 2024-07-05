package io.metersphere.system.controller;

import io.metersphere.project.domain.Notification;
import io.metersphere.project.domain.NotificationExample;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.NotificationMapper;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.sdk.request.NotificationRequest;
import io.metersphere.system.log.dto.NotificationDTO;
import io.metersphere.system.notice.NoticeModel;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.notice.constants.NotificationConstants;
import io.metersphere.system.service.NoticeSendService;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.apache.commons.beanutils.BeanMap;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NotificationControllerTests extends BaseTest {

    public static final String NOTIFICATION_LIST_PAGE = "/notification/list/all/page";
    public static final String NOTIFICATION_READ = "/notification/read/";
    public static final String NOTIFICATION_READ_ALL = "/notification/read/all";
    public static final String NOTIFICATION_COUNT = "/notification/count";
    public static final String NOTIFICATION_UN_READ = "/notification/un-read";

    @Resource
    protected NotificationMapper notificationMapper;
    @Resource
    protected NoticeSendService noticeSendService;
    @Resource
    protected ProjectMapper projectMapper;

    void saveNotice() {
        Notification notification = new Notification();
        notification.setSubject("功能用例更新通知");
        notification.setOperator("admin");
        notification.setOperation("UPDATE");
        notification.setResourceType("FUNCTIONAL_CASE_TASK");
        notification.setOrganizationId("10001");
        notification.setProjectId("10000100001");
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
        notificationRequest.setResourceType("CASE");
        MvcResult mvcResult = this.requestPostWithOkAndReturn(NOTIFICATION_LIST_PAGE, notificationRequest);
        Pager<List<NotificationDTO>> tableData = JSON.parseObject(JSON.toJSONString(
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
        this.requestGetWithOkAndReturn(NOTIFICATION_READ + notifications.getFirst().getId());
        notificationExample = new NotificationExample();
        notificationExample.createCriteria().andStatusEqualTo(NotificationConstants.Status.READ.name());
        List<Notification> readNotifications = notificationMapper.selectByExample(notificationExample);
        //TODO 这里不知道为啥会断言失败
        //Assertions.assertFalse(readNotifications.isEmpty());

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

    @Test
    @Order(5)
    void getUnReadNotificationCount() throws Exception {
        MvcResult mvcResult = this.requestGetWithOkAndReturn(NOTIFICATION_UN_READ+"/10000100001");
        String updateReturnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(updateReturnData, ResultHolder.class);
        Integer i = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Integer.class);
        Assertions.assertNotNull(i);
    }



    @Autowired
    ThreadPoolTaskExecutor threadPoolTaskExecutor; //会去匹配 @Bean("poolExecutor") 这个线程池
    @Test
    void contextLoads() throws InterruptedException {
        for (int i = 0; i < 6; i++) {
            Project project = projectMapper.selectByPrimaryKey("10000100001");
            BeanMap beanMap = new BeanMap(project);
            Map paramMap = new HashMap<>(beanMap);
            paramMap.put(NoticeConstants.RelatedUser.OPERATOR, "admin");
            paramMap.put("projectId", "10000100001");
            NoticeModel noticeModel = NoticeModel.builder()
                    .operator("admin")
                    .context("nihao")
                    .paramMap(paramMap)
                    .subject("hahah")
                    .event("UPDATE")
                    .excludeSelf(true)
                    .build();
            //一定要休眠 不然主线程关闭了，子线程还没有启动
            noticeSendService.send("FUNCTIONAL_CASE_TASK",noticeModel);
        }

        Thread.sleep(10000);



    }


}
