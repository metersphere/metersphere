package io.metersphere.api.dto.definition.request.sampler;

import io.metersphere.api.dto.definition.request.ElementUtil;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.definition.request.processors.post.MsJDBCPostProcessor;
import io.metersphere.api.dto.definition.request.processors.pre.MsJDBCPreProcessor;
import io.metersphere.api.dto.scenario.DatabaseConfig;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.dto.scenario.environment.GlobalScriptFilterRequest;
import io.metersphere.api.parse.api.JMeterScriptUtil;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.commons.constants.CommonConstants;
import io.metersphere.commons.constants.ElementConstants;
import io.metersphere.commons.constants.MsTestElementConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.commons.vo.JDBCProcessorVO;
import io.metersphere.environment.service.BaseEnvironmentService;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.service.definition.ApiDefinitionService;
import io.metersphere.service.definition.ApiTestCaseService;
import io.metersphere.utils.LoggerUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.jdbc.sampler.JDBCSampler;
import org.apache.jorphan.collections.HashTree;
import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
public class MsJDBCSampler extends MsTestElement {
    // type 必须放最前面，以便能够转换正确的类
    private String type = ElementConstants.JDBC_SAMPLER;
    private String clazzName = MsJDBCSampler.class.getCanonicalName();
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
    private boolean customizeReq;
    private Boolean isRefEnvironment;

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, MsParameter msParameter) {
        ParameterConfig config = (ParameterConfig) msParameter;
        // 清理掉历史遗留数据
        this.dataSource = null;
        // 非导出操作，且不是启用状态则跳过执行
        if (config != null && !config.isOperating() && !this.isEnable()) {
            return;
        } else if (config.isOperating() && StringUtils.isNotEmpty(config.getOperatingSampleTestName())) {
            this.setName(config.getOperatingSampleTestName());
        }
        
        if (this.getReferenced() != null && MsTestElementConstants.REF.name().equals(this.getReferenced())) {
            boolean ref = this.setRefElement();
            if (!ref) {
                LoggerUtil.error("引用对象已经被删除：" + this.getId());
                return;
            }
            hashTree = this.getHashTree();
        }
        if (config != null && config.getConfig() == null) {
            // 单独接口执行
            this.setProjectId(config.getProjectId());
            config.setConfig(ElementUtil.getEnvironmentConfig(StringUtils.isNotEmpty(useEnvironment) ? useEnvironment : environmentId, this.getProjectId()));
        }

        EnvironmentConfig envConfig = null;
        // 自定义请求非引用环境取自身环境
        if (StringUtils.equalsIgnoreCase(this.getReferenced(), ElementConstants.STEP_CREATED) && (isRefEnvironment == null || !isRefEnvironment)) {
            this.dataSource = null;
            envConfig = this.initDataSource();
        } else {
            if (config.isEffective(this.getProjectId()) && CollectionUtils.isNotEmpty(config.getConfig().get(this.getProjectId()).getDatabaseConfigs())
                    && isDataSource(config.getConfig().get(this.getProjectId()).getDatabaseConfigs())) {
                EnvironmentConfig environmentConfig = config.getConfig().get(this.getProjectId());
                if (environmentConfig.getDatabaseConfigs() != null && StringUtils.isNotEmpty(environmentConfig.getEnvironmentId())) {
                    this.environmentId = environmentConfig.getEnvironmentId();
                }
                this.dataSource = null;
                envConfig = this.initDataSource();
                if (dataSource == null && CollectionUtils.isNotEmpty(environmentConfig.getDatabaseConfigs())) {
                    dataSource = environmentConfig.getDatabaseConfigs().get(0);
                }
            } else {
                // 取当前环境下默认的一个数据源
                if (config.isEffective(this.getProjectId())) {
                    if (config.getConfig().get(this.getProjectId()) != null) {
                        envConfig = config.getConfig().get(this.getProjectId());
                        if (CollectionUtils.isNotEmpty(envConfig.getDatabaseConfigs())) {
                            LoggerUtil.info(this.getName() + "：开始获取当前环境下默认数据源");
                            DatabaseConfig dataSourceOrg = ElementUtil.dataSource(getProjectId(), dataSourceId, envConfig);
                            if (dataSourceOrg != null) {
                                this.dataSource = dataSourceOrg;
                            } else {
                                LoggerUtil.info(this.getName() + "：获取当前环境下默认数据源结束！未查找到默认数据源");
                                this.dataSource = envConfig.getDatabaseConfigs().get(0);
                            }
                        }
                    }
                }
            }
        }
        if (this.dataSource == null) {
            LoggerUtil.info(this.getName() + "  当前项目id", this.getProjectId() + "  当前环境配置信息", JSONUtil.toJSONString(config));
            String message = "数据源为空请选择数据源";
            MSException.throwException(StringUtils.isNotEmpty(this.getName()) ? this.getName() + "：" + message : message);
        }
        JDBCSampler jdbcSampler = jdbcSampler(config);
        // 失败重试
        HashTree samplerHashTree;
        if (config.getRetryNum() > 0 && !ElementUtil.isLoop(this.getParent())) {
            final HashTree loopTree = ElementUtil.retryHashTree(this.getName(), config.getRetryNum(), tree);
            samplerHashTree = loopTree.add(jdbcSampler);
        } else {
            samplerHashTree = tree.add(jdbcSampler);
        }
        tree.add(ElementUtil.jdbcDataSource(jdbcSampler.getDataSource(), this.dataSource));
        ElementUtil.jdbcArguments(this.getName(), this.getVariables(), tree);

        // 环境通用请求头
        Arguments envArguments = ElementUtil.getConfigArguments(config, this.getName(), this.getProjectId(), null);
        if (envArguments != null) {
            tree.add(envArguments);
        }
        //添加csv
        ElementUtil.addApiVariables(config, tree, this.getProjectId());
        //增加误报、全局断言
        HashTreeUtil.addPositive(envConfig, samplerHashTree, config, this.getProjectId(), jdbcSampler);
        //处理全局前后置脚本(步骤内)
        String environmentId = this.getEnvironmentId();
        if (environmentId == null) {
            environmentId = this.useEnvironment;
        }
        //根据配置将脚本放置在私有脚本之前
        JMeterScriptUtil.setScriptByEnvironmentConfig(envConfig, samplerHashTree, GlobalScriptFilterRequest.JDBC.name(), environmentId, config, false);
        if (CollectionUtils.isNotEmpty(hashTree)) {
            hashTree = ElementUtil.order(hashTree);
            hashTree.forEach(el -> {
                if (el instanceof MsJDBCPreProcessor || el instanceof MsJDBCPostProcessor) {
                    el.setParent(this);
                }
                el.toHashTree(samplerHashTree, el.getHashTree(), config);
            });
        }
        //根据配置将脚本放置在私有脚本之后
        JMeterScriptUtil.setScriptByEnvironmentConfig(envConfig, samplerHashTree, GlobalScriptFilterRequest.JDBC.name(), environmentId, config, true);
    }

    private boolean isDataSource(List<DatabaseConfig> databaseConfigs) {
        List<String> ids = databaseConfigs.stream().map(DatabaseConfig::getId).collect(Collectors.toList());
        if (StringUtils.isNotEmpty(this.dataSourceId) && ids.contains(this.dataSourceId)) {
            return true;
        }
        return false;
    }

    private boolean setRefElement() {
        try {
            MsJDBCSampler proxy = null;
            if (StringUtils.equals(this.getRefType(), CommonConstants.CASE)) {
                ApiTestCaseService apiTestCaseService = CommonBeanFactory.getBean(ApiTestCaseService.class);
                ApiTestCaseWithBLOBs bloBs = apiTestCaseService.get(this.getId());
                if (bloBs != null) {
                    this.setName(bloBs.getName());
                    this.setProjectId(bloBs.getProjectId());
                    JSONObject element = JSONUtil.parseObject(bloBs.getRequest());
                    ElementUtil.dataFormatting(element);
                    proxy = JSONUtil.parseObject(element.toString(), MsJDBCSampler.class);
                }
            } else {
                ApiDefinitionService apiDefinitionService = CommonBeanFactory.getBean(ApiDefinitionService.class);
                ApiDefinitionWithBLOBs apiDefinition = apiDefinitionService.getBLOBs(this.getId());
                if (apiDefinition != null) {
                    this.setProjectId(apiDefinition.getProjectId());
                    proxy = JSONUtil.parseObject(apiDefinition.getRequest(), MsJDBCSampler.class);
                    this.setName(apiDefinition.getName());
                }
            }
            if (proxy != null) {
                if (StringUtils.equals(this.getRefType(), CommonConstants.CASE)) {
                    ElementUtil.mergeHashTree(this, proxy.getHashTree());
                } else {
                    this.setHashTree(proxy.getHashTree());
                }
                this.setDataSource(proxy.getDataSource());
                this.setDataSourceId(proxy.getDataSourceId());
                this.setQuery(proxy.getQuery());
                this.setVariables(proxy.getVariables());
                this.setVariableNames(proxy.getVariableNames());
                this.setResultVariable(proxy.getResultVariable());
                this.setQueryTimeout(proxy.getQueryTimeout());
                return true;
            }
        } catch (Exception ex) {
            LogUtil.error(ex);
        }
        return false;
    }

    private EnvironmentConfig initDataSource() {
        BaseEnvironmentService apiTestEnvironmentService = CommonBeanFactory.getBean(BaseEnvironmentService.class);
        ApiTestEnvironmentWithBLOBs environment = apiTestEnvironmentService.get(environmentId);
        EnvironmentConfig envConfig = null;
        if (environment != null && environment.getConfig() != null) {
            envConfig = JSONUtil.parseObject(environment.getConfig(), EnvironmentConfig.class);
            if (CollectionUtils.isNotEmpty(envConfig.getDatabaseConfigs())) {
                envConfig.getDatabaseConfigs().forEach(item -> {
                    if (item.getId().equals(this.dataSourceId)) {
                        this.dataSource = item;
                    }
                });
            }
        }
        return envConfig;
    }


    private JDBCSampler jdbcSampler(ParameterConfig config) {
        JDBCSampler sampler = new JDBCSampler();
        JDBCProcessorVO vo = new JDBCProcessorVO();
        BeanUtils.copyBean(vo, this);
        ElementUtil.jdbcProcessor(sampler, config, vo);
        sampler.setEnabled(this.isEnable());
        sampler.setName(this.getName());
        if (StringUtils.isEmpty(this.getName())) {
            sampler.setName(ElementConstants.JDBC_SAMPLER);
        }
        if (config.isOperating()) {
            String[] testNameArr = sampler.getName().split("<->");
            if (testNameArr.length > 0) {
                String testName = testNameArr[0];
                sampler.setName(testName);
            }
        }
        return sampler;
    }
}
