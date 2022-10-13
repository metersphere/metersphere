package io.metersphere.api.dto.definition.request.processors.post;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.api.dto.definition.request.ElementUtil;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.scenario.DatabaseConfig;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.service.definition.ApiDefinitionService;
import io.metersphere.service.definition.ApiTestCaseService;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.commons.constants.ElementConstants;
import io.metersphere.commons.constants.MsTestElementConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.environment.service.BaseEnvironmentService;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.commons.utils.JSONUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.jdbc.config.DataSourceElement;
import org.apache.jmeter.protocol.jdbc.processor.JDBCPostProcessor;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author song.tianyang
 * @Date 2021/7/13 11:08 上午
 */
@Data
@EqualsAndHashCode(callSuper = true)

public class MsJDBCPostProcessor extends MsTestElement {
    // type 必须放最前面，以便能够转换正确的类
    private String type = ElementConstants.JDBC_POST;
    private String clazzName = MsJDBCPostProcessor.class.getCanonicalName();


    private DatabaseConfig dataSource;

    private String query;

    private long queryTimeout;

    private String resultVariable;

    private String variableNames;

    private List<KeyValue> variables;

    private String environmentId;

    private String dataSourceId;

    private String protocol = "SQL";


    private String useEnvironment;

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, MsParameter msParameter) {
        ParameterConfig config = (ParameterConfig) msParameter;
        // 历史数据清理
        this.dataSource = null;
        // 非导出操作，且不是启用状态则跳过执行
        if (!config.isOperating() && !this.isEnable()) {
            return;
        }
        if (this.getReferenced() != null && MsTestElementConstants.REF.name().equals(this.getReferenced())) {
            this.setRefElement();
        }
        if (config.getConfig() == null) {
            // 单独接口执行
            this.setProjectId(config.getProjectId());
            config.setConfig(ElementUtil.getEnvironmentConfig(StringUtils.isNotEmpty(useEnvironment) ? useEnvironment : environmentId, this.getProjectId()));
        }

        // 数据兼容处理
        if (config.getConfig() != null && StringUtils.isNotEmpty(this.getProjectId()) && config.getConfig().containsKey(this.getProjectId())) {
            // 1.8 之后 当前正常数据
        } else if (config.getConfig() != null && config.getConfig().containsKey(getParentProjectId())) {
            // 1.8 前后 混合数据
            this.setProjectId(getParentProjectId());
        } else {
            // 1.8 之前 数据
            if (config.getConfig() != null) {
                if (config.getConfig().containsKey(RunModeConstants.HIS_PRO_ID.toString())) {
                    this.setProjectId(RunModeConstants.HIS_PRO_ID.toString());
                } else {
                    // 测试计划执行
                    Iterator<String> it = config.getConfig().keySet().iterator();
                    if (it.hasNext()) {
                        this.setProjectId(it.next());
                    }
                }
            }
        }
        //如果当前数据源为null，则获取已选环境的数据源
        if (this.dataSource == null) {
            // 自选了数据源
            if (config.isEffective(this.getProjectId()) && CollectionUtils.isNotEmpty(config.getConfig().get(this.getProjectId()).getDatabaseConfigs())
                    && isDataSource(config.getConfig().get(this.getProjectId()).getDatabaseConfigs())) {
                EnvironmentConfig environmentConfig = config.getConfig().get(this.getProjectId());
                if (environmentConfig.getDatabaseConfigs() != null && StringUtils.isNotEmpty(environmentConfig.getEnvironmentId())) {
                    this.environmentId = environmentConfig.getEnvironmentId();
                }
                this.initDataSource();
            } else {
                // 取当前环境下默认的一个数据源
                if (config.isEffective(this.getProjectId()) && CollectionUtils.isNotEmpty(config.getConfig().get(this.getProjectId()).getDatabaseConfigs())) {
                    DatabaseConfig dataSourceOrg = ElementUtil.dataSource(getProjectId(), dataSourceId, config.getConfig().get(this.getProjectId()));
                    if (dataSourceOrg != null) {
                        this.dataSource = dataSourceOrg;
                    } else {
                        this.dataSource = config.getConfig().get(this.getProjectId()).getDatabaseConfigs().get(0);
                    }
                }
            }
        }
        if (this.dataSource == null) {
            // 用自身的数据
            if (StringUtils.isNotEmpty(dataSourceId)) {
                this.dataSource = null;
                this.initDataSource();
            }
            if (this.dataSource == null) {
                String message = "数据源为空请选择数据源";
                MSException.throwException(StringUtils.isNotEmpty(this.getName()) ? this.getName() + "：" + message : message);
            }
        }
        final HashTree samplerHashTree = tree.add(jdbcPostProcessor(config));
        tree.add(jdbcDataSource());
        Arguments arguments = arguments(StringUtils.isNotEmpty(this.getName()) ? this.getName() : "Arguments", this.getVariables());
        if (arguments != null) {
            tree.add(arguments);
        }
        if (CollectionUtils.isNotEmpty(hashTree)) {
            hashTree.forEach(el -> {
                el.toHashTree(samplerHashTree, el.getHashTree(), config);
            });
        }
    }

    private boolean isDataSource(List<DatabaseConfig> databaseConfigs) {
        List<String> ids = databaseConfigs.stream().map(DatabaseConfig::getId).collect(Collectors.toList());
        if (StringUtils.isNotEmpty(this.dataSourceId) && ids.contains(this.dataSourceId)) {
            return true;
        }
        return false;
    }

    private String getParentProjectId() {
        MsTestElement parent = this.getParent();
        while (parent != null) {
            if (StringUtils.isNotBlank(parent.getProjectId())) {
                return parent.getProjectId();
            }
            parent = parent.getParent();
        }
        return "";
    }

    private void setRefElement() {
        try {
            ApiDefinitionService apiDefinitionService = CommonBeanFactory.getBean(ApiDefinitionService.class);
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            MsJDBCPostProcessor proxy = null;
            if (StringUtils.equals(this.getRefType(), "CASE")) {
                ApiTestCaseService apiTestCaseService = CommonBeanFactory.getBean(ApiTestCaseService.class);
                ApiTestCaseWithBLOBs bloBs = apiTestCaseService.get(this.getId());
                if (bloBs != null) {
                    this.setName(bloBs.getName());
                    this.setProjectId(bloBs.getProjectId());
                    JSONObject element = JSONUtil.parseObject(bloBs.getRequest());
                    ElementUtil.dataFormatting(element);
                    proxy = mapper.readValue(element.toString(), new TypeReference<MsJDBCPostProcessor>() {
                    });
                }
            } else {
                ApiDefinitionWithBLOBs apiDefinition = apiDefinitionService.getBLOBs(this.getId());
                if (apiDefinition != null) {
                    this.setProjectId(apiDefinition.getProjectId());
                    proxy = mapper.readValue(apiDefinition.getRequest(), new TypeReference<MsJDBCPostProcessor>() {
                    });
                    this.setName(apiDefinition.getName());
                }
            }
            if (proxy != null) {
                this.setHashTree(proxy.getHashTree());
                this.setDataSource(proxy.getDataSource());
                this.setDataSourceId(proxy.getDataSourceId());
                this.setQuery(proxy.getQuery());
                this.setVariables(proxy.getVariables());
                this.setVariableNames(proxy.getVariableNames());
                this.setResultVariable(proxy.getResultVariable());
                this.setQueryTimeout(proxy.getQueryTimeout());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            LogUtil.error(ex);
        }
    }

    private void initDataSource() {
        BaseEnvironmentService apiTestEnvironmentService = CommonBeanFactory.getBean(BaseEnvironmentService.class);
        ApiTestEnvironmentWithBLOBs environment = apiTestEnvironmentService.get(environmentId);
        if (environment != null && environment.getConfig() != null) {
            EnvironmentConfig envConfig = JSONUtil.parseObject(environment.getConfig(), EnvironmentConfig.class);
            if (CollectionUtils.isNotEmpty(envConfig.getDatabaseConfigs())) {
                envConfig.getDatabaseConfigs().forEach(item -> {
                    if (item.getId().equals(this.dataSourceId)) {
                        this.dataSource = item;
                        return;
                    }
                });
            }
        }
    }

    private Arguments arguments(String name, List<KeyValue> variables) {
        if (CollectionUtils.isNotEmpty(variables)) {
            Arguments arguments = new Arguments();
            arguments.setEnabled(true);
            arguments.setName(name + "JDBC_Argument");
            arguments.setProperty(TestElement.TEST_CLASS, Arguments.class.getName());
            arguments.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("ArgumentsPanel"));
            variables.stream().filter(KeyValue::isValid).filter(KeyValue::isEnable).forEach(keyValue ->
                    arguments.addArgument(keyValue.getName(), ElementUtil.getEvlValue(keyValue.getValue()), "=")
            );
            return arguments;
        }
        return null;
    }

    private JDBCPostProcessor jdbcPostProcessor(ParameterConfig config) {
        JDBCPostProcessor jdbcPostProcessor = new JDBCPostProcessor();
        jdbcPostProcessor.setEnabled(this.isEnable());
        jdbcPostProcessor.setName(this.getName() == null ? ElementConstants.JDBC_POST : this.getName());
        jdbcPostProcessor.setProperty(TestElement.TEST_CLASS, JDBCPostProcessor.class.getName());
        jdbcPostProcessor.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestBeanGUI"));

        ElementUtil.setBaseParams(jdbcPostProcessor, this.getParent(), config, this.getId(), this.getIndex());
        // request.getDataSource() 是ID，需要转换为Name
        jdbcPostProcessor.setProperty("dataSource", this.dataSource.getName());
        jdbcPostProcessor.setProperty("query", this.getQuery());
        jdbcPostProcessor.setProperty("queryTimeout", String.valueOf(this.getQueryTimeout()));
        jdbcPostProcessor.setProperty("resultVariable", this.getResultVariable());
        jdbcPostProcessor.setProperty("variableNames", this.getVariableNames());
        jdbcPostProcessor.setProperty("resultSetHandler", "Store as String");
        jdbcPostProcessor.setProperty("queryType", "Callable Statement");
        return jdbcPostProcessor;
    }

    private DataSourceElement jdbcDataSource() {
        DataSourceElement dataSourceElement = new DataSourceElement();
        dataSourceElement.setEnabled(true);
        dataSourceElement.setName(this.getName() + " JDBCDataSource");
        dataSourceElement.setProperty(TestElement.TEST_CLASS, DataSourceElement.class.getName());
        dataSourceElement.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestBeanGUI"));
        dataSourceElement.setProperty("autocommit", true);
        dataSourceElement.setProperty("keepAlive", true);
        dataSourceElement.setProperty("preinit", false);
        dataSourceElement.setProperty("dataSource", dataSource.getName());
        dataSourceElement.setProperty("dbUrl", dataSource.getDbUrl());
        dataSourceElement.setProperty("driver", dataSource.getDriver());
        dataSourceElement.setProperty("username", dataSource.getUsername());
        dataSourceElement.setProperty("password", dataSource.getPassword());
        dataSourceElement.setProperty("poolMax", dataSource.getPoolMax());
        dataSourceElement.setProperty("timeout", String.valueOf(dataSource.getTimeout()));
        dataSourceElement.setProperty("connectionAge", 5000);
        dataSourceElement.setProperty("trimInterval", 6000);
        dataSourceElement.setProperty("transactionIsolation", "DEFAULT");
        return dataSourceElement;
    }
}
