package io.metersphere.notice.service;

import io.metersphere.api.dto.APIReportResult;
import io.metersphere.base.domain.Notice;
import io.metersphere.base.domain.SystemParameter;
import io.metersphere.base.domain.TestCaseWithBLOBs;
import io.metersphere.commons.constants.ParamConstants;
import io.metersphere.commons.utils.EncryptUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.LoadTestDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.service.SystemParameterService;
import io.metersphere.service.UserService;
import io.metersphere.track.request.testreview.SaveCommentRequest;
import io.metersphere.track.request.testreview.SaveTestCaseReviewRequest;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class MailService {
    @Resource
    private ApiAndPerformanceHelper apiAndPerformanceHelper;
    @Resource
    private UserService userService;
    @Resource
    private SystemParameterService systemParameterService;

    public void sendHtml(String id, List<Notice> notice, String status, String type) {
        JavaMailSenderImpl javaMailSender = getMailSender();
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        String testName = "";
        if (type.equals("api")) {
            APIReportResult reportResult = apiAndPerformanceHelper.getApi(id);
            testName = reportResult.getTestName();
        } else if (type.equals("performance")) {
            LoadTestDTO performanceResult = apiAndPerformanceHelper.getPerformance(id);
            testName = performanceResult.getName();
            status = performanceResult.getStatus();
        }

        Map<String, String> context = new HashMap<>();
        context.put("title", type + Translator.get("timing_task_result_notification"));
        context.put("testName", testName);

        try {
            String failTemplate = IOUtils.toString(this.getClass().getResource("/mail/fail.html"), StandardCharsets.UTF_8);
            String successTemplate = IOUtils.toString(this.getClass().getResource("/mail/success.html"), StandardCharsets.UTF_8);

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(javaMailSender.getUsername());
            helper.setSubject(Translator.get("timing_task_result_notification"));
            String[] users;
            List<String> successEmailList = new ArrayList<>();
            List<String> failEmailList = new ArrayList<>();
            if (notice.size() > 0) {
                for (Notice n : notice) {
                    if (n.getEnable().equals("true") && n.getEvent().equals("执行成功")) {
                        successEmailList = userService.queryEmail(n.getNames());
                    }
                    if (n.getEnable().equals("true") && n.getEvent().equals("执行失败")) {
                        failEmailList = userService.queryEmail(n.getNames());
                    }
                }
            } else {
                LogUtil.error("Recipient information is empty");
            }

            if (status.equals("Success")) {
                users = successEmailList.toArray(new String[0]);
                helper.setText(getContent(successTemplate, context), true);
            } else {
                users = failEmailList.toArray(new String[0]);
                helper.setText(getContent(failTemplate, context), true);
            }
            helper.setTo(users);
        } catch (Exception e) {
            LogUtil.error(e);
        }
        try {
            javaMailSender.send(mimeMessage);
        } catch (MailException e) {
            LogUtil.error(e);
        }
    }

    private String getContent(String template, Map<String, String> context) {
        if (MapUtils.isNotEmpty(context)) {
            for (String k : context.keySet()) {
                if (StringUtils.isNotBlank(context.get(k)))
                    template = RegExUtils.replaceAll(template, "\\$\\{" + k + "}", context.get(k));
            }
        }
        return template;
    }


    public void sendHtml(List<String> userIds, String type, SaveTestCaseReviewRequest reviewRequest, SaveCommentRequest request, TestCaseWithBLOBs testCaseWithBLOBs) {
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
        JavaMailSenderImpl javaMailSender = getMailSender();
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        Map<String, String> context = new HashMap<>();
        context.put("creator", reviewRequest.getCreator());
        context.put("maintainer", testCaseWithBLOBs.getMaintainer());
        context.put("testCaseName", testCaseWithBLOBs.getName());
        context.put("reviewName", reviewRequest.getName());
        context.put("description", request.getDescription());
        context.put("start", start);
        context.put("end", end);

        try {
            String reviewerTemplate = IOUtils.toString(this.getClass().getResource("/mail/reviewer.html"), StandardCharsets.UTF_8);
            String commentTemplate = IOUtils.toString(this.getClass().getResource("/mail/comment.html"), StandardCharsets.UTF_8);
            String endTemplate = IOUtils.toString(this.getClass().getResource("/mail/end.html"), StandardCharsets.UTF_8);


            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(javaMailSender.getUsername());
            helper.setSubject(Translator.get("test_review_task_notice"));
            String[] users;
            List<String> emails = new ArrayList<>();
            try {
                emails = userService.queryEmailByIds(userIds);
            } catch (Exception e) {
                LogUtil.error("Recipient information is empty");
            }
            users = emails.toArray(new String[0]);
            switch (type) {
                case "reviewer":
                    helper.setText(getContent(reviewerTemplate, context), true);
                    break;
                case "comment":
                    helper.setText(getContent(commentTemplate, context), true);
                    break;
                case "end":
                    helper.setText(getContent(endTemplate, context), true);
                    break;
                default:
                    break;
            }
            helper.setTo(users);

        } catch (Exception e) {
            LogUtil.error(e);
        }
        try {
            javaMailSender.send(mimeMessage);
        } catch (MailException e) {
            LogUtil.error(e);
        }
    }

    private JavaMailSenderImpl getMailSender() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        List<SystemParameter> paramList = systemParameterService.getParamList(ParamConstants.Classify.MAIL.getValue());
        javaMailSender.setDefaultEncoding("UTF-8");
        javaMailSender.setProtocol("smtps");
        for (SystemParameter p : paramList) {
            if (p.getParamKey().equals("smtp.host")) {
                javaMailSender.setHost(p.getParamValue());
            }
            if (p.getParamKey().equals("smtp.port")) {
                javaMailSender.setPort(Integer.parseInt(p.getParamValue()));
            }
            if (p.getParamKey().equals("smtp.account")) {
                javaMailSender.setUsername(p.getParamValue());
            }
            if (p.getParamKey().equals("smtp.password")) {
                javaMailSender.setPassword(EncryptUtils.aesDecrypt(p.getParamValue()).toString());
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
}

