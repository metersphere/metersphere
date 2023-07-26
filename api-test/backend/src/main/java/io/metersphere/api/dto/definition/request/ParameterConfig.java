package io.metersphere.api.dto.definition.request;

import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.definition.request.variable.ScenarioVariable;
import io.metersphere.api.dto.scenario.HttpConfig;
import io.metersphere.api.dto.scenario.HttpConfigCondition;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.base.domain.ApiDefinition;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import io.metersphere.base.domain.TestPlanApiCase;
import io.metersphere.commons.constants.CommonConstants;
import io.metersphere.commons.constants.ConditionType;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.environment.ssl.MsKeyStore;
import io.metersphere.jmeter.utils.ScriptEngineUtils;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.service.definition.ApiDefinitionService;
import io.metersphere.service.definition.ApiTestCaseService;
import io.metersphere.service.plan.TestPlanApiCaseService;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class ParameterConfig extends MsParameter {

    public ParameterConfig(String currentProjectId, boolean isApi) {
        this.currentProjectId = currentProjectId;
        this.setApi(isApi);
        if (MapUtils.isEmpty(this.config)) {
            this.config = new HashMap<>();
        }
    }

    /**
     * 环境配置
     */
    private Map<String, EnvironmentConfig> config;
    /**
     * 缓存同一批请求的认证信息
     */
    private Map<String, MsKeyStore> keyStoreMap = new HashMap<>();
    /**
     * 公共场景参数
     */
    private List<ScenarioVariable> variables;
    /**
     * 当前场景变量，逐层传递
     */
    private List<ScenarioVariable> transferVariables;

    /**
     * 公共场景参数
     */
    private List<KeyValue> headers;

    /**
     * 公共Cookie
     */
    private boolean enableCookieShare;

    /**
     * 是否是导入/导出操作
     */
    private boolean isOperating;
    /**
     * 导入/导出操作时取样器的testName值
     */
    private String operatingSampleTestName;

    private String scenarioId;
    /**
     * 当前项目id
     */
    private String currentProjectId;
    /**
     * 报告 ID
     */
    private String reportId;

    private String reportType;

    private boolean runLocal;

    private boolean isApi;
    /**
     * 失败重试次数
     */
    private long retryNum;
    /**
     * 排除生成临界控制器的场景
     */
    private List<String> excludeScenarioIds = new ArrayList<>();

    private List<String> csvFilePaths = new ArrayList<>();

    public boolean isEffective(String projectId) {
        if ((StringUtils.isNotBlank(projectId) && this.config != null && this.config.get(projectId) != null)
                || (StringUtils.isNotBlank(this.currentProjectId) && this.config != null && this.config.get(currentProjectId) != null)) {
            return true;
        }
        return false;
    }

    static public Arguments valueSupposeMock(Arguments arguments) {
        for (int i = 0; i < arguments.getArguments().size(); ++i) {
            String argValue = arguments.getArgument(i).getValue();
            arguments.getArgument(i).setValue(ScriptEngineUtils.buildFunctionCallString(argValue));
        }
        return arguments;
    }


    public HttpConfig matchConfig(MsHTTPSamplerProxy samplerProxy, HttpConfig httpConfig) {
        boolean isNext = true;
        if (CollectionUtils.isNotEmpty(httpConfig.getConditions())) {
            for (HttpConfigCondition item : httpConfig.getConditions()) {
                if (item.getType().equals(ConditionType.PATH.name())) {
                    HttpConfig config = httpConfig.getPathCondition(samplerProxy.getPath(), item);
                    if (config != null) {
                        isNext = false;
                        httpConfig = config;
                        break;
                    }
                } else if (item.getType().equals(ConditionType.MODULE.name())) {
                    ApiDefinition apiDefinition = null;
                    ApiDefinitionService apiDefinitionService = CommonBeanFactory.getBean(ApiDefinitionService.class);
                    ApiTestCaseService apiTestCaseService = CommonBeanFactory.getBean(ApiTestCaseService.class);
                    if (StringUtils.isNotEmpty(samplerProxy.getRefType()) && samplerProxy.getRefType().equals(CommonConstants.CASE)) {
                        ApiTestCaseWithBLOBs caseWithBLOBs = apiTestCaseService.get(samplerProxy.getId());
                        if (caseWithBLOBs != null) {
                            apiDefinition = apiDefinitionService.get(caseWithBLOBs.getApiDefinitionId());
                        }
                    } else {
                        apiDefinition = apiDefinitionService.get(samplerProxy.getId());
                        // 兼容导入数据
                        if (apiDefinition == null) {
                            apiDefinition = apiDefinitionService.get(samplerProxy.getName());
                        }
                        ApiTestCaseWithBLOBs apiTestCaseWithBLOBs = apiTestCaseService.get(samplerProxy.getId());
                        if (apiTestCaseWithBLOBs == null) {
                            apiTestCaseWithBLOBs = apiTestCaseService.get(samplerProxy.getName());
                        }
                        if (apiTestCaseWithBLOBs != null) {
                            apiDefinition = apiDefinitionService.get(apiTestCaseWithBLOBs.getApiDefinitionId());
                        } else {
                            TestPlanApiCaseService testPlanApiCaseService = CommonBeanFactory.getBean(TestPlanApiCaseService.class);
                            TestPlanApiCase testPlanApiCase = testPlanApiCaseService.getById(samplerProxy.getId());
                            if (testPlanApiCase != null) {
                                ApiTestCaseWithBLOBs caseWithBLOBs = apiTestCaseService.get(testPlanApiCase.getApiCaseId());
                                if (caseWithBLOBs != null) {
                                    apiDefinition = apiDefinitionService.get(caseWithBLOBs.getApiDefinitionId());
                                }
                            }
                        }
                    }
                    if (apiDefinition != null) {
                        HttpConfig config = httpConfig.getModuleCondition(apiDefinition.getModuleId(), item);
                        if (config != null) {
                            isNext = false;
                            httpConfig = config;
                            break;
                        }
                    }
                }
            }
            if (isNext) {
                for (HttpConfigCondition item : httpConfig.getConditions()) {
                    if (item.getType().equals(ConditionType.NONE.name())) {
                        httpConfig = httpConfig.initHttpConfig(item);
                        break;
                    }
                }
            }
        }
        return httpConfig;
    }

    private String getParentProjectId(MsHTTPSamplerProxy samplerProxy) {
        MsTestElement parent = samplerProxy.getParent();
        while (parent != null) {
            if (StringUtils.isNotBlank(parent.getProjectId())) {
                return parent.getProjectId();
            }
            parent = parent.getParent();
        }
        return "";
    }

    public void compatible(MsHTTPSamplerProxy samplerProxy) {
        if (samplerProxy.isCustomizeReq() && samplerProxy.getIsRefEnvironment() == null) {
            if (StringUtils.isNotBlank(samplerProxy.getUrl())) {
                samplerProxy.setIsRefEnvironment(false);
            } else {
                samplerProxy.setIsRefEnvironment(true);
            }
        }

        // 数据兼容处理
        if (this.getConfig() != null && StringUtils.isNotEmpty(samplerProxy.getProjectId()) && this.getConfig().containsKey(samplerProxy.getProjectId())) {
            // 1.8 之后 当前正常数据
        } else if (this.getConfig() != null && this.getConfig().containsKey(getParentProjectId(samplerProxy))) {
            // 1.8 前后 混合数据
            samplerProxy.setProjectId(getParentProjectId(samplerProxy));
        } else {
            // 1.8 之前 数据
            if (this.getConfig() != null) {
                if (!this.getConfig().containsKey(RunModeConstants.HIS_PRO_ID.toString())) {
                    // 测试计划执行
                    Iterator<String> it = this.getConfig().keySet().iterator();
                    if (it.hasNext()) {
                        samplerProxy.setProjectId(it.next());
                    }
                } else {
                    samplerProxy.setProjectId(RunModeConstants.HIS_PRO_ID.toString());
                }
            }
        }
    }

    public void margeVariables(List<ScenarioVariable> variables, List<ScenarioVariable> transferVariables) {
        if (CollectionUtils.isNotEmpty(transferVariables) && CollectionUtils.isNotEmpty(variables)) {
            List<ScenarioVariable> constants = variables.stream()
                    .filter(ScenarioVariable::isConstantValid).collect(Collectors.toList());

            Map<String, List<ScenarioVariable>> transferVariableGroup = transferVariables.stream()
                    .collect(Collectors.groupingBy(ScenarioVariable::getName, LinkedHashMap::new, Collectors.toList()));

            Map<String, List<ScenarioVariable>> constantsGroup = constants.stream()
                    .collect(Collectors.groupingBy(ScenarioVariable::getName, LinkedHashMap::new, Collectors.toList()));
            // 更新相同名称的值
            for (ScenarioVariable constant : constants) {
                if (transferVariableGroup.containsKey(constant.getName())
                        && CollectionUtils.isNotEmpty(transferVariableGroup.get(constant.getName()))) {
                    constant.setValue(transferVariableGroup.get(constant.getName()).get(0).getValue());
                }
            }
            // 添加当前没有的值
            List<ScenarioVariable> transferConstants = transferVariables.stream()
                    .filter(ScenarioVariable::isConstantValid).collect(Collectors.toList());
            transferConstants.forEach(item -> {
                if (!constantsGroup.containsKey(item.getName())) {
                    variables.add(item);
                }
            });
        }
    }

    /**
     * 获取项目环境配置，如果没有则返回当前项目环境配置
     *
     * @param projectId 项目ID
     */
    public EnvironmentConfig get(String projectId) {
        EnvironmentConfig envConfig = this.getConfig().get(projectId);
        if (envConfig == null && StringUtils.isNotEmpty(this.getCurrentProjectId())) {
            return this.config.get(this.getCurrentProjectId());
        }
        return envConfig;
    }
}
