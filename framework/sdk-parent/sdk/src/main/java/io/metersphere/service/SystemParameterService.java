package io.metersphere.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.BaseSystemParameterMapper;
import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.commons.constants.ParamConstants;
import io.metersphere.commons.constants.ProjectApplicationType;
import io.metersphere.commons.constants.ResourceStatusEnum;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.EncryptUtils;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.dto.TestResourcePoolDTO;
import io.metersphere.environment.service.BaseEnvironmentService;
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
import io.metersphere.request.HeaderRequest;
import io.metersphere.request.resourcepool.QueryResourcePoolRequest;
import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@Transactional(rollbackFor = Exception.class)
public class SystemParameterService {
    @Resource
    private UserHeaderMapper userHeaderMapper;
    @Resource
    private SystemParameterMapper systemParameterMapper;
    @Resource
    private BaseSystemParameterMapper baseSystemParameterMapper;
    @Resource
    private SystemHeaderMapper systemHeaderMapper;
    @Resource
    private MailNoticeSender mailNoticeSender;
    @Resource
    private BaseEnvironmentService baseEnvironmentService;
    @Resource
    private MicroService microService;
    @Resource
    private BaseTestResourcePoolService baseTestResourcePoolService;
    @Resource
    private BaseProjectService baseProjectService;
    @Resource
    private BaseProjectApplicationService baseProjectApplicationService;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private ProjectApplicationMapper projectApplicationMapper;

    public String searchEmail() {
        return baseSystemParameterMapper.email();
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
            NoticeModel noticeModel = NoticeModel.builder()
                    .subject("MeterSphere测试邮件")
                    .receivers(Arrays.asList(new Receiver(recipients, recipients)))
                    .build();
            try {
                mailNoticeSender.sendExternalMail("这是一封测试邮件，邮件发送成功", noticeModel);
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
            return StringUtils.EMPTY;
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
                if (StringUtils.equals(param.getParamKey(), ParamConstants.BASE.DOC_URL.getValue())) {
                    baseSystemConfigDTO.setDocUrl(param.getParamValue());
                }
            }
        }
        return baseSystemConfigDTO;
    }

    public TestResourcePoolDTO getTestResourcePool() {
        QueryResourcePoolRequest resourcePoolRequest = new QueryResourcePoolRequest();
        resourcePoolRequest.setStatus(ResourceStatusEnum.VALID.name());
        List<TestResourcePoolDTO> poolDTOS = baseTestResourcePoolService.listResourcePools(resourcePoolRequest);
        if (CollectionUtils.isNotEmpty(poolDTOS)) {
            List<TestResourcePoolDTO> localPools = poolDTOS.stream().filter(item ->
                    StringUtils.equals(item.getName(), "LOCAL")).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(localPools)) {
                return localPools.get(0);
            } else {
                return poolDTOS.get(0);
            }
        }
        return null;
    }

    public void batchSaveApp(List<String> projectIds, String poolId, ProjectApplicationMapper batchMapper) {
        if (CollectionUtils.isNotEmpty(projectIds)) {
            List<String> appProjectIds = baseProjectApplicationService.getProjectIds(projectIds);
            projectIds.removeAll(appProjectIds);
            // 添加
            projectIds.forEach(item -> {
                ProjectApplication application = new ProjectApplication();
                application.setProjectId(item);
                application.setType(ProjectApplicationType.POOL_ENABLE.name());
                application.setTypeValue(String.valueOf(true));
                batchMapper.insert(application);

                ProjectApplication applicationValue = new ProjectApplication();
                applicationValue.setProjectId(item);
                applicationValue.setType(ProjectApplicationType.RESOURCE_POOL_ID.name());
                applicationValue.setTypeValue(poolId);
                batchMapper.insert(applicationValue);
            });
        }
    }

    public void batchSaveProjectApp(String poolId, long size) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ProjectApplicationMapper batchMapper = sqlSession.getMapper(ProjectApplicationMapper.class);
        final long pageSize = 500;
        int pageTotal = (int) Math.ceil(size / pageSize);
        for (int i = 0; i <= pageTotal; i++) {
            long pageNum = i * pageSize;
            List<String> projectIds = baseProjectService.getPage(pageNum, pageSize);
            if (CollectionUtils.isNotEmpty(projectIds)) {
                batchSaveApp(projectIds, poolId, batchMapper);
            }
        }
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    public void saveProjectApplication(String projectId) {
        BaseSystemConfigDTO config = getBaseInfo();
        if (config != null && StringUtils.equals(config.getRunMode(), "POOL")) {
            TestResourcePoolDTO poolDTO = getTestResourcePool();
            if (poolDTO != null) {
                ProjectApplication applicationValue = new ProjectApplication();
                applicationValue.setProjectId(projectId);
                applicationValue.setType(ProjectApplicationType.RESOURCE_POOL_ID.name());
                applicationValue.setTypeValue(poolDTO.getId());
                projectApplicationMapper.insert(applicationValue);

                ProjectApplication application = new ProjectApplication();
                application.setProjectId(projectId);
                application.setType(ProjectApplicationType.POOL_ENABLE.name());
                application.setTypeValue(String.valueOf(true));
                projectApplicationMapper.insert(application);
            }
        }
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
                        microService.getForData(MicroServiceName.API_TEST, "/exec/thread/pool/set-core-size/" + poolSize);
                    } catch (Exception e) {
                        MSException.throwException("并发数设置不规范，请重新设置");
                    }
                }
            }
            // 启用资源池
            if (param.getParamKey().equals("base.run.mode") && param.getParamValue().equals("POOL")) {
                TestResourcePoolDTO poolDTO = getTestResourcePool();
                if (poolDTO != null) {
                    long size = baseProjectService.count();
                    batchSaveProjectApp(poolDTO.getId(), size);
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
                    baseEnvironmentService.checkMockEvnInfoByBaseUrl(param.getParamValue());
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
        baseSystemParameterMapper.saveBaseurl(baseurl);
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
}
