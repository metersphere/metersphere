package io.metersphere.notice.service;

import io.metersphere.base.domain.ApiTestReport;
import io.metersphere.base.domain.LoadTestReportWithBLOBs;
import io.metersphere.base.domain.SystemParameter;
import io.metersphere.base.domain.TestCaseWithBLOBs;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.constants.ParamConstants;
import io.metersphere.commons.constants.PerformanceTestStatus;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.EncryptUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.notice.domain.MessageDetail;
import io.metersphere.notice.domain.UserDetail;
import io.metersphere.service.SystemParameterService;
import io.metersphere.service.UserService;
import io.metersphere.track.request.testcase.IssuesRequest;
import io.metersphere.track.request.testplan.AddTestPlanRequest;
import io.metersphere.track.request.testreview.SaveCommentRequest;
import io.metersphere.track.request.testreview.SaveTestCaseReviewRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class MailService {
    @Resource
    private UserService userService;
    @Resource
    private SystemParameterService systemParameterService;

    //接口和性能测试
    public void sendLoadNotification(MessageDetail messageDetail, LoadTestReportWithBLOBs loadTestReport, String eventType) {
        List<String> userIds = new ArrayList<>();
        BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
        Map<String, String> context = new HashMap<>();
        context.put("testName", loadTestReport.getName());
        context.put("id", loadTestReport.getId());
        context.put("type", "performance");
        context.put("url", baseSystemConfigDTO.getUrl());
        String performanceTemplate = "";
        try {
            if (StringUtils.equals(loadTestReport.getStatus(), PerformanceTestStatus.Completed.name())) {
                performanceTemplate = IOUtils.toString(this.getClass().getResource("/mail/PerformanceApiSuccessNotification.html"), StandardCharsets.UTF_8);
            } else if (StringUtils.equals(loadTestReport.getStatus(), PerformanceTestStatus.Error.name())) {
                performanceTemplate = IOUtils.toString(this.getClass().getResource("/mail/PerformanceFailedNotification.html"), StandardCharsets.UTF_8);
            }
            sendApiOrLoadNotification(addresseeIdList(messageDetail, userIds, eventType), context, performanceTemplate, loadTestReport.getTriggerMode());
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    public void sendApiNotification(MessageDetail messageDetail, ApiTestReport apiTestReport, String eventType) {
        List<String> userIds = new ArrayList<>();
        BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
        Map<String, String> context = new HashMap<>();
        context.put("testName", apiTestReport.getName());
        context.put("type", "api");
        context.put("url", baseSystemConfigDTO.getUrl());
        context.put("id", apiTestReport.getId());
        String apiTemplate = "";
        try {
            if (StringUtils.equals(APITestStatus.Success.name(), apiTestReport.getStatus())) {
                apiTemplate = IOUtils.toString(this.getClass().getResource("/mail/ApiSuccessfulNotification.html"), StandardCharsets.UTF_8);
            } else if (StringUtils.equals(APITestStatus.Error.name(), apiTestReport.getStatus())) {
                apiTemplate = IOUtils.toString(this.getClass().getResource("/mail/ApiFailedNotification.html"), StandardCharsets.UTF_8);
            }
            sendApiOrLoadNotification(addresseeIdList(messageDetail, userIds, eventType), context, apiTemplate, apiTestReport.getTriggerMode());
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    private void sendApiOrLoadNotification(List<String> userIds, Map<String, String> context, String Template, String type) throws MessagingException {
        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }
        JavaMailSenderImpl javaMailSender = getMailSender();
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setFrom(javaMailSender.getUsername());
        if (StringUtils.equals(type, NoticeConstants.API)) {
            helper.setSubject("MeterSphere平台" + Translator.get("task_notification"));
        }
        if (StringUtils.equals(type, NoticeConstants.SCHEDULE)) {
            helper.setSubject("MeterSphere平台" + Translator.get("task_notification_"));
        }
        String[] users;
        List<String> emails = new ArrayList<>();
        List<UserDetail> list = userService.queryTypeByIds(userIds);
        list.forEach(u -> {
            emails.add(u.getEmail());
        });
        users = emails.toArray(new String[0]);
        helper.setText(getContent(Template, context), true);
        helper.setTo(users);
        javaMailSender.send(mimeMessage);
    }
    //测试评审

    public void sendEndNotice(MessageDetail messageDetail, List<String> userIds, SaveTestCaseReviewRequest reviewRequest, String eventType) {
        Map<String, String> context = getReviewContext(reviewRequest);
        try {
            String endTemplate = IOUtils.toString(this.getClass().getResource("/mail/ReviewEnd.html"), StandardCharsets.UTF_8);
            sendReviewNotice(addresseeIdList(messageDetail, userIds, eventType), context, endTemplate);
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    public void sendDeleteNotice(MessageDetail messageDetail, List<String> userIds, SaveTestCaseReviewRequest reviewRequest, String eventType) {
        Map<String, String> context = getReviewContext(reviewRequest);
        try {
            String endTemplate = IOUtils.toString(this.getClass().getResource("/mail/ReviewDelete.html"), StandardCharsets.UTF_8);
            sendReviewNotice(addresseeIdList(messageDetail, userIds, eventType), context, endTemplate);
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    public void sendCommentNotice(MessageDetail messageDetail, List<String> userIds, SaveCommentRequest request, TestCaseWithBLOBs testCaseWithBLOBs, String eventType) {
        BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
        Map<String, String> context = new HashMap<>();
        context.put("maintainer", testCaseWithBLOBs.getMaintainer());
        context.put("testCaseName", testCaseWithBLOBs.getName());
        context.put("description", request.getDescription());
        context.put("url", baseSystemConfigDTO.getUrl());
        context.put("id", testCaseWithBLOBs.getId());
        try {
            String commentTemplate = IOUtils.toString(this.getClass().getResource("/mail/ReviewComments.html"), StandardCharsets.UTF_8);
            sendReviewNotice(addresseeIdList(messageDetail, userIds, eventType), context, commentTemplate);
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    public void sendReviewerNotice(MessageDetail messageDetail, List<String> userIds, SaveTestCaseReviewRequest reviewRequest, String eventType) {
        Map<String, String> context = getReviewContext(reviewRequest);
        try {
            String reviewerTemplate = IOUtils.toString(this.getClass().getResource("/mail/ReviewInitiate.html"), StandardCharsets.UTF_8);
            sendReviewNotice(addresseeIdList(messageDetail, userIds, eventType), context, reviewerTemplate);
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    private void sendReviewNotice(List<String> userIds, Map<String, String> context, String Template) throws MessagingException {
        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }
        JavaMailSenderImpl javaMailSender = getMailSender();
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setFrom(javaMailSender.getUsername());
        helper.setSubject("MeterSphere平台" + Translator.get("test_review_task_notice"));
        String[] users;
        List<String> emails = new ArrayList<>();
        List<UserDetail> list = userService.queryTypeByIds(userIds);
        list.forEach(u -> {
            emails.add(u.getEmail());
        });
        users = emails.toArray(new String[0]);
        helper.setText(getContent(Template, context), true);
        helper.setTo(users);
        if (users.length > 0) {
            javaMailSender.send(mimeMessage);
        }
    }

    //测试计划

    public void sendTestPlanStartNotice(MessageDetail messageDetail, List<String> userIds, AddTestPlanRequest testPlan, String eventType) {
        Map<String, String> context = getTestPlanContext(testPlan);
        context.put("creator", testPlan.getCreator());
        try {
            String endTemplate = IOUtils.toString(this.getClass().getResource("/mail/TestPlanStart.html"), StandardCharsets.UTF_8);
            sendTestPlanNotice(addresseeIdList(messageDetail, userIds, eventType), context, endTemplate);
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    public void sendTestPlanEndNotice(MessageDetail messageDetail, List<String> userIds, AddTestPlanRequest testPlan, String eventType) {
        Map<String, String> context = getTestPlanContext(testPlan);
        context.put("creator", userIds.toString());
        try {
            String endTemplate = IOUtils.toString(this.getClass().getResource("/mail/TestPlanEnd.html"), StandardCharsets.UTF_8);
            sendTestPlanNotice(addresseeIdList(messageDetail, userIds, eventType), context, endTemplate);
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    public void sendTestPlanDeleteNotice(MessageDetail messageDetail, List<String> userIds, AddTestPlanRequest testPlan, String eventType) {
        Map<String, String> context = getTestPlanContext(testPlan);
        context.put("creator", userIds.toString());
        try {
            String endTemplate = IOUtils.toString(this.getClass().getResource("/mail/TestPlanDelete.html"), StandardCharsets.UTF_8);
            sendTestPlanNotice(addresseeIdList(messageDetail, userIds, eventType), context, endTemplate);
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    private void sendTestPlanNotice(List<String> userIds, Map<String, String> context, String Template) throws MessagingException {
        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }
        JavaMailSenderImpl javaMailSender = getMailSender();
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setFrom(javaMailSender.getUsername());
        helper.setSubject("MeterSphere平台" + Translator.get("test_plan_notification"));
        String[] users;
        List<String> emails = new ArrayList<>();
        List<UserDetail> list = userService.queryTypeByIds(userIds);
        list.forEach(u -> {
            emails.add(u.getEmail());
        });
        users = emails.toArray(new String[0]);
        helper.setText(getContent(Template, context), true);
        helper.setTo(users);
        javaMailSender.send(mimeMessage);

    }

    //缺陷任务
    public void sendIssuesNotice(MessageDetail messageDetail, List<String> userIds, IssuesRequest issuesRequest, String eventType, SessionUser user) {
        Map<String, String> context = new HashMap<>();
        context.put("issuesName", issuesRequest.getTitle());
        context.put("creator", user.getName());
        try {
            String endTemplate = IOUtils.toString(this.getClass().getResource("/mail/IssuesCreate.html"), StandardCharsets.UTF_8);
            sendIssuesNotice(addresseeIdList(messageDetail, userIds, eventType), context, endTemplate);
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    private void sendIssuesNotice(List<String> userIds, Map<String, String> context, String Template) throws MessagingException {
        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }
        JavaMailSenderImpl javaMailSender = getMailSender();
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setFrom(javaMailSender.getUsername());
        helper.setSubject("MeterSphere平台" + Translator.get("task_defect_notification"));
        String[] users;
        List<String> emails = new ArrayList<>();
        List<UserDetail> list = userService.queryTypeByIds(userIds);
        list.forEach(u -> {
            emails.add(u.getEmail());
        });
        users = emails.toArray(new String[0]);
        helper.setText(getContent(Template, context), true);
        helper.setTo(users);
        javaMailSender.send(mimeMessage);

    }

    //评审内容
    private Map<String, String> getReviewContext(SaveTestCaseReviewRequest reviewRequest) {
        Long startTime = reviewRequest.getCreateTime();
        Long endTime = reviewRequest.getEndTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String start = null;
        String sTime = String.valueOf(startTime);
        String eTime = String.valueOf(endTime);
        if (!sTime.equals("null")) {
            start = sdf.format(new Date(Long.parseLong(sTime)));
        }
        String end = null;
        if (!eTime.equals("null")) {
            end = sdf.format(new Date(Long.parseLong(eTime)));
        }

        Map<String, String> context = new HashMap<>();
        BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
        context.put("url", baseSystemConfigDTO.getUrl());
        context.put("creator", reviewRequest.getCreator());
        context.put("reviewName", reviewRequest.getName());
        context.put("start", start);
        context.put("end", end);
        context.put("id", reviewRequest.getId());
        return context;
    }

    //计划内容
    private Map<String, String> getTestPlanContext(AddTestPlanRequest testPlan) {
        Long startTime = testPlan.getPlannedStartTime();
        Long endTime = testPlan.getPlannedEndTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String start = null;
        String sTime = String.valueOf(startTime);
        String eTime = String.valueOf(endTime);
        if (!sTime.equals("null")) {
            start = sdf.format(new Date(Long.parseLong(sTime)));
        }
        String end = null;
        if (!eTime.equals("null")) {
            end = sdf.format(new Date(Long.parseLong(eTime)));
        }

        Map<String, String> context = new HashMap<>();
        BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
        context.put("url", baseSystemConfigDTO.getUrl());
        context.put("testPlanName", testPlan.getName());
        context.put("start", start);
        context.put("end", end);
        context.put("id", testPlan.getId());
        return context;
    }




    private JavaMailSenderImpl getMailSender() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        List<SystemParameter> paramList = systemParameterService.getParamList(ParamConstants.Classify.MAIL.getValue());
        javaMailSender.setDefaultEncoding("UTF-8");
        javaMailSender.setProtocol("smtps");
        for (SystemParameter p : paramList) {
            switch (p.getParamKey()) {
                case "smtp.host":
                    javaMailSender.setHost(p.getParamValue());
                    break;
                case "smtp.port":
                    javaMailSender.setPort(Integer.parseInt(p.getParamValue()));
                    break;
                case "smtp.account":
                    javaMailSender.setUsername(p.getParamValue());
                    break;
                case "smtp.password":
                    javaMailSender.setPassword(EncryptUtils.aesDecrypt(p.getParamValue()).toString());
                    break;
                default:
                    break;
            }
        }
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.timeout", "30000");
        props.put("mail.smtp.connectiontimeout", "5000");
        javaMailSender.setJavaMailProperties(props);
        return javaMailSender;
    }

    private String getContent(String template, Map<String, String> context) {
        if (MapUtils.isNotEmpty(context)) {
            for (String k : context.keySet()) {
                if (StringUtils.isNotBlank(context.get(k))) {
                    template = RegExUtils.replaceAll(template, "\\$\\{" + k + "}", context.get(k));
                } else {
                    template = RegExUtils.replaceAll(template, "\\$\\{" + k + "}", "");
                }
            }
        }
        return template;
    }

    private List<String> addresseeIdList(MessageDetail messageDetail, List<String> userIds, String eventType) {
        List<String> addresseeIdList = new ArrayList<>();
        if (StringUtils.equals(eventType, messageDetail.getEvent())) {
            messageDetail.getUserIds().forEach(u -> {
                if (!StringUtils.equals(NoticeConstants.EXECUTOR, u) && !StringUtils.equals(NoticeConstants.EXECUTOR, u) && !StringUtils.equals(NoticeConstants.MAINTAINER, u)) {
                    addresseeIdList.add(u);
                }
                if (StringUtils.equals(NoticeConstants.CREATE, eventType) && StringUtils.equals(NoticeConstants.EXECUTOR, u)) {
                    addresseeIdList.addAll(userIds);
                }
                if (StringUtils.equals(NoticeConstants.UPDATE, eventType) && StringUtils.equals(NoticeConstants.FOUNDER, u)) {
                    addresseeIdList.addAll(userIds);
                }
                if (StringUtils.equals(NoticeConstants.DELETE, eventType) && StringUtils.equals(NoticeConstants.FOUNDER, u)) {
                    addresseeIdList.addAll(userIds);
                }
                if (StringUtils.equals(NoticeConstants.COMMENT, eventType) && StringUtils.equals(NoticeConstants.MAINTAINER, u)) {
                    addresseeIdList.addAll(userIds);
                }


            });
        }
        return addresseeIdList;
    }

}





