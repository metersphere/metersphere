package io.metersphere.notice.sender.impl;

import io.metersphere.commons.utils.LogUtil;
import io.metersphere.notice.domain.MessageDetail;
import io.metersphere.notice.sender.AbstractNoticeSender;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.service.MailService;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.RegExUtils;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Map;

@Component
public class MailNoticeSender extends AbstractNoticeSender {
    @Resource
    private MailService mailService;

    private void sendMail(MessageDetail messageDetail, String template, NoticeModel noticeModel) throws MessagingException {
        JavaMailSenderImpl javaMailSender = mailService.getMailSender();
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setFrom(javaMailSender.getUsername());
        helper.setSubject("MeterSphere " + noticeModel.getSubject());
        List<String> emails = super.getUserEmails(messageDetail.getUserIds());
        String[] users = emails.toArray(new String[0]);
        LogUtil.info("收件人地址: " + emails);
        helper.setText(this.getContent(template, noticeModel.getParamMap()), true);
        helper.setTo(users);
        javaMailSender.send(mimeMessage);
    }


    public String getContent(String template, Map<String, Object> context) {
        if (MapUtils.isNotEmpty(context)) {
            for (String k : context.keySet()) {
                if (context.get(k) != null) {
                    template = RegExUtils.replaceAll(template, "\\$\\{" + k + "}", context.get(k).toString());
                } else {
                    template = RegExUtils.replaceAll(template, "\\$\\{" + k + "}", "未设置");
                }
            }
        }
        return template;
    }

    @Override
    public void send(MessageDetail messageDetail, NoticeModel noticeModel) {
        String context = super.getHtmlContext(messageDetail, noticeModel);
        try {
            sendMail(messageDetail, context, noticeModel);
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }
}
