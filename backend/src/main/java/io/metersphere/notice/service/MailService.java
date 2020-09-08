package io.metersphere.notice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {
   /* @Autowired
    private JavaMailSender mailSender;
    @Value("${mail.fromMail.addr}")
    private String from;*/

    public void sendHtml(String id) {
        SimpleMailMessage message = new SimpleMailMessage();
/*
        message.setFrom(from);
*/
        message.setTo("15135125273@163.com");
        message.setSubject("测试报告");
        message.setText("全部通知");
        /*try {
            mailSender.send(message);
        } catch (Exception e) {
        }*/

    }
}
