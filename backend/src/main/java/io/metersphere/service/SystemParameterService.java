package io.metersphere.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.exec.ApiPoolDebugService;
import io.metersphere.api.exec.queue.ExecThreadPoolExecutor;
import io.metersphere.api.service.ApiTestEnvironmentService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.SystemHeaderMapper;
import io.metersphere.base.mapper.SystemParameterMapper;
import io.metersphere.base.mapper.UserHeaderMapper;
import io.metersphere.base.mapper.ext.ExtSystemParameterMapper;
import io.metersphere.commons.constants.ParamConstants;
import io.metersphere.commons.constants.ResourceStatusEnum;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.EncryptUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.controller.request.HeaderRequest;
import io.metersphere.controller.request.resourcepool.QueryResourcePoolRequest;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.dto.TestResourcePoolDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.ldap.domain.LdapInfo;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.system.SystemReference;
import io.metersphere.notice.domain.MailInfo;
import io.metersphere.notice.domain.Receiver;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.sender.impl.MailNoticeSender;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.*;
import java.util.stream.Collectors;


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
    @Resource
    private MailNoticeSender mailNoticeSender;
    @Resource
    private TestResourcePoolService testResourcePoolService;

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
        JavaMailSenderImpl javaMailSender = mailNoticeSender.getMailSender(hashMap);
        try {
            javaMailSender.testConnection();
        } catch (MessagingException e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(Translator.get("connection_failed"));
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

                LogUtil.debug("发件人地址" + javaMailSender.getUsername());
                LogUtil.debug("helper" + helper);
                helper.setSubject("MeterSphere测试邮件");

                LogUtil.info("收件人地址: {}", Arrays.asList(recipients));
                helper.setText("这是一封测试邮件，邮件发送成功", true);
                helper.setTo(recipients);
                try {
                    javaMailSender.send(mimeMessage);
                } catch (Exception e) {
                    LogUtil.error("发送邮件失败: ", e);
                }
            } catch (Exception e) {
                LogUtil.error(e);
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
                if (StringUtils.equals(param.getParamKey(), ParamConstants.BASE.SELENIUM_DOCKER_URL.getValue())) {
                    baseSystemConfigDTO.setSeleniumDockerUrl(param.getParamValue());
                }
                if (StringUtils.equals(param.getParamKey(), ParamConstants.BASE.RUN_MODE.getValue())) {
                    baseSystemConfigDTO.setRunMode(param.getParamValue());
                }
            }
        }
        return baseSystemConfigDTO;
    }

    public void saveBaseInfo(List<SystemParameter> parameters) {
        SystemParameterExample example = new SystemParameterExample();

        parameters.forEach(param -> {
            if (param.getParamKey().equals("base.concurrency")) {
                if (StringUtils.isNotEmpty(param.getParamValue())) {
                    try {
                        int poolSize = Integer.parseInt(param.getParamValue());
                        if (poolSize > 10000) {
                            MSException.throwException("并发数设置太大，请重新设置");
                        }
                        CommonBeanFactory.getBean(ExecThreadPoolExecutor.class).setCorePoolSize(poolSize);
                    } catch (Exception e) {
                        MSException.throwException("并发数设置不规范，请重新设置");
                    }
                }
            }
            // 去掉路径最后的 /
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
                    apiTestEnvironmentService.batchUpdateMockEvnInfoByBaseUrl(oldBaseUrl, param.getParamValue());
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

    public SystemParameter getInfo(String key) {
        return systemParameterMapper.selectByPrimaryKey(key);
    }

    public void editInfo(SystemParameter systemParameter) {
        if (StringUtils.isBlank(systemParameter.getParamKey())) {
            return;
        }
        systemParameterMapper.updateByPrimaryKeySelective(systemParameter);
    }

    public List<TestResourcePoolDTO> getTestResourcePool() {
        QueryResourcePoolRequest resourcePoolRequest = new QueryResourcePoolRequest();
        resourcePoolRequest.setStatus(ResourceStatusEnum.VALID.name());
        return testResourcePoolService.listResourcePools(resourcePoolRequest);
    }

    public String filterQuota(List<TestResourcePoolDTO> list) {
        QuotaService baseQuotaService = CommonBeanFactory.getBean(QuotaService.class);
        Set<String> pools = baseQuotaService.getQuotaResourcePools();
        List<TestResourcePoolDTO> poolList = new ArrayList<>();
        if (!pools.isEmpty()) {
            poolList = list.stream().filter(pool -> pools.contains(pool.getId())).collect(Collectors.toList());
        }
        if (CollectionUtils.isNotEmpty(poolList)) {
            return getPreLocalPoolId(poolList);
        } else if (CollectionUtils.isNotEmpty(list)) {
            return getPreLocalPoolId(list);
        }
        return null;
    }

    String getPreLocalPoolId(List<TestResourcePoolDTO> poolList) {
        if (CollectionUtils.isEmpty(poolList)) {
            return null;
        }
        List<TestResourcePoolDTO> poolDTOS = poolList.stream().filter(pool ->
                StringUtils.equals(pool.getName(), "LOCAL")).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(poolDTOS)) {
            return poolDTOS.get(0).getId();
        } else {
            return poolList.get(0).getId();
        }
    }
}
