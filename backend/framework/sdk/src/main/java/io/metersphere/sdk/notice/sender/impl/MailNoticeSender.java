package io.metersphere.sdk.notice.sender.impl;


import io.metersphere.sdk.constants.ParamConstants;
import io.metersphere.sdk.notice.sender.AbstractNoticeSender;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

@Component
public class MailNoticeSender extends AbstractNoticeSender {


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
