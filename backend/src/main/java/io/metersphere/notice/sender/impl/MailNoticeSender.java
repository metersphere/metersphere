package io.metersphere.notice.sender.impl;

import io.metersphere.commons.utils.LogUtil;
import io.metersphere.notice.domain.MessageDetail;
import io.metersphere.notice.domain.Receiver;
import io.metersphere.notice.domain.UserDetail;
import io.metersphere.notice.sender.AbstractNoticeSender;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.service.MailService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MailNoticeSender extends AbstractNoticeSender {
    @Resource
    private MailService mailService;

    public void sendMail(String context, NoticeModel noticeModel) throws MessagingException {
        LogUtil.debug("发送邮件开始 ");
        JavaMailSenderImpl javaMailSender = mailService.getMailSender();
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        //helper.setFrom(javaMailSender.getUsername());
        if (javaMailSender.getUsername().contains("@")) {
            helper.setFrom(javaMailSender.getUsername());
        } else {
            String mailHost = javaMailSender.getHost();
            String domainName = mailHost.substring(mailHost.indexOf(".") + 1, mailHost.length());
            helper.setFrom(javaMailSender.getUsername() + "@" + domainName);
        }
        LogUtil.debug("发件人地址" + javaMailSender.getUsername());
        LogUtil.debug("helper" + helper);
        helper.setSubject("MeterSphere " + noticeModel.getSubject());
        List<String> userIds = noticeModel.getReceivers().stream()
                .map(Receiver::getUserId)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }

        String[] users = super.getUserDetails(userIds).stream()
                .map(UserDetail::getEmail)
                .distinct()
                .toArray(String[]::new);

        LogUtil.info("收件人地址: {}", userIds);
        helper.setText(context, true);
        helper.setTo(users);
        javaMailSender.send(mimeMessage);
    }

    public void sendExternalMail(String context, NoticeModel noticeModel) throws MessagingException {
        LogUtil.debug("发送邮件开始 ");
        JavaMailSenderImpl javaMailSender = mailService.getMailSender();
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        if (javaMailSender.getUsername().contains("@")) {
            helper.setFrom(javaMailSender.getUsername());
        } else {
            String mailHost = javaMailSender.getHost();
            String domainName = mailHost.substring(mailHost.indexOf(".") + 1, mailHost.length());
            helper.setFrom(javaMailSender.getUsername() + "@" + domainName);
        }
        LogUtil.debug("发件人地址" + javaMailSender.getUsername());
        LogUtil.debug("helper" + helper);
        helper.setSubject(noticeModel.getSubject());
        List<String> userIds = noticeModel.getReceivers().stream()
                .map(Receiver::getUserId)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }

        List<String> recipients = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(noticeModel.getRecipients())){
            recipients = noticeModel.getRecipients().stream()
                    .map(Receiver::getUserId)
                    .distinct()
                    .collect(Collectors.toList());
        }

        String[] users = userIds.stream()
                .distinct()
                .toArray(String[]::new);

        LogUtil.info("收件人地址: {}", userIds);
        helper.setText(context, true);
        helper.setTo(users);

        if(CollectionUtils.isNotEmpty(recipients)){
            String[] ccArr = recipients.stream()
                    .distinct()
                    .toArray(String[]::new);
            helper.setCc(ccArr);
        }

        javaMailSender.send(mimeMessage);
    }

    @Override
    public void send(MessageDetail messageDetail, NoticeModel noticeModel) {
        String context = super.getHtmlContext(messageDetail, noticeModel);
        try {
            sendMail(context, noticeModel);
            LogUtil.debug("发送邮件结束");
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }
}
