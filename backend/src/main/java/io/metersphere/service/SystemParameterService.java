package io.metersphere.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.service.ApiTestEnvironmentService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.SystemHeaderMapper;
import io.metersphere.base.mapper.SystemParameterMapper;
import io.metersphere.base.mapper.UserHeaderMapper;
import io.metersphere.base.mapper.ext.ExtSystemParameterMapper;
import io.metersphere.commons.constants.ParamConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.EncryptUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.controller.request.HeaderRequest;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.ldap.domain.LdapInfo;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.system.SystemReference;
import io.metersphere.notice.domain.MailInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.UUID;


@Service
@Transactional(rollbackFor = Exception.class)
public class SystemParameterService {
    @Resource
    private UserHeaderMapper userHeaderMapper;
    @Resource
    private SystemParameterMapper systemParameterMapper;
    @Resource
    private ExtSystemParameterMapper extSystemParameterMapper;
    @Resource
    private SystemHeaderMapper systemHeaderMapper;
    @Resource
    private ApiTestEnvironmentService apiTestEnvironmentService;

    public String searchEmail() {
        return extSystemParameterMapper.email();
    }

    public String getSystemLanguage() {
        String result = StringUtils.EMPTY;
        SystemParameterExample example = new SystemParameterExample();
        example.createCriteria().andParamKeyEqualTo(ParamConstants.I18n.LANGUAGE.getValue());
        List<SystemParameter> list = systemParameterMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(list)) {
            String value = list.get(0).getParamValue();
            if (StringUtils.isNotBlank(value)) {
                result = value;
            }
        }
        return result;
    }

    public void editMail(List<SystemParameter> parameters) {
        List<SystemParameter> paramList = this.getParamList(ParamConstants.Classify.MAIL.getValue());
        boolean empty = paramList.size() <= 0;

        parameters.forEach(parameter -> {
            SystemParameterExample example = new SystemParameterExample();
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

    public List<SystemParameter> getParamList(String type) {
        SystemParameterExample example = new SystemParameterExample();
        example.createCriteria().andParamKeyLike(type + "%");
        return systemParameterMapper.selectByExample(example);
    }

    public void testConnection(HashMap<String, String> hashMap) {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setDefaultEncoding("UTF-8");
        javaMailSender.setHost(hashMap.get(ParamConstants.MAIL.SERVER.getValue()));
        javaMailSender.setPort(Integer.valueOf(hashMap.get(ParamConstants.MAIL.PORT.getValue())));
        javaMailSender.setUsername(hashMap.get(ParamConstants.MAIL.ACCOUNT.getValue()));
        javaMailSender.setPassword(hashMap.get(ParamConstants.MAIL.PASSWORD.getValue()));
        Properties props = new Properties();
        String recipients = hashMap.get(ParamConstants.MAIL.RECIPIENTS.getValue());
        if (BooleanUtils.toBoolean(hashMap.get(ParamConstants.MAIL.SSL.getValue()))) {
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }
        if (BooleanUtils.toBoolean(hashMap.get(ParamConstants.MAIL.TLS.getValue()))) {
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.ssl.trust", hashMap.get(ParamConstants.MAIL.SERVER.getValue()));

        }
        props.put("mail.smtp.timeout", "30000");
        props.put("mail.smtp.connectiontimeout", "5000");
        javaMailSender.setJavaMailProperties(props);
        try {
            javaMailSender.testConnection();
        } catch (MessagingException e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(Translator.get("connection_failed"));
        }
        if (!StringUtils.isBlank(recipients)) {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = null;
            try {
                helper = new MimeMessageHelper(mimeMessage, true);
                if (javaMailSender.getUsername().contains("@")) {
                    helper.setFrom(javaMailSender.getUsername());
                } else {
                    String mailHost = javaMailSender.getHost();
                    String domainName = mailHost.substring(mailHost.indexOf(".") + 1, mailHost.length());
                    helper.setFrom(javaMailSender.getUsername() + "@" + domainName);
                }
                helper.setSubject("MeterSphere测试邮件 ");
                helper.setText("这是一封测试邮件，邮件发送成功", true);
                helper.setTo(recipients);
                javaMailSender.send(mimeMessage);
            } catch (MessagingException e) {
                LogUtil.error(e.getMessage(), e);
                MSException.throwException(Translator.get("connection_failed"));
            }
        }


    }

    public String getVersion() {
        return System.getenv("MS_VERSION");
    }

    public MailInfo mailInfo(String type) {
        List<SystemParameter> paramList = this.getParamList(type);
        MailInfo mailInfo = new MailInfo();
        if (!CollectionUtils.isEmpty(paramList)) {
            for (SystemParameter param : paramList) {
                if (StringUtils.equals(param.getParamKey(), ParamConstants.MAIL.SERVER.getValue())) {
                    mailInfo.setHost(param.getParamValue());
                } else if (StringUtils.equals(param.getParamKey(), ParamConstants.MAIL.PORT.getValue())) {
                    mailInfo.setPort(param.getParamValue());
                } else if (StringUtils.equals(param.getParamKey(), ParamConstants.MAIL.ACCOUNT.getValue())) {
                    mailInfo.setAccount(param.getParamValue());
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

    public void saveLdap(List<SystemParameter> parameters) {
        SystemParameterExample example = new SystemParameterExample();
        parameters.forEach(param -> {
            if (param.getParamKey().equals(ParamConstants.LDAP.PASSWORD.getValue())) {
                String string = EncryptUtils.aesEncrypt(param.getParamValue()).toString();
                param.setParamValue(string);
            }
            example.createCriteria().andParamKeyEqualTo(param.getParamKey());
            if (systemParameterMapper.countByExample(example) > 0) {
                systemParameterMapper.updateByPrimaryKey(param);
            } else {
                systemParameterMapper.insert(param);
            }
            example.clear();
        });


    }

    public LdapInfo getLdapInfo(String type) {
        List<SystemParameter> params = getParamList(type);
        LdapInfo ldap = new LdapInfo();
        if (!CollectionUtils.isEmpty(params)) {
            for (SystemParameter param : params) {
                if (StringUtils.equals(param.getParamKey(), ParamConstants.LDAP.URL.getValue())) {
                    ldap.setUrl(param.getParamValue());
                } else if (StringUtils.equals(param.getParamKey(), ParamConstants.LDAP.DN.getValue())) {
                    ldap.setDn(param.getParamValue());
                } else if (StringUtils.equals(param.getParamKey(), ParamConstants.LDAP.PASSWORD.getValue())) {
                    String password = EncryptUtils.aesDecrypt(param.getParamValue()).toString();
                    ldap.setPassword(password);
                } else if (StringUtils.equals(param.getParamKey(), ParamConstants.LDAP.OU.getValue())) {
                    ldap.setOu(param.getParamValue());
                } else if (StringUtils.equals(param.getParamKey(), ParamConstants.LDAP.FILTER.getValue())) {
                    ldap.setFilter(param.getParamValue());
                } else if (StringUtils.equals(param.getParamKey(), ParamConstants.LDAP.MAPPING.getValue())) {
                    ldap.setMapping(param.getParamValue());
                } else if (StringUtils.equals(param.getParamKey(), ParamConstants.LDAP.OPEN.getValue())) {
                    ldap.setOpen(param.getParamValue());
                }
            }
        }
        return ldap;
    }


    /**
     * @param key System Param
     * @return 系统key对应的值 ｜ ""
     */
    public String getValue(String key) {
        SystemParameter param = systemParameterMapper.selectByPrimaryKey(key);
        if (param == null || StringUtils.isBlank(param.getParamValue())) {
            return "";
        }
        return param.getParamValue();
    }

    public BaseSystemConfigDTO getBaseInfo() {
        BaseSystemConfigDTO baseSystemConfigDTO = new BaseSystemConfigDTO();
        List<SystemParameter> paramList = this.getParamList(ParamConstants.Classify.BASE.getValue());
        if (!CollectionUtils.isEmpty(paramList)) {
            for (SystemParameter param : paramList) {
                if (StringUtils.equals(param.getParamKey(), ParamConstants.BASE.URL.getValue())) {
                    baseSystemConfigDTO.setUrl(param.getParamValue());
                }
                if (StringUtils.equals(param.getParamKey(), ParamConstants.BASE.CONCURRENCY.getValue())) {
                    baseSystemConfigDTO.setConcurrency(param.getParamValue());
                }
                if (StringUtils.equals(param.getParamKey(), ParamConstants.BASE.PROMETHEUS_HOST.getValue())) {
                    baseSystemConfigDTO.setPrometheusHost(param.getParamValue());
                }
            }
        }
        return baseSystemConfigDTO;
    }

    public void saveBaseInfo(List<SystemParameter> parameters) {
        SystemParameterExample example = new SystemParameterExample();

        parameters.forEach(param -> {
            // 去掉路径最后的 /
            param.setParamValue(StringUtils.removeEnd(param.getParamValue(), "/"));
            example.createCriteria().andParamKeyEqualTo(param.getParamKey());
            if (systemParameterMapper.countByExample(example) > 0) {
                systemParameterMapper.updateByPrimaryKey(param);
            } else {
                systemParameterMapper.insert(param);
            }
            example.clear();

            if (StringUtils.equals(param.getParamKey(), "base.url")) {
                apiTestEnvironmentService.checkMockEvnInfoByBaseUrl(param.getParamValue());
            }
        });
    }

    public void saveInitParam(String key) {
        SystemParameter parameter = new SystemParameter();
        parameter.setParamKey(key);
        parameter.setParamValue("over");
        parameter.setType("text");
        systemParameterMapper.insert(parameter);
    }


    //保存表头
    public void saveHeader(UserHeader userHeader) {
        UserHeaderExample example = new UserHeaderExample();
        example.createCriteria().andUserIdEqualTo(userHeader.getUserId()).andTypeEqualTo(userHeader.getType());
        if (userHeaderMapper.countByExample(example) > 0) {
            userHeaderMapper.deleteByExample(example);
            userHeader.setId(UUID.randomUUID().toString());
            userHeaderMapper.insert(userHeader);
        } else {
            userHeader.setId(UUID.randomUUID().toString());
            userHeaderMapper.insert(userHeader);
        }
        example.clear();
    }

    //默认表头
    public SystemHeader getHeader(String type) {
        return systemHeaderMapper.selectByPrimaryKey(type);
    }

    public UserHeader queryUserHeader(HeaderRequest headerRequest) {
        UserHeaderExample example = new UserHeaderExample();
        example.createCriteria().andUserIdEqualTo(headerRequest.getUserId()).andTypeEqualTo(headerRequest.getType());
        List<UserHeader> list = userHeaderMapper.selectByExample(example);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public String getLogDetails() {
        LdapInfo ldapInfo = this.getLdapInfo(ParamConstants.Classify.LDAP.getValue());
        if (ldapInfo != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(ldapInfo, SystemReference.ldapColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ldapInfo.getUrl()), null, "LDAP设置", null, columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getMailLogDetails() {
        MailInfo mailInfo = this.mailInfo(ParamConstants.Classify.MAIL.getValue());
        if (mailInfo != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(mailInfo, SystemReference.mailColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(mailInfo.getAccount()), null, "邮件设置", null, columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getBaseLogDetails() {
        BaseSystemConfigDTO configDTO = this.getBaseInfo();
        if (configDTO != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(configDTO, SystemReference.baseColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(configDTO.getUrl()), null, "基本配置", null, columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public void saveBaseurl(String baseurl) {
        extSystemParameterMapper.saveBaseurl(baseurl);
    }
}
