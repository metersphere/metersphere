package io.metersphere.api.dto.definition.request;

import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.definition.request.variable.ScenarioVariable;
import io.metersphere.api.dto.scenario.HttpConfig;
import io.metersphere.api.dto.scenario.HttpConfigCondition;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.dto.ssl.MsKeyStore;
import io.metersphere.api.service.ApiDefinitionService;
import io.metersphere.api.service.ApiTestCaseService;
import io.metersphere.base.domain.ApiDefinition;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import io.metersphere.base.domain.TestPlanApiCase;
import io.metersphere.commons.constants.ConditionType;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.jmeter.utils.ScriptEngineUtils;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.track.service.TestPlanApiCaseService;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;

import java.util.*;

@Data
public class ParameterConfig extends MsParameter {
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
     * 公共场景参数
     */
    private List<KeyValue> headers;

    /**
     * 公共Cookie
     */
    private boolean enableCookieShare;
    /**
     * 是否停止继续
     */
    private Boolean onSampleError;

    /**
     * 是否是导入/导出操作
     */
    private boolean isOperating;
    /**
     * 项目ID，支持单接口执行
     */
    private String projectId;

    private String scenarioId;

    private String reportType;
    /**
     * 排除生成临界控制器的场景
     */
    private List<String> excludeScenarioIds = new ArrayList<>();

    private List<String> csvFilePaths = new ArrayList<>();


    public boolean isEffective(String projectId) {
        if (this.config != null && this.config.get(projectId) != null) {
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


    public HttpConfig matchConfig(MsHTTPSamplerProxy samplerProxy) {
        HttpConfig httpConfig = this.getConfig().get(samplerProxy.getProjectId()).getHttpConfig();
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
                    if (StringUtils.isNotEmpty(samplerProxy.getRefType()) && samplerProxy.getRefType().equals("CASE")) {
                        ApiTestCaseWithBLOBs caseWithBLOBs = apiTestCaseService.get(samplerProxy.getId());
                        if (caseWithBLOBs != null) {
                            apiDefinition = apiDefinitionService.get(caseWithBLOBs.getApiDefinitionId());
                        }
                    } else {
                        apiDefinition = apiDefinitionService.get(samplerProxy.getId());
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
}
