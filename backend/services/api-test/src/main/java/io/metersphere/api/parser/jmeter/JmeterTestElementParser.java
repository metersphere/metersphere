package io.metersphere.api.parser.jmeter;

import io.metersphere.api.parser.TestElementParser;
import io.metersphere.api.utils.JmeterElementConverterRegister;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.LogUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.sampler.DebugSampler;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;

import java.io.ByteArrayOutputStream;

/**
 * @Author: jianxing
 * @CreateTime: 2023-10-27  10:07
 * <p>
 * 将 Ms 的组件转换为 jmx 脚本
 */
public class JmeterTestElementParser implements TestElementParser {

    private Boolean onSampleError;
    private String name;
    private Boolean enable = true;
    private ParameterConfig config;
    private boolean displayJMeterProperties = false;

    private boolean displayJMeterVariables = true;

    private boolean displaySystemProperties = false;

    @Override
    public String parse(AbstractMsTestElement msTestElement, ParameterConfig config) {
        this.config = config;
        HashTree hashTree = new ListedHashTree();
        TestPlan testPlan = getPlan();
        name = msTestElement.getName();
        enable = msTestElement.getEnable();
        final HashTree testPlanTree = hashTree.add(testPlan);
        final HashTree groupTree = testPlanTree.add(getThreadGroup());
        // 添加 debugSampler
        groupTree.add(getDebugSampler());

        // 解析 msTestElement
        JmeterElementConverterRegister.getConverter(msTestElement.getClass())
                .toHashTree(groupTree, msTestElement, config);

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

    public ThreadGroup getThreadGroup() {
        LoopController loopController = new LoopController();
        loopController.setName("LoopController");
        loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
        loopController.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("LoopControlPanel"));
        loopController.setEnabled(this.enable);
        loopController.setLoops(1);

        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setEnabled(this.enable);
        threadGroup.setName(config.getReportId());
        threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
        threadGroup.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("ThreadGroupGui"));
        threadGroup.setNumThreads(1);
        threadGroup.setRampUp(1);
        threadGroup.setDelay(0);
        threadGroup.setDuration(0);
        threadGroup.setProperty(ThreadGroup.ON_SAMPLE_ERROR, ThreadGroup.ON_SAMPLE_ERROR_CONTINUE);
        threadGroup.setScheduler(false);
        if (onSampleError != null && !onSampleError) {
            threadGroup.setProperty("ThreadGroup.on_sample_error", "stopthread");
        }
        threadGroup.setSamplerController(loopController);
        return threadGroup;
    }

    private DebugSampler getDebugSampler() {
        DebugSampler debugSampler = new DebugSampler();
        debugSampler.setEnabled(true);
        debugSampler.setName("DebugSampler");
        debugSampler.setProperty(TestElement.TEST_CLASS, DebugSampler.class.getName());
        debugSampler.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestBeanGUI"));

        debugSampler.setDisplaySystemProperties(this.displaySystemProperties);
        debugSampler.setDisplayJMeterVariables(this.displayJMeterVariables);
        debugSampler.setDisplayJMeterProperties(this.displayJMeterProperties);

        // 上面三行直接Set属性会导致DebugSampler构建时取不到值，可能是JMeter的Bug,需要SetProperty
        debugSampler.setProperty("displayJMeterProperties", this.displayJMeterProperties);
        debugSampler.setProperty("displayJMeterVariables", this.displayJMeterVariables);
        debugSampler.setProperty("displaySystemProperties", this.displaySystemProperties);
        return debugSampler;
    }
}
