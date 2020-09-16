package io.metersphere.notice.service;

import io.metersphere.api.dto.APIReportResult;
import io.metersphere.api.service.APIReportService;
import io.metersphere.base.domain.Notice;
import io.metersphere.base.domain.SystemParameter;
import io.metersphere.commons.constants.ParamConstants;
import io.metersphere.commons.utils.EncryptUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.LoadTestDTO;
import io.metersphere.performance.service.PerformanceTestService;
import io.metersphere.service.SystemParameterService;
import io.metersphere.service.UserService;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service
public class MailService {
    @Resource
    private APIReportService apiReportService;
    @Resource
    private PerformanceTestService performanceTestService;
    @Resource
    private UserService userService;
    @Resource
    private SystemParameterService systemParameterService;

    public void sendHtml(String id, List<Notice> notice,String type) {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        List<SystemParameter> paramList=systemParameterService.getParamList(ParamConstants.Classify.MAIL.getValue());
        javaMailSender.setDefaultEncoding("UTF-8");
        javaMailSender.setProtocol("smtps");
        for(SystemParameter p:paramList){
                if(p.getParamKey().equals("smtp.host")){
                    javaMailSender.setHost(p.getParamValue());
                }
                if(p.getParamKey().equals("smtp.port")){
                    javaMailSender.setPort(Integer.parseInt(p.getParamValue()));
                }
                if(p.getParamKey().equals("smtp.account")){
                    javaMailSender.setUsername(p.getParamValue());
                }
            if(p.getParamKey().equals("smtp.password")){
                    javaMailSender.setPassword(EncryptUtils.aesDecrypt(p.getParamValue()).toString());
                }
            }
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable","true");
        props.put("mail.smtp.starttls.required","true");
        props.put("mail.smtp.timeout", "30000");
        props.put("mail.smtp.connectiontimeout", "5000");
        javaMailSender.setJavaMailProperties(props);
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        String testName="";
        String state="";
        if(type.equals("api")){
            APIReportResult reportResult=apiReportService.get(id);
            testName=reportResult.getTestName();
            state=reportResult.getStatus();
        }else if(type.equals("performance")){
            LoadTestDTO  performanceResult=performanceTestService.get(id);
            testName=performanceResult.getName();
            state=performanceResult.getStatus();
        }
        String html1="<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\">\n" +
                "  <title>MeterSphere</title>\n" +
                "</head>\n" +
                "<body style=\"text-align: left\">\n" +
                "    <div>\n" +
                "      <h3>"+type+"定时任务结果通知</h3>\n" +
                "      <p>   尊敬的用户：您好，您所执行的"+testName+"运行失败，请点击报告链接查看</p>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
        String html2="<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\">\n" +
                "  <title>MeterSphere</title>\n" +
                "</head>\n" +
                "<body style=\"text-align: left\">\n" +
                "    <div>\n" +
                "      <h3>"+type+"定时任务结果通知</h3>\n" +
                "      <p>    尊敬的用户：您好，"+testName+"运行成功，请点击报告链接查看</p>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(javaMailSender.getUsername());
            helper.setSubject("MeterSphere定时任务结果通知");
            String users[] = {};
            List<String> successEmailList=new ArrayList<>();
            List<String> failEmailList=new ArrayList<>();
            if(notice.size()>0){
                for(Notice n:notice){
                    if (n.getEnable().equals("true")&&n.getEvent().equals("执行成功")) {
                        successEmailList=userService.queryEmail(n.getNames());
                    }
                    if (n.getEnable().equals("true")&&n.getEvent().equals("执行失败")) {
                        failEmailList=userService.queryEmail(n.getNames());
                    }

                }
            }else{
                LogUtil.error("Recipient information is empty");
            }

            if(state.equals("success")){
                users=successEmailList.toArray(new String[successEmailList.size()]);
                helper.setText(html2,true);
            }else{
                users=failEmailList.toArray(new String[failEmailList.size()]);
                helper.setText(html1,true);

            }
            helper.setTo(users);
            javaMailSender.send(mimeMessage);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}

