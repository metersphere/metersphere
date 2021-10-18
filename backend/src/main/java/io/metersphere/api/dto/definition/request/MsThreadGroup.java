package io.metersphere.api.dto.definition.request;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONType;
import io.metersphere.api.dto.RunningParamKeys;
import io.metersphere.api.dto.definition.request.processors.MsJSR223Processor;
import io.metersphere.api.dto.definition.request.sampler.MsDebugSampler;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.definition.request.sampler.MsJDBCSampler;
import io.metersphere.api.dto.definition.request.sampler.MsTCPSampler;
import io.metersphere.api.dto.mockconfig.MockConfigStaticData;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.service.ApiTestEnvironmentService;
import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.commons.constants.RunModeConstants;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.HashTree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@JSONType(typeName = "ThreadGroup")
public class MsThreadGroup extends MsTestElement {
    private String type = "ThreadGroup";
    private String clazzName = "io.metersphere.api.dto.definition.request.MsThreadGroup";

    private boolean enableCookieShare;
    private Boolean onSampleError;

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, MsParameter msParameter) {
        ParameterConfig config = (ParameterConfig) msParameter;
        final HashTree groupTree = tree.add(getThreadGroup());
        if ((config != null && config.isEnableCookieShare()) || enableCookieShare) {
            CookieManager cookieManager = new CookieManager();
            cookieManager.setProperty(TestElement.TEST_CLASS, CookieManager.class.getName());
            cookieManager.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("CookiePanel"));
            cookieManager.setEnabled(true);
            cookieManager.setName("CookieManager");
            cookieManager.setClearEachIteration(false);
            cookieManager.setControlledByThread(false);
            groupTree.add(cookieManager);
        }

        if (CollectionUtils.isNotEmpty(hashTree)) {
            MsJSR223Processor preProcessor = null;
            MsJSR223Processor postProcessor = null;
            //获取projectConfig
            String projectId = this.checkProjectId(hashTree);
            this.checkEnviromentConfig(projectId,config,hashTree);
            if (config.getConfig() != null) {
                if (config.isEffective(projectId)) {
                    EnvironmentConfig environmentConfig = config.getConfig().get(projectId);
                    if (environmentConfig != null) {
                        preProcessor = environmentConfig.getPreProcessor();
                        postProcessor = environmentConfig.getPostProcessor();
                    }
                }
            }

            //检查全局前后置脚本
            if (preProcessor != null && StringUtils.isNotEmpty(preProcessor.getScript())) {
                preProcessor.setType("JSR223Processor");
                preProcessor.setName("PRE_PROCESSOR_ENV_"+preProcessor.isConnScenario());
                preProcessor.setClazzName("io.metersphere.api.dto.definition.request.processors.MsJSR223Processor");
                preProcessor.toHashTree(groupTree, preProcessor.getHashTree(), config);
            }
            for (MsTestElement el : hashTree) {
                el.toHashTree(groupTree, el.getHashTree(), config);
            }

            if (postProcessor != null && StringUtils.isNotEmpty(postProcessor.getScript())) {
                postProcessor.setType("JSR223Processor");
                postProcessor.setName("POST_PROCESSOR_ENV_"+preProcessor.isConnScenario());
                postProcessor.setClazzName("io.metersphere.api.dto.definition.request.processors.MsJSR223Processor");
                postProcessor.toHashTree(groupTree, postProcessor.getHashTree(), config);
            }
            MsDebugSampler el = new MsDebugSampler();
            el.setName(RunningParamKeys.RUNNING_DEBUG_SAMPLER_NAME);
            el.toHashTree(groupTree, el.getHashTree(), config);
        }
    }

    private String checkProjectId(List<MsTestElement> hashTree) {
        String projectId = this.getProjectId();
        if (StringUtils.isEmpty(projectId)) {
            for (MsTestElement el : hashTree) {
                if (el instanceof MsHTTPSamplerProxy) {
                    MsHTTPSamplerProxy httpSamplerProxy = (MsHTTPSamplerProxy) el;
                    projectId = httpSamplerProxy.getProjectId();
                    if (StringUtils.isNotEmpty(projectId)) {
                        break;
                    }
                } else if (el instanceof MsJDBCSampler) {
                    MsJDBCSampler jdbcSampler = (MsJDBCSampler) el;
                    projectId = jdbcSampler.getProjectId();
                    if (StringUtils.isNotEmpty(projectId)) {
                        break;
                    }
                } else if (el instanceof MsTCPSampler) {
                    MsTCPSampler tcpSampler = (MsTCPSampler) el;
                    projectId = tcpSampler.getProjectId();
                    if (StringUtils.isNotEmpty(projectId)) {
                        break;
                    }
                } else if (el instanceof MsScenario) {
                    MsScenario scenario = (MsScenario)el;
                    if(scenario.getHashTree() != null){
                        for (MsTestElement itemEl : scenario.getHashTree()) {
                            if (itemEl instanceof MsHTTPSamplerProxy) {
                                MsHTTPSamplerProxy httpSamplerProxy = (MsHTTPSamplerProxy) itemEl;
                                projectId = httpSamplerProxy.getProjectId();
                                if (StringUtils.isNotEmpty(projectId)) {
                                    break;
                                }
                            } else if (itemEl instanceof MsJDBCSampler) {
                                MsJDBCSampler jdbcSampler = (MsJDBCSampler) itemEl;
                                projectId = jdbcSampler.getProjectId();
                                if (StringUtils.isNotEmpty(projectId)) {
                                    break;
                                }
                            } else if (itemEl instanceof MsTCPSampler) {
                                MsTCPSampler tcpSampler = (MsTCPSampler) itemEl;
                                projectId = tcpSampler.getProjectId();
                                if (StringUtils.isNotEmpty(projectId)) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return projectId;
    }

    private void checkEnviromentConfig(String projectId, ParameterConfig config, List<MsTestElement> hashTree) {
        //检查全局前后置脚本
        if (config.getConfig() == null) {
            // 单独接口执行
            this.setProjectId(projectId);

            String environmentId = null;
            for (MsTestElement el : hashTree) {
                if (el instanceof MsHTTPSamplerProxy) {
                    MsHTTPSamplerProxy httpSamplerProxy = (MsHTTPSamplerProxy) el;
                    environmentId = httpSamplerProxy.getUseEnvironment();
                    if (StringUtils.isNotEmpty(environmentId)) {
                        break;
                    }
                } else if (el instanceof MsJDBCSampler) {
                    MsJDBCSampler jdbcSampler = (MsJDBCSampler) el;
                    environmentId = jdbcSampler.getEnvironmentId();
                    if (StringUtils.isNotEmpty(environmentId)) {
                        break;
                    }
                } else if (el instanceof MsTCPSampler) {
                    MsTCPSampler tcpSampler = (MsTCPSampler) el;
                    environmentId = tcpSampler.getUseEnvironment();
                    if (StringUtils.isNotEmpty(environmentId)) {
                        break;
                    }
                } else if (el instanceof MsScenario) {
                    Map<String, EnvironmentConfig> envConfig = new HashMap<>(16);
                    MsScenario scenario = (MsScenario)el;
                    // 兼容历史数据
                    if (scenario.getEnvironmentMap() == null || scenario.getEnvironmentMap().isEmpty()) {
                        scenario.setEnvironmentMap(new HashMap<>(16));
                        if (StringUtils.isNotBlank(environmentId)) {
                            // 兼容1.8之前 没有environmentMap但有environmentId的数据
                            scenario.getEnvironmentMap().put(RunModeConstants.HIS_PRO_ID.toString(), environmentId);
                        }
                    }
                    if (scenario.getEnvironmentMap() != null && !scenario.getEnvironmentMap().isEmpty()) {
                        scenario.getEnvironmentMap().keySet().forEach(itemProjectId -> {
                            ApiTestEnvironmentService environmentService = CommonBeanFactory.getBean(ApiTestEnvironmentService.class);
                            ApiTestEnvironmentWithBLOBs environment = environmentService.get(scenario.getEnvironmentMap().get(itemProjectId));
                            if (environment != null && environment.getConfig() != null) {
                                EnvironmentConfig env = JSONObject.parseObject(environment.getConfig(), EnvironmentConfig.class);
                                env.setApiEnvironmentid(environment.getId());
                                envConfig.put(itemProjectId, env);
                                if (StringUtils.equals(environment.getName(), MockConfigStaticData.MOCK_EVN_NAME)) {
                                    this.setMockEnvironment(true);
                                }
                            }
                        });
                        config.setConfig(envConfig);
                    }
                }
            }
            if (StringUtils.isNotEmpty(environmentId)) {
                config.setConfig(ElementUtil.getEnvironmentConfig(environmentId, projectId, this.isMockEnvironment()));
            }
        }
    }

    public ThreadGroup getThreadGroup() {
        LoopController loopController = new LoopController();
        loopController.setName("LoopController");
        loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
        loopController.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("LoopControlPanel"));
        loopController.setEnabled(this.isEnable());
        loopController.setLoops(1);

        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setEnabled(this.isEnable());
        threadGroup.setName(this.getName());
        threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
        threadGroup.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("ThreadGroupGui"));
        threadGroup.setNumThreads(1);
        threadGroup.setRampUp(1);
        threadGroup.setDelay(0);
        threadGroup.setDuration(0);
        threadGroup.setProperty(ThreadGroup.ON_SAMPLE_ERROR, ThreadGroup.ON_SAMPLE_ERROR_CONTINUE);
        threadGroup.setScheduler(false);
        if (onSampleError != null && !onSampleError) {
            threadGroup.setProperty("ThreadGroup.on_sample_error", "stoptest");
        }
        threadGroup.setSamplerController(loopController);
        return threadGroup;
    }

}
