package io.metersphere.api.dto.definition.request.sampler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.api.dto.definition.request.MsTestElement;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.scenario.DatabaseConfig;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.service.ApiDefinitionService;
import io.metersphere.api.service.ApiTestCaseService;
import io.metersphere.api.service.ApiTestEnvironmentService;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.commons.constants.DelimiterConstants;
import io.metersphere.commons.constants.MsTestElementConstants;
import io.metersphere.commons.constants.RunModeConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.jdbc.config.DataSourceElement;
import org.apache.jmeter.protocol.jdbc.sampler.JDBCSampler;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
@JSONType(typeName = "JDBCSampler")
public class MsJDBCSampler extends MsTestElement {
    // type 必须放最前面，以便能够转换正确的类
    private String type = "JDBCSampler";
    @JSONField(ordinal = 20)
    private DatabaseConfig dataSource;
    @JSONField(ordinal = 21)
    private String query;
    @JSONField(ordinal = 22)
    private long queryTimeout;
    @JSONField(ordinal = 23)
    private String resultVariable;
    @JSONField(ordinal = 24)
    private String variableNames;
    @JSONField(ordinal = 25)
    private List<KeyValue> variables;
    @JSONField(ordinal = 26)
    private String environmentId;
    @JSONField(ordinal = 28)
    private String dataSourceId;
    @JSONField(ordinal = 29)
    private String protocol = "SQL";

    @JSONField(ordinal = 30)
    private String useEnvironment;

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, ParameterConfig config) {
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
            config.setConfig(getEnvironmentConfig(StringUtils.isNotEmpty(useEnvironment) ? useEnvironment : environmentId));
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
        // 自选了数据源
        if (config.isEffective(this.getProjectId()) && CollectionUtils.isNotEmpty(config.getConfig().get(this.getProjectId()).getDatabaseConfigs())
                && isDataSource(config.getConfig().get(this.getProjectId()).getDatabaseConfigs())) {
            this.dataSource = null;
            this.initDataSource();
        } else {
            this.dataSource = null;
            // 取当前环境下默认的一个数据源
            if (config.isEffective(this.getProjectId()) && CollectionUtils.isNotEmpty(config.getConfig().get(this.getProjectId()).getDatabaseConfigs())) {
                this.dataSource = config.getConfig().get(this.getProjectId()).getDatabaseConfigs().get(0);
            }
        }

        if (this.dataSource == null) {
            // 用自身的数据
            if (StringUtils.isNotEmpty(dataSourceId)) {
                this.dataSource = null;
                this.initDataSource();
            }
            if (this.dataSource == null) {
                MSException.throwException("数据源为空无法执行");
            }
        }
        final HashTree samplerHashTree = tree.add(jdbcSampler(config));
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
            MsJDBCSampler proxy = null;
            if (StringUtils.equals(this.getRefType(), "CASE")) {
                ApiTestCaseService apiTestCaseService = CommonBeanFactory.getBean(ApiTestCaseService.class);
                ApiTestCaseWithBLOBs bloBs = apiTestCaseService.get(this.getId());
                if (bloBs != null) {
                    this.setName(bloBs.getName());
                    this.setProjectId(bloBs.getProjectId());
                    proxy = mapper.readValue(bloBs.getRequest(), new TypeReference<MsJDBCSampler>() {
                    });
                }
            } else {
                ApiDefinitionWithBLOBs apiDefinition = apiDefinitionService.getBLOBs(this.getId());
                if (apiDefinition != null) {
                    this.setProjectId(apiDefinition.getProjectId());
                    proxy = mapper.readValue(apiDefinition.getRequest(), new TypeReference<MsJDBCSampler>() {
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
            LogUtil.error(ex.getMessage());
        }
    }

    private void initDataSource() {
        ApiTestEnvironmentService environmentService = CommonBeanFactory.getBean(ApiTestEnvironmentService.class);
        ApiTestEnvironmentWithBLOBs environment = environmentService.get(environmentId);
        if (environment != null && environment.getConfig() != null) {
            EnvironmentConfig envConfig = JSONObject.parseObject(environment.getConfig(), EnvironmentConfig.class);
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
                    arguments.addArgument(keyValue.getName(), keyValue.getValue(), "=")
            );
            return arguments;
        }
        return null;
    }

    private JDBCSampler jdbcSampler(ParameterConfig config) {
        JDBCSampler sampler = new JDBCSampler();
        sampler.setEnabled(this.isEnable());
        sampler.setName(this.getName());
        String name = this.getParentName(this.getParent());
        if (StringUtils.isNotEmpty(name) && !config.isOperating()) {
            sampler.setName(this.getName() + DelimiterConstants.SEPARATOR.toString() + name);
        }
        sampler.setProperty(TestElement.TEST_CLASS, JDBCSampler.class.getName());
        sampler.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestBeanGUI"));
        sampler.setProperty("MS-ID", this.getId());
        sampler.setProperty("MS-RESOURCE-ID", this.getResourceId());
        List<String> id_names = new LinkedList<>();
        this.getScenarioSet(this, id_names);
        sampler.setProperty("MS-SCENARIO", JSON.toJSONString(id_names));

        // request.getDataSource() 是ID，需要转换为Name
        sampler.setProperty("dataSource", this.dataSource.getName());
        sampler.setProperty("query", this.getQuery());
        sampler.setProperty("queryTimeout", String.valueOf(this.getQueryTimeout()));
        sampler.setProperty("resultVariable", this.getResultVariable());
        sampler.setProperty("variableNames", this.getVariableNames());
        sampler.setProperty("resultSetHandler", "Store as String");
        sampler.setProperty("queryType", "Callable Statement");
        return sampler;
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
