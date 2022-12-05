package io.metersphere.api.dto.definition.request.processors.pre;


import io.metersphere.api.dto.definition.request.ElementUtil;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.scenario.DatabaseConfig;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.commons.constants.ElementConstants;
import io.metersphere.commons.constants.RequestTypeConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.vo.JDBCProcessorVO;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.protocol.jdbc.processor.JDBCPreProcessor;
import org.apache.jorphan.collections.HashTree;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author song.tianyang
 * @Date 2021/7/13 11:08 上午
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MsJDBCPreProcessor extends MsTestElement {
    // type 必须放最前面，以便能够转换正确的类
    private String type = ElementConstants.JDBC_PRE;
    private String clazzName = MsJDBCPreProcessor.class.getCanonicalName();
    private DatabaseConfig dataSource;
    private String query;
    private long queryTimeout;
    private String resultVariable;
    private String variableNames;
    private List<KeyValue> variables;
    private String environmentId;
    private String dataSourceId;
    private String protocol = RequestTypeConstants.SQL;
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
        if (config.getConfig() == null) {
            // 单独接口执行
            this.setProjectId(config.getProjectId());
            config.setConfig(ElementUtil.getEnvironmentConfig(StringUtils.isNotEmpty(useEnvironment) ? useEnvironment : environmentId, this.getProjectId()));
        }

        // 自选了数据源
        if (config.isEffective(this.getProjectId()) && CollectionUtils.isNotEmpty(config.getConfig().get(this.getProjectId()).getDatabaseConfigs())
                && isDataSource(config.getConfig().get(this.getProjectId()).getDatabaseConfigs())) {
            EnvironmentConfig environmentConfig = config.getConfig().get(this.getProjectId());
            if (environmentConfig.getDatabaseConfigs() != null && StringUtils.isNotEmpty(environmentConfig.getEnvironmentId())) {
                this.environmentId = environmentConfig.getEnvironmentId();
            }
            this.dataSource = ElementUtil.initDataSource(this.environmentId, this.dataSourceId);
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
        if (this.dataSource == null) {
            // 用自身的数据
            if (StringUtils.isNotEmpty(dataSourceId)) {
                this.dataSource = ElementUtil.initDataSource(this.environmentId, this.dataSourceId);
            }
            if (this.dataSource == null) {
                String message = "数据源为空请选择数据源";
                MSException.throwException(StringUtils.isNotEmpty(this.getName()) ? this.getName() + "：" + message : message);
            }
        }

        JDBCPreProcessor jdbcPreProcessor = jdbcPreProcessor(config);
        final HashTree samplerHashTree = tree.add(jdbcPreProcessor);
        // 数据源
        tree.add(ElementUtil.jdbcDataSource(jdbcPreProcessor.getDataSource(), this.dataSource));
        // 参数
        ElementUtil.jdbcArguments(this.getName(), this.getVariables(), tree);
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

    private JDBCPreProcessor jdbcPreProcessor(ParameterConfig config) {
        JDBCPreProcessor jdbcPreProcessor = new JDBCPreProcessor();
        JDBCProcessorVO vo = new JDBCProcessorVO();
        BeanUtils.copyBean(vo, this);
        ElementUtil.jdbcProcessor(jdbcPreProcessor, config, vo);
        return jdbcPreProcessor;
    }
}
