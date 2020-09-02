package io.metersphere.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailServiceImpl implements MailService {
    @Autowired
    private JavaMailSender mailSender;
    @Value("${mail.fromMail.addr}")
    private String mailFrom;

    @Override
    public void sendMail(String reportId) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo("15135125273@163.com", "wenyan.yang@fit2cloud.com");
        message.setSubject("automatic");
        message.setText("自动邮件发布");
        try {
            mailSender.send(message);
            System.out.println("发送简单邮件");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("发送简单邮件失败");
        }

    }
}
