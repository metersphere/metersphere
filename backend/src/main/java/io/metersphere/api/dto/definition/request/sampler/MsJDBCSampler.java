package io.metersphere.api.dto.definition.request.sampler;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import io.metersphere.api.dto.definition.request.MsTestElement;
import io.metersphere.api.dto.scenario.DatabaseConfig;
import io.metersphere.api.dto.scenario.KeyValue;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.jdbc.config.DataSourceElement;
import org.apache.jmeter.protocol.jdbc.sampler.JDBCSampler;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@JSONType(typeName = "JDBCSampler")
public class MsJDBCSampler extends MsTestElement {
    // type 必须放最前面，以便能够转换正确的类
    private String type = "JDBCSampler";
    @JSONField(ordinal = 10)
    private DatabaseConfig dataSource;
    @JSONField(ordinal = 11)
    private String query;
    @JSONField(ordinal = 12)
    private long queryTimeout;
    @JSONField(ordinal = 13)
    private String resultVariable;
    @JSONField(ordinal = 14)
    private String variableNames;
    @JSONField(ordinal = 15)
    private List<KeyValue> variables;
    @JSONField(ordinal = 16)
    private String environmentId;

    public void toHashTree(HashTree tree, List<MsTestElement> hashTree) {
        final HashTree samplerHashTree = new ListedHashTree();
        samplerHashTree.add(jdbcDataSource());
        samplerHashTree.add(arguments(this.getName() + " Variables", this.getVariables()));
        tree.set(jdbcSampler(), samplerHashTree);
        if (CollectionUtils.isNotEmpty(hashTree)) {
            hashTree.forEach(el -> {
                el.toHashTree(samplerHashTree, el.getHashTree());
            });
        }
    }

    private Arguments arguments(String name, List<KeyValue> variables) {
        Arguments arguments = new Arguments();
        arguments.setEnabled(true);
        arguments.setName(name);
        arguments.setProperty(TestElement.TEST_CLASS, Arguments.class.getName());
        arguments.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("ArgumentsPanel"));
        variables.stream().filter(KeyValue::isValid).filter(KeyValue::isEnable).forEach(keyValue ->
                arguments.addArgument(keyValue.getName(), keyValue.getValue(), "=")
        );
        return arguments;
    }

    private JDBCSampler jdbcSampler() {
        JDBCSampler sampler = new JDBCSampler();
        sampler.setName(this.getName());
        sampler.setProperty(TestElement.TEST_CLASS, JDBCSampler.class.getName());
        sampler.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestBeanGUI"));
        // request.getDataSource() 是ID，需要转换为Name
        sampler.setDataSource(this.dataSource.getName());
        sampler.setQuery(this.getQuery());
        sampler.setQueryTimeout(String.valueOf(this.getQueryTimeout()));
        sampler.setResultVariable(this.getResultVariable());
        sampler.setVariableNames(this.getVariableNames());
        sampler.setResultSetHandler("Store as String");
        sampler.setQueryType("Callable Statement");
        return sampler;
    }

    private DataSourceElement jdbcDataSource() {
        DataSourceElement dataSourceElement = new DataSourceElement();
        dataSourceElement.setEnabled(true);
        dataSourceElement.setName(this.getName() + " JDBCDataSource");
        dataSourceElement.setProperty(TestElement.TEST_CLASS, DataSourceElement.class.getName());
        dataSourceElement.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestBeanGUI"));
        dataSourceElement.setAutocommit(true);
        dataSourceElement.setKeepAlive(true);
        dataSourceElement.setPreinit(true);
        dataSourceElement.setDataSource(dataSource.getName());
        dataSourceElement.setDbUrl(dataSource.getDbUrl());
        dataSourceElement.setDriver(dataSource.getDriver());
        dataSourceElement.setUsername(dataSource.getUsername());
        dataSourceElement.setPassword(dataSource.getPassword());
        dataSourceElement.setPoolMax(String.valueOf(dataSource.getPoolMax()));
        dataSourceElement.setTimeout(String.valueOf(dataSource.getTimeout()));
        dataSourceElement.setConnectionAge("5000");
        dataSourceElement.setTrimInterval("60000");
        dataSourceElement.setTransactionIsolation("DEFAULT");
        return dataSourceElement;
    }
}
