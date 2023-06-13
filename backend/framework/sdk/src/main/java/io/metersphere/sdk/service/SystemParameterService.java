package io.metersphere.sdk.service;

import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.sdk.constants.ParamConstants;
import io.metersphere.sdk.dto.EMailInfoDto;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.notice.sender.impl.MailNoticeSender;
import io.metersphere.sdk.util.EncryptUtils;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.SystemParameter;
import io.metersphere.system.domain.SystemParameterExample;
import io.metersphere.system.mapper.SystemParameterMapper;
import jakarta.annotation.Resource;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class SystemParameterService {

    @Resource
    SystemParameterMapper systemParameterMapper;
    @Resource
    MailNoticeSender mailNoticeSender;

    public void saveBaseInfo(List<SystemParameter> parameters) {
        SystemParameterExample example = new SystemParameterExample();
        parameters.forEach(param -> {
            param.setParamValue(StringUtils.removeEnd(param.getParamValue(), "/"));
            if (StringUtils.equals(param.getParamKey(), "base.url")) {
                example.createCriteria().andParamKeyEqualTo(param.getParamKey());
                List<SystemParameter> baseUrlParameterList = systemParameterMapper.selectByExample(example);
                if (CollectionUtils.isNotEmpty(baseUrlParameterList)) {
                    SystemParameter parameter = baseUrlParameterList.get(0);
                    if (!StringUtils.equals(parameter.getParamValue(), param.getParamValue())) {
                        systemParameterMapper.updateByPrimaryKey(param);
                    }
                } else {
                    systemParameterMapper.insert(param);
                }
                example.clear();
            } else {
                example.createCriteria().andParamKeyEqualTo(param.getParamKey());
                if (systemParameterMapper.countByExample(example) > 0) {
                    systemParameterMapper.updateByPrimaryKey(param);
                } else {
                    systemParameterMapper.insert(param);
                }
                example.clear();
            }
        });
    }

    public BaseSystemConfigDTO getBaseInfo() {
        List<SystemParameter> paramList = this.getParamList(ParamConstants.Classify.BASE.getValue());
        return TransBaseToDto(paramList);
    }

    private BaseSystemConfigDTO TransBaseToDto(List<SystemParameter> paramList) {
        BaseSystemConfigDTO baseSystemConfigDTO = new BaseSystemConfigDTO();
        if (!CollectionUtils.isEmpty(paramList)) {
            for (SystemParameter param : paramList) {
                if (StringUtils.equals(param.getParamKey(), ParamConstants.BASE.URL.getValue())) {
                    baseSystemConfigDTO.setUrl(param.getParamValue());
                }
                if (StringUtils.equals(param.getParamKey(), ParamConstants.BASE.PROMETHEUS_HOST.getValue())) {
                    baseSystemConfigDTO.setPrometheusHost(param.getParamValue());
                }
            }
        }
        return baseSystemConfigDTO;
    }


    public List<SystemParameter> getParamList(String type) {
        SystemParameterExample example = new SystemParameterExample();
        example.createCriteria().andParamKeyLike(type + "%");
        return systemParameterMapper.selectByExample(example);
    }

    public EMailInfoDto getEmailInfo() {
        List<SystemParameter> paramList = this.getParamList(ParamConstants.Classify.MAIL.getValue());
        return TransEmailToDto(paramList);
    }

    private EMailInfoDto TransEmailToDto(List<SystemParameter> paramList) {
        EMailInfoDto mailInfo = new EMailInfoDto();
        if (!CollectionUtils.isEmpty(paramList)) {
            for (SystemParameter param : paramList) {
                if (StringUtils.equals(param.getParamKey(), ParamConstants.MAIL.SERVER.getValue())) {
                    mailInfo.setHost(param.getParamValue());
                } else if (StringUtils.equals(param.getParamKey(), ParamConstants.MAIL.PORT.getValue())) {
                    mailInfo.setPort(param.getParamValue());
                } else if (StringUtils.equals(param.getParamKey(), ParamConstants.MAIL.ACCOUNT.getValue())) {
                    mailInfo.setAccount(param.getParamValue());
                } else if (StringUtils.equals(param.getParamKey(), ParamConstants.MAIL.FROM.getValue())) {
                    mailInfo.setFrom(param.getParamValue());
                } else if (StringUtils.equals(param.getParamKey(), ParamConstants.MAIL.PASSWORD.getValue())) {
                    String password = EncryptUtils.aesDecrypt(param.getParamValue()).toString();
                    mailInfo.setPassword(password);
                } else if (StringUtils.equals(param.getParamKey(), ParamConstants.MAIL.SSL.getValue())) {
                    mailInfo.setSsl(param.getParamValue());
                } else if (StringUtils.equals(param.getParamKey(), ParamConstants.MAIL.TLS.getValue())) {
                    mailInfo.setTls(param.getParamValue());
                } else if (StringUtils.equals(param.getParamKey(), ParamConstants.MAIL.RECIPIENTS.getValue())) {
                    mailInfo.setRecipient(param.getParamValue());
                }
            }
        }
        return mailInfo;
    }

    public void editEMailInfo(List<SystemParameter> parameters) {
        SystemParameterExample example = new SystemParameterExample();
        parameters.forEach(parameter -> {
            if (parameter.getParamKey().equals(ParamConstants.MAIL.PASSWORD.getValue())) {
                if (!StringUtils.isBlank(parameter.getParamValue())) {
                    String string = EncryptUtils.aesEncrypt(parameter.getParamValue()).toString();
                    parameter.setParamValue(string);
                }
            }
            example.createCriteria().andParamKeyEqualTo(parameter.getParamKey());
            if (systemParameterMapper.countByExample(example) > 0) {
                systemParameterMapper.updateByPrimaryKey(parameter);
            } else {
                systemParameterMapper.insert(parameter);
            }
            example.clear();
        });
    }

    public void testEmailConnection(HashMap<String, String> hashMap) {
        JavaMailSenderImpl javaMailSender = null;
        try {
            javaMailSender = mailNoticeSender.getMailSender(hashMap);
            javaMailSender.testConnection();
        } catch (Exception e) {
            LogUtils.error(e.getMessage(), e);
            throw new MSException(Translator.get("connection_failed"));
        }

        String recipients = hashMap.get(ParamConstants.MAIL.RECIPIENTS.getValue());
        if (!StringUtils.isBlank(recipients)) {
            try {
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
                String smtpFrom = hashMap.get(ParamConstants.MAIL.FROM.getValue());
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
                helper.setSubject("MeterSphere测试邮件");

                LogUtils.info("收件人地址: {}", Arrays.asList(recipients));
                helper.setText("这是一封测试邮件，邮件发送成功", true);
                helper.setTo(recipients);
                try {
                    javaMailSender.send(mimeMessage);
                } catch (Exception e) {
                    LogUtils.error("发送邮件失败: ", e);
                }
            } catch (Exception e) {
                LogUtils.error(e);
                throw new MSException(Translator.get("connection_failed"));
            }
        }
    }

}
