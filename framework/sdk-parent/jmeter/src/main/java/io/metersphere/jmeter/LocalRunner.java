package io.metersphere.jmeter;

import io.metersphere.utils.LoggerUtil;
import org.apache.jmeter.engine.JMeterEngineException;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jorphan.collections.HashTree;

public class LocalRunner {
    private HashTree jmxTree;

    public LocalRunner(HashTree jmxTree) {
        this.jmxTree = jmxTree;
    }

    public LocalRunner() {
    }

    public void run(String report) {
        StandardJMeterEngine engine = new StandardJMeterEngine();
        engine.configure(jmxTree);
        try {
            LoggerUtil.info("LocalRunner 开始执行报告",report);
            engine.runTest();
        } catch (JMeterEngineException e) {
            engine.stopTest(true);
        }
    }
}