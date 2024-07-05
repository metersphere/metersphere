package io.metersphere.api.parser.jmeter.processor;

import io.metersphere.api.dto.ApiParamConfig;
import io.metersphere.api.parser.jmeter.constants.JmeterAlias;
import io.metersphere.api.parser.jmeter.constants.JmeterProperty;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.project.api.KeyValueParam;
import io.metersphere.project.api.processor.SQLProcessor;
import io.metersphere.project.constants.ScriptLanguageType;
import io.metersphere.project.dto.environment.EnvironmentInfoDTO;
import io.metersphere.project.dto.environment.datasource.DataSource;
import io.metersphere.sdk.util.LogUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.extractor.JSR223PostProcessor;
import org.apache.jmeter.modifiers.JSR223PreProcessor;
import org.apache.jmeter.protocol.jdbc.config.DataSourceElement;
import org.apache.jmeter.protocol.jdbc.processor.JDBCPreProcessor;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.util.List;

import static io.metersphere.api.parser.jmeter.constants.JmeterAlias.TEST_BEAN_GUI;


/**
 * @Author: jianxing
 * @CreateTime: 2023-12-26  14:49
 */
public abstract class SqlProcessorConverter extends MsProcessorConverter<SQLProcessor> {

    public <T extends TestElement> void parse(HashTree hashTree,
                                                        SQLProcessor sqlProcessor,
                                                        ParameterConfig config,
                                                        Class<T> jdbcProcessorClass) {
        if (!needParse(sqlProcessor, config)) {
            return;
        }

        ApiParamConfig apiParamConfig = (ApiParamConfig) config;
        EnvironmentInfoDTO envConfig = apiParamConfig.getEnvConfig(sqlProcessor.getProjectId());
        DataSource dataSource = getDataSource(sqlProcessor, envConfig);
        if (dataSource == null) {
            return;
        }
        // 添加数据源
        DataSourceElement dataSourceElement = getDataSourceElement(dataSource);
        hashTree.add(dataSourceElement);

        try {
            // 添加前后置处理器
            TestElement jdbcProcessor = jdbcProcessorClass.getDeclaredConstructor().newInstance();
            jdbcProcessor = getJdbcProcessor(sqlProcessor, jdbcProcessor, dataSource);
            hashTree.add(jdbcProcessor);
            List<KeyValueParam> extractParams = sqlProcessor.getExtractParams()
                    .stream()
                    .filter(KeyValueParam::isValid)
                    .toList();
            // 添加提取的变量
            TestElement jdbcPostProcessor;
            if (jdbcProcessor instanceof JDBCPreProcessor) {
                jdbcPostProcessor = getExtractParamProcessor(sqlProcessor.getName(), extractParams, JSR223PreProcessor.class);
            } else {
                jdbcPostProcessor = getExtractParamProcessor(sqlProcessor.getName(), extractParams, JSR223PostProcessor.class);
            }
            if (jdbcPostProcessor != null) {
                hashTree.add(jdbcPostProcessor);
            }

        } catch (Exception e) {
            LogUtils.error(e);
        }
    }

    public <T extends TestElement> T getExtractParamProcessor(String name, List<KeyValueParam> extractParams, Class<T> elementType) {
        if (CollectionUtils.isNotEmpty(extractParams)) {
            T processor;
            try {
                processor = elementType.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to create processor: " + elementType.getSimpleName(), e);
            }

            processor.setName(name);
            processor.setProperty(TestElement.TEST_CLASS, elementType.getName());
            processor.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass(JmeterAlias.TEST_BEAN_GUI));
            processor.setProperty(JmeterProperty.SCRIPT_LANGUAGE, ScriptLanguageType.BEANSHELL.name().toLowerCase());

            StringBuilder scriptBuilder = new StringBuilder();
            extractParams.forEach(keyValue -> {
                        String script = """
                                        vars.put("%s","${%s}");
                                        """;
                        scriptBuilder.append(String.format(script, keyValue.getKey(), keyValue.getValue()));
                    });
            processor.setProperty(JmeterProperty.SCRIPT, scriptBuilder.toString());
            return processor;
        }

        return null;
    }

    protected DataSource getDataSource(SQLProcessor sqlProcessor, EnvironmentInfoDTO envConfig) {
        if (envConfig == null) {
            return null;
        }
        List<DataSource> dataSources = envConfig.getConfig().getDataSources();

        // 先按ID匹配
        List<DataSource> dataSourceResults = dataSources.stream()
                .filter(item -> StringUtils.equals(item.getId(), sqlProcessor.getDataSourceId()))
                .toList();

        // 再按名称匹配
        if (CollectionUtils.isEmpty(dataSourceResults)) {
            dataSourceResults = dataSources.stream()
                    .filter(item -> StringUtils.equals(item.getDataSource(), sqlProcessor.getDataSourceName()))
                    .toList();
        }

        return CollectionUtils.isEmpty(dataSourceResults) ? null : dataSourceResults.getFirst();
    }

    protected TestElement getJdbcProcessor(SQLProcessor sqlProcessor, TestElement jdbcProcessor, DataSource dataSource) {
        jdbcProcessor.setEnabled(sqlProcessor.getEnable());
        jdbcProcessor.setName(sqlProcessor.getName() == null ? jdbcProcessor.getClass().getSimpleName() : sqlProcessor.getName());
        jdbcProcessor.setProperty(TestElement.TEST_CLASS, jdbcProcessor.getClass().getSimpleName());
        jdbcProcessor.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass(TEST_BEAN_GUI));
        jdbcProcessor.setProperty("dataSource", dataSource.getDataSource());
        jdbcProcessor.setProperty("query", sqlProcessor.getScript());
        jdbcProcessor.setProperty("queryTimeout", String.valueOf(sqlProcessor.getQueryTimeout()));
        jdbcProcessor.setProperty("resultVariable", sqlProcessor.getResultVariable());
        jdbcProcessor.setProperty("variableNames", sqlProcessor.getVariableNames());
        jdbcProcessor.setProperty("resultSetHandler", "Store as String");
        jdbcProcessor.setProperty("queryType", "Callable Statement");
        return jdbcProcessor;
    }

    public DataSourceElement getDataSourceElement(DataSource dataSource) {
        DataSourceElement dataSourceElement = new DataSourceElement();
        dataSourceElement.setEnabled(true);
        dataSourceElement.setName(dataSource.getDataSource() + "_JDBCDataSource");
        dataSourceElement.setProperty(TestElement.TEST_CLASS, DataSourceElement.class.getName());
        dataSourceElement.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass(TEST_BEAN_GUI));
        dataSourceElement.setProperty("autocommit", true);
        dataSourceElement.setProperty("keepAlive", true);
        dataSourceElement.setProperty("preinit", false);
        dataSourceElement.setProperty("dataSource", dataSource.getDataSource());
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
