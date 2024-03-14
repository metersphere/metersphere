package io.metersphere.api.service;

import io.metersphere.project.api.KeyValueEnableParam;
import io.metersphere.project.api.assertion.MsResponseCodeAssertion;
import io.metersphere.project.api.processor.MsProcessor;
import io.metersphere.project.api.processor.SQLProcessor;
import io.metersphere.project.dto.environment.EnvironmentConfig;
import io.metersphere.project.dto.environment.EnvironmentGroupProjectDTO;
import io.metersphere.project.dto.environment.EnvironmentGroupRequest;
import io.metersphere.project.dto.environment.EnvironmentRequest;
import io.metersphere.project.dto.environment.datasource.DataSource;
import io.metersphere.project.dto.environment.http.HttpConfig;
import io.metersphere.project.dto.environment.http.HttpConfigPathMatchRule;
import io.metersphere.project.dto.environment.http.SelectModule;
import io.metersphere.project.dto.environment.processors.EnvProcessorConfig;
import io.metersphere.project.dto.environment.processors.EnvRequestScriptProcessor;
import io.metersphere.project.dto.environment.processors.EnvScenarioScriptProcessor;
import io.metersphere.project.dto.environment.variables.CommonVariables;
import io.metersphere.project.service.EnvironmentGroupService;
import io.metersphere.project.service.EnvironmentService;
import io.metersphere.sdk.constants.MsAssertionCondition;
import io.metersphere.sdk.constants.VariableTypeConstants;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.domain.EnvironmentGroup;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.base.BaseTest;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @Author: jianxing
 * @CreateTime: 2023-10-20  11:32
 */
@Service
public class BaseEnvTestService {
    @Resource
    private EnvironmentService environmentService;
    @Resource
    private EnvironmentGroupService environmentGroupService;

    public Environment initEnv(String moduleId) {
        EnvironmentRequest envRequest = new EnvironmentRequest();
        envRequest.setProjectId(BaseTest.DEFAULT_PROJECT_ID);
        envRequest.setName(UUID.randomUUID().toString());
        // 添加插件的环境配置，供后续测试使用
        Map<String, Map<String, Object>> pluginConfigMap = new HashMap<>();
        pluginConfigMap.put("tcpp-sampler", new HashMap<>());

        EnvScenarioScriptProcessor envScenarioScriptProcessor = new EnvScenarioScriptProcessor();
        envScenarioScriptProcessor.setScript("test");
        envScenarioScriptProcessor.setEnableCommonScript(false);
        envScenarioScriptProcessor.setAssociateScenarioResult(true);
        EnvScenarioScriptProcessor envScenarioScriptProcessor1 = BeanUtils.copyBean(new EnvScenarioScriptProcessor(), envScenarioScriptProcessor);
        envScenarioScriptProcessor1.setAssociateScenarioResult(false);
        SQLProcessor sqlProcessor = new SQLProcessor();
        sqlProcessor.setScript("select * from test");
        sqlProcessor.setName("select * from test");

        EnvironmentConfig environmentConfig = new EnvironmentConfig();
        DataSource dataSource = getDataSource();
        environmentConfig.setDataSources(List.of(dataSource));

        CommonVariables commonVariables1 = new CommonVariables();
        commonVariables1.setParamType(VariableTypeConstants.CONSTANT.name());
        commonVariables1.setKey("a");
        commonVariables1.setValue("c");
        CommonVariables commonVariables2 = new CommonVariables();
        commonVariables2.setParamType(VariableTypeConstants.CONSTANT.name());
        commonVariables2.setKey("q");
        commonVariables2.setValue("qq");
        CommonVariables commonVariables3 = new CommonVariables();
        commonVariables3.setParamType(VariableTypeConstants.LIST.name());
        commonVariables3.setKey("list1");
        commonVariables3.setValue("1,2,3,5");
        environmentConfig.setCommonVariables(List.of(commonVariables1, commonVariables2, commonVariables3));

        EnvProcessorConfig preProcessorConfig = environmentConfig.getPreProcessorConfig();
        EnvProcessorConfig postProcessorConfig = environmentConfig.getPostProcessorConfig();
        List<MsProcessor> preProcessors = preProcessorConfig.getApiProcessorConfig().getScenarioProcessorConfig().getProcessors();
        preProcessors.add(envScenarioScriptProcessor);
        preProcessors.add(envScenarioScriptProcessor1);
        preProcessors.add(sqlProcessor);

        List<MsProcessor> postProcessors = postProcessorConfig.getApiProcessorConfig().getScenarioProcessorConfig().getProcessors();
        postProcessors.add(envScenarioScriptProcessor);
        postProcessors.add(envScenarioScriptProcessor1);
        postProcessors.add(sqlProcessor);

        EnvRequestScriptProcessor envRequestScriptProcessor = new EnvRequestScriptProcessor();
        envRequestScriptProcessor.setScript("test");
        envRequestScriptProcessor.setBeforeStepScript(true);
        envRequestScriptProcessor.setIgnoreProtocols(List.of("TCP"));
        EnvRequestScriptProcessor envRequestScriptProcessor1 = new EnvRequestScriptProcessor();
        envRequestScriptProcessor1.setScript("test1");
        envRequestScriptProcessor1.setBeforeStepScript(false);
        envRequestScriptProcessor1.setIgnoreProtocols(List.of());
        List<MsProcessor> preRequestProcessors = preProcessorConfig.getApiProcessorConfig().getRequestProcessorConfig().getProcessors();
        preRequestProcessors.add(envRequestScriptProcessor);
        preRequestProcessors.add(sqlProcessor);
        List<MsProcessor> postRequestProcessors = postProcessorConfig.getApiProcessorConfig().getRequestProcessorConfig().getProcessors();
        postRequestProcessors.add(envRequestScriptProcessor);
        postRequestProcessors.add(sqlProcessor);

        MsResponseCodeAssertion responseCodeAssertion = new MsResponseCodeAssertion();
        responseCodeAssertion.setExpectedValue("200");
        responseCodeAssertion.setCondition(MsAssertionCondition.EMPTY.name());
        responseCodeAssertion.setName("test");
        environmentConfig.getAssertionConfig().getAssertions().add(responseCodeAssertion);

        KeyValueEnableParam header1 = new KeyValueEnableParam();
        header1.setKey("a");
        header1.setValue("aa");

        KeyValueEnableParam header2 = new KeyValueEnableParam();
        header2.setKey("b");
        header2.setValue("bb");

        KeyValueEnableParam header3 = new KeyValueEnableParam();
        header3.setKey("Cookie");
        header3.setValue("a=b");

        HttpConfig httpNoneConfig = new HttpConfig();
        httpNoneConfig.setHostname("localhost:8081");
        httpNoneConfig.setType(HttpConfig.HttpConfigMatchType.NONE.name());
        httpNoneConfig.setHeaders(List.of(header1, header2, header3));

        HttpConfig httpModuleConfig = new HttpConfig();
        httpModuleConfig.setHostname("localhost:8081");
        httpModuleConfig.setType(HttpConfig.HttpConfigMatchType.MODULE.name());
        SelectModule selectModule = new SelectModule();
        selectModule.setModuleId(moduleId);
        selectModule.setContainChildModule(true);
        httpModuleConfig.getModuleMatchRule().setModules(List.of(selectModule));
        httpModuleConfig.setHeaders(List.of(header1, header2, header3));

        HttpConfig httpPathConfig = new HttpConfig();
        httpPathConfig.setHostname("localhost:8081");
        httpPathConfig.setType(HttpConfig.HttpConfigMatchType.PATH.name());
        httpPathConfig.getPathMatchRule().setPath("/test");
        httpPathConfig.getPathMatchRule().setCondition(HttpConfigPathMatchRule.MatchRuleCondition.CONTAINS.name());
        httpPathConfig.setHeaders(List.of(header1, header2, header3));

        environmentConfig.setHttpConfig(List.of(httpNoneConfig, httpModuleConfig, httpPathConfig));

        environmentConfig.setPluginConfigMap(pluginConfigMap);
        envRequest.setConfig(environmentConfig);
        return environmentService.add(envRequest, "admin", null);
    }

    public EnvironmentGroup initEnvGroup(Environment environment) {
        EnvironmentGroupRequest groupRequest = new EnvironmentGroupRequest();
        groupRequest.setProjectId(BaseTest.DEFAULT_PROJECT_ID);
        groupRequest.setName(UUID.randomUUID().toString());
        EnvironmentGroupProjectDTO environmentGroupProjectDTO = new EnvironmentGroupProjectDTO();
        environmentGroupProjectDTO.setEnvironmentId(environment.getId());
        environmentGroupProjectDTO.setProjectId(BaseTest.DEFAULT_PROJECT_ID);
        groupRequest.setEnvGroupProject(List.of(environmentGroupProjectDTO));
        return environmentGroupService.add(groupRequest, "admin");
    }

    private DataSource getDataSource() {
        DataSource dataSource = new DataSource();
        dataSource.setDataSource("test");
        dataSource.setId("dataSourceId");
        dataSource.setDriver("com.mysql.cj.jdbc.Driver");
        dataSource.setUsername("root");
        dataSource.setPassword("root");
        dataSource.setDbUrl("jdbc:mysql://192.168.15.41:3306/metersphere");
        return dataSource;
    }
}
