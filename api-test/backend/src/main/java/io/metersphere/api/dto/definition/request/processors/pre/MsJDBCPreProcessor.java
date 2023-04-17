package io.metersphere.api.dto.definition.request.processors.pre;


import io.metersphere.api.dto.definition.request.ElementUtil;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.scenario.DatabaseConfig;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.commons.constants.ElementConstants;
import io.metersphere.commons.constants.RequestTypeConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.JSONUtil;
import io.metersphere.commons.vo.JDBCProcessorVO;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.utils.LoggerUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.protocol.jdbc.processor.JDBCPreProcessor;
import org.apache.jorphan.collections.HashTree;

import java.util.List;

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
        //检查projectId 旧数据
        if (StringUtils.isBlank(this.getProjectId()) && this.getParent() != null) {
            this.setProjectId(this.getParent().getProjectId());
        }
        if (config.getConfig() == null) {
            // 单独接口执行
            this.setProjectId(config.getProjectId());
            config.setConfig(ElementUtil.getEnvironmentConfig(StringUtils.isNotEmpty(useEnvironment) ? useEnvironment : environmentId, this.getProjectId()));
        }
        this.dataSource = ElementUtil.selectDataSourceFromJDBCProcessor(this.getName(), this.environmentId, this.dataSourceId, this.getProjectId(), config);
        if (this.dataSource == null) {
            // 用自身的数据
            if (StringUtils.isNotEmpty(dataSourceId)) {
                this.dataSource = ElementUtil.initDataSource(this.environmentId, this.dataSourceId);
            }
            if (this.dataSource == null) {
                LoggerUtil.info(this.getName() + "  当前项目id", this.getProjectId() + "  当前环境配置信息", JSONUtil.toJSONString(config));
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

    private JDBCPreProcessor jdbcPreProcessor(ParameterConfig config) {
        JDBCPreProcessor jdbcPreProcessor = new JDBCPreProcessor();
        JDBCProcessorVO vo = new JDBCProcessorVO();
        BeanUtils.copyBean(vo, this);
        ElementUtil.jdbcProcessor(jdbcPreProcessor, config, vo);
        return jdbcPreProcessor;
    }
}
