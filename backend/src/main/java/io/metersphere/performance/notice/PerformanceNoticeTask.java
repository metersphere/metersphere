package io.metersphere.performance.notice;

import io.metersphere.base.domain.LoadTest;
import io.metersphere.base.domain.LoadTestWithBLOBs;
import io.metersphere.base.mapper.LoadTestMapper;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.constants.PerformanceTestStatus;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.notice.domain.MessageDetail;
import io.metersphere.notice.domain.MessageSettingDetail;
import io.metersphere.notice.domain.NoticeDetail;
import io.metersphere.notice.service.DingTaskService;
import io.metersphere.notice.service.MailService;
import io.metersphere.notice.service.NoticeService;
import io.metersphere.notice.service.WxChatTaskService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class PerformanceNoticeTask {
    @Resource
    private NoticeService noticeService;
    @Resource
    private DingTaskService dingTaskService;
    @Resource
    private WxChatTaskService wxChatTaskService;
    @Resource
    private MailService mailService;
    @Resource
    private LoadTestMapper loadTestMapper;
    private final ExecutorService executorService = Executors.newFixedThreadPool(20);
    private boolean isRunning = true;

    @PreDestroy
    public void preDestroy() {
        isRunning = false;
    }
    public  void registerNoticeTask(String triggerMode,LoadTestWithBLOBs loadTest) {
        executorService.submit(() -> {
            while (isRunning) {
                LoadTestWithBLOBs result = loadTestMapper.selectByPrimaryKey(loadTest.getId());
                if (StringUtils.equals(result.getStatus(), PerformanceTestStatus.Completed.name())) {
                    isRunning = false;
                    sendSuccessNotice(triggerMode,loadTest);
                    return;
                }
                if (StringUtils.equals(result.getStatus(), PerformanceTestStatus.Error.name())) {
                    isRunning = false;
                    sendFailNotice(triggerMode,loadTest);
                    return;
                }
                try {
                    Thread.sleep(1000 * 60);// 每分钟检查 loadtest 的状态
                } catch (InterruptedException e) {
                    LogUtil.error(e);
                }
            }
        });
    }

    public void sendSuccessNotice(String triggerMode,LoadTestWithBLOBs loadTest) {
        if (StringUtils.equals(NoticeConstants.API, "API")||StringUtils.equals(NoticeConstants.SCHEDULE,"SCHEDULE")) {
            List<String> userIds = new ArrayList<>();
            MessageSettingDetail messageSettingDetail = noticeService.searchMessage();
            List<MessageDetail> taskList = messageSettingDetail.getJenkinsTask();
            if(StringUtils.equals(triggerMode,NoticeConstants.SCHEDULE)){
                List<NoticeDetail> noticeList = null;
                noticeList = noticeService.queryNotice(loadTest.getId());
                mailService.sendPerformanceNotification(noticeList, PerformanceTestStatus.Completed.name(), loadTest, loadTest.getId());            }else{
                return;
            }

            if(StringUtils.equals(triggerMode,NoticeConstants.SCHEDULE)){
                String contextSuccess="";
                contextSuccess = "jenkins任务通知:" + loadTest.getName() + "执行成功";
                String finalContextSuccess = contextSuccess;
                taskList.forEach(r -> {
                    switch (r.getType()) {
                        case NoticeConstants.NAIL_ROBOT:
                            if (StringUtils.equals(NoticeConstants.EXECUTE_SUCCESSFUL, r.getEvent())
                                    && StringUtils.equals(loadTest.getStatus(), PerformanceTestStatus.Completed.name())) {
                                dingTaskService.sendNailRobot(r, userIds, finalContextSuccess, NoticeConstants.EXECUTE_SUCCESSFUL);
                            }
                            break;
                        case NoticeConstants.WECHAT_ROBOT:
                            if (StringUtils.equals(NoticeConstants.EXECUTE_SUCCESSFUL, r.getEvent())
                                    && StringUtils.equals(loadTest.getStatus(), PerformanceTestStatus.Completed.name())) {
                                wxChatTaskService.sendWechatRobot(r, userIds, finalContextSuccess, NoticeConstants.EXECUTE_SUCCESSFUL);
                            }
                            break;
                        case NoticeConstants.EMAIL:
                            if (StringUtils.equals(NoticeConstants.EXECUTE_SUCCESSFUL, r.getEvent())
                                    && StringUtils.equals(loadTest.getStatus(), PerformanceTestStatus.Completed.name())) {
                                try {
                                    mailService.sendLoadJenkinsNotification(finalContextSuccess, r);
                                } catch (MessagingException messagingException) {
                                    messagingException.printStackTrace();
                                }
                            }
                            break;
                    }
                });
            }

        }
    }

    public void sendFailNotice(String triggerMode,LoadTestWithBLOBs loadTest) {
        if (StringUtils.equals(NoticeConstants.API, "API")) {
            List<String> userIds = new ArrayList<>();
            MessageSettingDetail messageSettingDetail = noticeService.searchMessage();
            List<MessageDetail> taskList = messageSettingDetail.getJenkinsTask();
            String contextFailed = "jenkins任务通知:" + loadTest.getName() + "执行失败";
            taskList.forEach(r -> {
                switch (r.getType()) {
                    case NoticeConstants.NAIL_ROBOT:
                        if (StringUtils.equals(NoticeConstants.EXECUTE_FAILED, r.getEvent())) {
                            dingTaskService.sendNailRobot(r, userIds, contextFailed, NoticeConstants.EXECUTE_FAILED);
                        }
                        break;
                    case NoticeConstants.WECHAT_ROBOT:
                        if (StringUtils.equals(NoticeConstants.EXECUTE_FAILED, r.getEvent())) {
                            wxChatTaskService.sendWechatRobot(r, userIds, contextFailed, NoticeConstants.EXECUTE_FAILED);
                        }
                        break;
                    case NoticeConstants.EMAIL:
                        if (StringUtils.equals(NoticeConstants.EXECUTE_FAILED, r.getEvent())) {
                            try {
                                mailService.sendLoadJenkinsNotification(contextFailed, r);
                            } catch (MessagingException messagingException) {
                                messagingException.printStackTrace();
                            }
                        }
                        break;
                }
            });
        }
    }

}