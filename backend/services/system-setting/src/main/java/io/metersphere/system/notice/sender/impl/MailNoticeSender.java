package io.metersphere.system.notice.sender.impl;


import io.metersphere.sdk.constants.ParamConstants;
import io.metersphere.sdk.util.EncryptUtils;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.domain.SystemParameter;
import io.metersphere.system.domain.SystemParameterExample;
import io.metersphere.system.domain.User;
import io.metersphere.system.mapper.SystemParameterMapper;
import io.metersphere.system.notice.MessageDetail;
import io.metersphere.system.notice.NoticeModel;
import io.metersphere.system.notice.Receiver;
import io.metersphere.system.notice.sender.AbstractNoticeSender;
import jakarta.annotation.Resource;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class MailNoticeSender extends AbstractNoticeSender {

    @Resource
    private SystemParameterMapper systemParameterMapper;

    public void sendMail(String context, NoticeModel noticeModel, String projectId, String subjectText) throws Exception {
        List<Receiver> receivers = super.getReceivers(noticeModel.getReceivers(), noticeModel.isExcludeSelf(), noticeModel.getOperator());
        if (CollectionUtils.isEmpty(receivers)) {
            return;
        }
        List<String> userIds = receivers.stream()
                .map(Receiver::getUserId)
                .distinct()
                .collect(Collectors.toList());
        String[] users = super.getUsers(userIds, projectId, true).stream()
                .map(User::getEmail)
                .distinct()
                .toArray(String[]::new);

        send(subjectText, context, users, new String[0]);
    }

    private void send(String subject, String context, String[] users, String[] cc) throws Exception {
        LogUtils.debug("发送邮件开始 ");
        SystemParameterExample example = new SystemParameterExample();
        example.createCriteria().andParamKeyLike(ParamConstants.Classify.MAIL.getValue() + "%");
        List<SystemParameter> paramList = systemParameterMapper.selectByExample(example);
        Map<String, String> paramMap = paramList.stream().collect(Collectors.toMap(SystemParameter::getParamKey, p -> {
            if (StringUtils.equals(p.getParamKey(), ParamConstants.MAIL.PASSWORD.getValue())) {
                return EncryptUtils.aesDecrypt(p.getParamValue());
            }
            if (StringUtils.isEmpty(p.getParamValue())) {
                return "";
            } else {
                return p.getParamValue();
            }
        }));
        JavaMailSenderImpl javaMailSender = getMailSender(paramMap);
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        String username = javaMailSender.getUsername();
        String email;
        if (username.contains("@")) {
            email = username;
        } else {
            String mailHost = javaMailSender.getHost();
            String domainName = mailHost.substring(mailHost.indexOf(".") + 1);
            email = username + "@" + domainName;
        }
        InternetAddress from = new InternetAddress();
        String smtpFrom = paramMap.get(ParamConstants.MAIL.FROM.getValue());
        if (StringUtils.isBlank(smtpFrom)) {
            from.setAddress(email);
            from.setPersonal(username);
        } else {
            // 指定发件人后，address 应该是邮件服务器验证过的发件人
            if (smtpFrom.contains("@")) {
                from.setAddress(smtpFrom);
            } else {
                from.setAddress(email);
            }
            from.setPersonal(smtpFrom);
        }
        helper.setFrom(from);

        LogUtils.debug("发件人地址" + javaMailSender.getUsername());
        LogUtils.debug("helper" + helper);
        if (subject.length() > 60) {
            subject = subject.substring(0,59);
        }
        helper.setSubject("MeterSphere " + subject);

        LogUtils.info("收件人地址: {}", Arrays.asList(users));
        helper.setText(context, true);
        // 有抄送
        if (cc != null && cc.length > 0) {
            //设置抄送人 CC（Carbon Copy）
            helper.setCc(cc);
            // to 参数表示收件人
            helper.setTo(users);
            javaMailSender.send(mimeMessage);
        }
        // 无抄送
        else {
            for (String u : users) {
                helper.setTo(u);
                try {
                    javaMailSender.send(mimeMessage);
                } catch (Exception e) {
                    LogUtils.error("发送邮件失败: ", e);
                }
            }
        }
    }

    public void sendExternalMail(String context, NoticeModel noticeModel) throws Exception {

        List<String> userIds = noticeModel.getReceivers().stream()
                .map(Receiver::getUserId)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }

        List<String> recipients = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(noticeModel.getRecipients())) {
            recipients = noticeModel.getRecipients().stream()
                    .map(Receiver::getUserId)
                    .distinct()
                    .collect(Collectors.toList());
        }

        String[] users = userIds.stream()
                .distinct()
                .toArray(String[]::new);
        String[] ccArr = new String[0];
        if (CollectionUtils.isNotEmpty(recipients)) {
            ccArr = recipients.stream()
                    .distinct()
                    .toArray(String[]::new);
        }

        send(noticeModel.getSubject(), context, users, ccArr);
    }

    @Override
    public void send(MessageDetail messageDetail, NoticeModel noticeModel) {
        String context = super.getContext(messageDetail, noticeModel);
        String subjectText = super.getSubjectText(messageDetail, noticeModel);
        try {
            sendMail(context, noticeModel, messageDetail.getProjectId(), subjectText);
            LogUtils.debug("发送邮件结束");
        } catch (Exception e) {
            LogUtils.error(e);
        }
    }

    public JavaMailSenderImpl getMailSender(Map<String, String> paramMap) {
        Properties props = new Properties();
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setDefaultEncoding(StandardCharsets.UTF_8.name());
        javaMailSender.setProtocol("smtp");
        javaMailSender.setHost(paramMap.get(ParamConstants.MAIL.SERVER.getValue()));
        javaMailSender.setPort(Integer.parseInt(paramMap.get(ParamConstants.MAIL.PORT.getValue())));
        javaMailSender.setUsername(paramMap.get(ParamConstants.MAIL.ACCOUNT.getValue()));
        javaMailSender.setPassword(paramMap.get(ParamConstants.MAIL.PASSWORD.getValue()));

        if (BooleanUtils.toBoolean(paramMap.get(ParamConstants.MAIL.SSL.getValue()))) {
            javaMailSender.setProtocol("smtps");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }
        if (BooleanUtils.toBoolean(paramMap.get(ParamConstants.MAIL.TSL.getValue()))) {
            String result = BooleanUtils.toString(BooleanUtils.toBoolean(paramMap.get(ParamConstants.MAIL.TSL.getValue())), "true", "false");
            props.put("mail.smtp.starttls.enable", result);
            props.put("mail.smtp.starttls.required", result);
        }

        props.put("mail.smtp.ssl.trust", javaMailSender.getHost());
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.timeout", "30000");
        props.put("mail.smtp.connectiontimeout", "5000");
        javaMailSender.setJavaMailProperties(props);
        return javaMailSender;
    }

}
