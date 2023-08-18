package io.metersphere.sdk.service;

import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.sdk.constants.ParamConstants;
import io.metersphere.sdk.dto.BaseSystemConfigDTO;
import io.metersphere.sdk.dto.EMailInfoDto;
import io.metersphere.sdk.dto.LogDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.log.constants.OperationLogModule;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.mapper.BaseSystemParameterMapper;
import io.metersphere.sdk.notice.sender.impl.MailNoticeSender;
import io.metersphere.sdk.util.EncryptUtils;
import io.metersphere.sdk.util.JSON;
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

import java.util.ArrayList;
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

    @Resource
    BaseSystemParameterMapper baseSystemParameterMapper;

    public void saveBaseInfo(List<SystemParameter> parameters) {
        SystemParameterExample example = new SystemParameterExample();
        parameters.forEach(param -> {
            param.setParamValue(StringUtils.removeEnd(param.getParamValue(), "/"));
            if (StringUtils.equals(param.getParamKey(), "base.url")) {
                example.createCriteria().andParamKeyEqualTo(param.getParamKey());
                List<SystemParameter> baseUrlParameterList = systemParameterMapper.selectByExample(example);
                String oldBaseUrl = null;
                if (CollectionUtils.isNotEmpty(baseUrlParameterList)) {
                    SystemParameter parameter = baseUrlParameterList.get(0);
                    if (!StringUtils.equals(parameter.getParamValue(), param.getParamValue())) {
                        oldBaseUrl = parameter.getParamValue();
                        systemParameterMapper.updateByPrimaryKey(param);
                    }
                } else {
                    systemParameterMapper.insert(param);
                }
                example.clear();
                if (StringUtils.isNotEmpty(oldBaseUrl)) {
                    //TODO 当前站点改变,mock环境的站点也需要改变
                }
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
                } else if (StringUtils.equals(param.getParamKey(), ParamConstants.MAIL.TSL.getValue())) {
                    mailInfo.setTsl(param.getParamValue());
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

    /**
     * 添加接口日志
     *
     * @return
     */
    public LogDTO updateBaseLog(List<SystemParameter> systemParameter) {
        List<SystemParameter> originalValue = getOriginalValue(systemParameter);
        LogDTO dto = new LogDTO(
                OperationLogConstants.SYSTEM,
                OperationLogConstants.SYSTEM,
                "system-parameter",
                null,
                OperationLogType.ADD.name(),
                OperationLogModule.SETTING_SYSTEM_PARAMETER,
                "基础设置");

        dto.setPath("/system/parameter/save/base-info");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(originalValue));
        return dto;
    }

    public LogDTO updateLog(List<SystemParameter> systemParameter) {
        List<SystemParameter> originalValue = getOriginalValue(systemParameter);
        LogDTO dto = new LogDTO(
                OperationLogConstants.SYSTEM,
                OperationLogConstants.SYSTEM,
                "system-parameter",
                null,
                OperationLogType.ADD.name(),
                OperationLogModule.SETTING_SYSTEM_PARAMETER,
                "基础设置");

        dto.setPath("/system/parameter/edit/email-info");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(originalValue));
        return dto;
    }

    private List<SystemParameter> getOriginalValue(List<SystemParameter> systemParameter) {
        SystemParameterExample example = new SystemParameterExample();
        List<SystemParameter> originalValue = new ArrayList<>();
        systemParameter.forEach(param -> {
            String paramKey = param.getParamKey();
            example.createCriteria().andParamKeyEqualTo(paramKey);
            List<SystemParameter> baseUrlParameterList = systemParameterMapper.selectByExample(example);
            originalValue.addAll(baseUrlParameterList);
            example.clear();
        });
        return originalValue;
    }

    public void saveBaseUrl(String baseUrl) {
        baseSystemParameterMapper.saveBaseUrl(baseUrl);
    }
}
