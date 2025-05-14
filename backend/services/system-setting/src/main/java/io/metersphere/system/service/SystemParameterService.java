package io.metersphere.system.service;

import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.sdk.constants.ParamConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.EncryptUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.SystemParameter;
import io.metersphere.system.domain.SystemParameterExample;
import io.metersphere.system.dto.sdk.BaseCleanConfigDTO;
import io.metersphere.system.dto.sdk.BaseSystemConfigDTO;
import io.metersphere.system.dto.sdk.EMailInfoDto;
import io.metersphere.system.dto.sdk.UploadInfoDTO;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.mapper.BaseSystemParameterMapper;
import io.metersphere.system.mapper.SystemParameterMapper;
import io.metersphere.system.notice.sender.impl.MailNoticeSender;
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

    private static final String DEFAULT_LOG_TIME = "6D";
    private static final String DEFAULT_HISTORY_TIME = "10";
    private static final String DEFAULT_API_CONCURRENT_CONFIG = "3";

    public void saveBaseInfo(List<SystemParameter> parameters) {
        SystemParameterExample example = new SystemParameterExample();
        parameters.forEach(param -> {
            param.setParamValue(StringUtils.removeEnd(param.getParamValue(), "/"));
            if (StringUtils.equals(param.getParamKey(), "base.url")) {
                example.createCriteria().andParamKeyEqualTo(param.getParamKey());
                List<SystemParameter> baseUrlParameterList = systemParameterMapper.selectByExample(example);
                String oldBaseUrl = null;
                if (CollectionUtils.isNotEmpty(baseUrlParameterList)) {
                    SystemParameter parameter = baseUrlParameterList.getFirst();
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
        BaseSystemConfigDTO baseSystemConfig = transBaseToDto(paramList);
        UploadInfoDTO uploadConfigInfo = getUploadConfigInfo();
        baseSystemConfig.setFileMaxSize(uploadConfigInfo.getFileSize());
        return baseSystemConfig;
    }

    private BaseSystemConfigDTO transBaseToDto(List<SystemParameter> paramList) {
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
                    String password = EncryptUtils.aesDecrypt(param.getParamValue());
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

    public void editUploadConfigInfo(List<SystemParameter> parameters) {
        SystemParameterExample example = new SystemParameterExample();
        SystemParameter uploadConfig = parameters.getFirst();
        if (StringUtils.equals(uploadConfig.getParamKey(), ParamConstants.UploadConfig.UPLOAD_FILE_SIZE.getValue())) {
            example.createCriteria().andParamKeyEqualTo(uploadConfig.getParamKey());
            if (systemParameterMapper.countByExample(example) > 0) {
                systemParameterMapper.updateByPrimaryKey(uploadConfig);
            } else {
                systemParameterMapper.insert(uploadConfig);
            }
            example.clear();
        } else {
            throw new MSException(Translator.get("upload_config_save_param_error"));
        }
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
                OperationLogConstants.SYSTEM,
                null,
                OperationLogType.UPDATE.name(),
                OperationLogModule.SETTING_SYSTEM_PARAMETER_BASE_CONFIG,
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
                OperationLogConstants.SYSTEM,
                null,
                OperationLogType.UPDATE.name(),
                OperationLogModule.SETTING_SYSTEM_PARAMETER_BASE_CONFIG,
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


    public void editLogConfig(List<SystemParameter> systemParameter) {
        systemParameter.forEach(parameter -> {
            SystemParameterExample example = new SystemParameterExample();
            example.createCriteria().andParamKeyEqualTo(parameter.getParamKey());
            if (systemParameterMapper.countByExample(example) > 0) {
                systemParameterMapper.updateByPrimaryKey(parameter);
            } else {
                systemParameterMapper.insert(parameter);
            }
        });
    }


    public LogDTO cleanOperationConfigLog(List<SystemParameter> systemParameter) {
        List<SystemParameter> originalValue = getOriginalValue(systemParameter);
        LogDTO dto = new LogDTO(
                OperationLogConstants.SYSTEM,
                OperationLogConstants.SYSTEM,
                OperationLogConstants.SYSTEM,
                null,
                OperationLogType.UPDATE.name(),
                OperationLogModule.SETTING_SYSTEM_PARAMETER_BASE_CONFIG,
                "基础设置");

        dto.setPath("/system/parameter/edit/clean-config");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(originalValue));
        return dto;
    }

    public BaseCleanConfigDTO getLogConfigInfo() {
        List<SystemParameter> paramList = this.getParamList((ParamConstants.Classify.CLEAN_CONFIG.getValue()));
        return transCleanConfigToDto(paramList);
    }

    public UploadInfoDTO getUploadConfigInfo() {
        List<SystemParameter> paramList = this.getParamList(ParamConstants.Classify.UPLOAD_CONFIG.getValue());
        return transUploadConfigToDto(paramList);
    }

    private BaseCleanConfigDTO transCleanConfigToDto(List<SystemParameter> paramList) {
        BaseCleanConfigDTO configDTO = new BaseCleanConfigDTO();
        if (CollectionUtils.isNotEmpty(paramList)) {
            paramList.forEach(param -> {
                if (StringUtils.equals(param.getParamKey(), ParamConstants.CleanConfig.OPERATION_LOG.getValue())) {
                    configDTO.setOperationLog(StringUtils.defaultIfBlank(param.getParamValue(), DEFAULT_LOG_TIME));
                } else if (StringUtils.equals(param.getParamKey(), ParamConstants.CleanConfig.OPERATION_HISTORY.getValue())) {
                    configDTO.setOperationHistory(StringUtils.defaultIfBlank(param.getParamValue(), DEFAULT_HISTORY_TIME));
                }
            });
        }
        return configDTO;
    }

    private UploadInfoDTO transUploadConfigToDto(List<SystemParameter> paramList) {
        UploadInfoDTO configDTO = new UploadInfoDTO();
        if (CollectionUtils.isNotEmpty(paramList)) {
            paramList.forEach(param -> {
                if (StringUtils.equals(param.getParamKey(), ParamConstants.UploadConfig.UPLOAD_FILE_SIZE.getValue())) {
                    configDTO.setFileSize(param.getParamValue());
                }
            });
        }
        return configDTO;
    }

    public String getApiConcurrentConfig() {
        List<SystemParameter> paramList = this.getParamList(ParamConstants.ApiConcurrentConfig.API_CONCURRENT_CONFIG.getValue());
        if (CollectionUtils.isNotEmpty(paramList)) {
            return paramList.getFirst().getParamValue();
        }
        return DEFAULT_API_CONCURRENT_CONFIG;
    }
}
