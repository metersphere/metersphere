package io.metersphere.api.parser.jmeter;

import io.metersphere.api.dto.ApiParamConfig;
import io.metersphere.api.dto.request.MsScenario;
import io.metersphere.api.dto.scenario.ScenarioOtherConfig;
import io.metersphere.api.parser.TestElementParser;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.plugin.api.spi.AbstractJmeterElementConverter;
import io.metersphere.plugin.api.spi.AbstractMsProtocolTestElement;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.dto.environment.EnvironmentInfoDTO;
import io.metersphere.project.dto.environment.variables.CommonVariables;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.LogUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.modifiers.UserParameters;
import org.apache.jmeter.sampler.DebugSampler;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Optional;

/**
 * @Author: jianxing
 * @CreateTime: 2023-10-27  10:07
 * <p>
 * 将 Ms 的组件转换为 jmx 脚本
 */
public class JmeterTestElementParser implements TestElementParser {

    private String name;
    private ParameterConfig config;

    /**
     * 解析生成 jmx 脚本
     *
     * @param msTestElement 组件
     * @param config        参数配置
     * @return jmx 脚本
     */
    @Override
    public String parse(AbstractMsTestElement msTestElement, ParameterConfig config) {
        this.config = config;
        HashTree hashTree = new ListedHashTree();
        TestPlan testPlan = getPlan();

        // 添加场景ID到 JMeter 测试计划变量中
        if (msTestElement instanceof MsScenario msScenario) {
            Arguments arguments = new Arguments();
            arguments.addArgument("ms_scenario_id", msScenario.getResourceId());
            testPlan.setUserDefinedVariables(arguments);
        }

        name = msTestElement.getName();
        final HashTree testPlanTree = hashTree.add(testPlan);
        final HashTree groupTree = testPlanTree.add(getThreadGroup(msTestElement));

        if (msTestElement instanceof AbstractMsProtocolTestElement) {
            // 如果是单接口执行，添加环境变量，接口插件也需要支持访问变量
            // 场景执行时，场景解析器里会处理变量
            ApiParamConfig apiParamConfig = (ApiParamConfig) config;
            EnvironmentInfoDTO envConfig = apiParamConfig.getEnvConfig(msTestElement.getProjectId());
            // 处理环境变量
            UserParameters userParameters = getEnvUserParameters(msTestElement.getName(), envConfig);
            Optional.ofNullable(userParameters).ifPresent(groupTree::add);
        }

        // 拦截器拦截
        HashTree wrapperTree = AbstractJmeterElementConverter.intercept(config, msTestElement, groupTree);

        // 解析 msTestElement
        JmeterElementConverterRegister.getConverter(msTestElement.getClass()).toHashTree(wrapperTree, msTestElement, config);

        // 添加 debugSampler，放最后才能采集到变量信息
        groupTree.add(getDebugSampler());

        return getJmx(hashTree);
    }

    public String getJmx(HashTree hashTree) {
        try (ByteArrayOutputStream bas = new ByteArrayOutputStream()) {
            SaveService.saveTree(hashTree, bas);
            return bas.toString();
        } catch (Exception e) {
            LogUtils.error(e);
            throw new MSException(e);
        }
    }

    public TestPlan getPlan() {
        TestPlan testPlan = new TestPlan(name);
        testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
        testPlan.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestPlanGui"));
        testPlan.setEnabled(true);
        testPlan.setFunctionalMode(false);
        testPlan.setSerialized(false);
        testPlan.setTearDownOnShutdown(true);
        testPlan.setUserDefinedVariables(new Arguments());
        return testPlan;
    }

    public ThreadGroup getThreadGroup(AbstractMsTestElement msTestElement) {
        LoopController loopController = new LoopController();
        loopController.setName("LoopController");
        loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
        loopController.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("LoopControlPanel"));
        loopController.setEnabled(true);
        loopController.setLoops(1);

        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setEnabled(true);
        threadGroup.setName(config.getTaskItemId());
        threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
        threadGroup.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("ThreadGroupGui"));
        threadGroup.setNumThreads(1);
        threadGroup.setRampUp(1);
        threadGroup.setDelay(0);
        threadGroup.setDuration(0);
        threadGroup.setProperty(ThreadGroup.ON_SAMPLE_ERROR, ThreadGroup.ON_SAMPLE_ERROR_CONTINUE);
        threadGroup.setScheduler(false);

        // 设置失败停止
        if (msTestElement instanceof MsScenario msScenario && msScenario.getScenarioConfig() != null) {
            ScenarioOtherConfig otherConfig = msScenario.getScenarioConfig().getOtherConfig();
            if (StringUtils.equals(otherConfig.getFailureStrategy(), ScenarioOtherConfig.FailureStrategy.STOP.name())) {
                threadGroup.setProperty("ThreadGroup.on_sample_error", "stopthread");
            }
        }

        threadGroup.setSamplerController(loopController);
        return threadGroup;
    }

    private DebugSampler getDebugSampler() {
        DebugSampler debugSampler = new DebugSampler();
        debugSampler.setEnabled(true);
        debugSampler.setName("RunningDebugSampler");
        debugSampler.setProperty(TestElement.TEST_CLASS, DebugSampler.class.getName());
        debugSampler.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestBeanGUI"));

        boolean displaySystemProperties = false;
        debugSampler.setDisplaySystemProperties(displaySystemProperties);
        boolean displayJMeterVariables = true;
        debugSampler.setDisplayJMeterVariables(displayJMeterVariables);
        boolean displayJMeterProperties = false;
        debugSampler.setDisplayJMeterProperties(displayJMeterProperties);

        // 上面三行直接Set属性会导致DebugSampler构建时取不到值，可能是JMeter的Bug,需要SetProperty
        debugSampler.setProperty("displayJMeterProperties", displayJMeterProperties);
        debugSampler.setProperty("displayJMeterVariables", displayJMeterVariables);
        debugSampler.setProperty("displaySystemProperties", displaySystemProperties);
        return debugSampler;
    }

    /**
     * 添加环境变量
     *
     * @param name
     * @param envInfo
     */
    private UserParameters getEnvUserParameters(String name, EnvironmentInfoDTO envInfo) {
        if (envInfo == null) {
            return null;
        }

        List<CommonVariables> envVariables = envInfo.getConfig().getCommonVariables();
        envVariables = envVariables.stream()
                .filter(variable -> BooleanUtils.isTrue(variable.getEnable()) && variable.isValid())
                .toList();
        if (CollectionUtils.isEmpty(envVariables)) {
            return null;
        }

        return JmeterTestElementParserHelper.getUserParameters(name, envVariables);
    }
}
