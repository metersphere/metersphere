package io.metersphere.api.jmeter;

import io.metersphere.commons.utils.LogUtil;
import org.apache.jmeter.engine.JMeterEngine;
import org.apache.jmeter.engine.JMeterEngineException;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jorphan.collections.HashTree;

public class LocalRunner {
    private final HashTree jmxTree;

    public LocalRunner(HashTree jmxTree) {
        this.jmxTree = jmxTree;
    }

    public void run() {
        JMeterEngine engine = new StandardJMeterEngine();
        engine.configure(jmxTree);
        try {
            engine.runTest();
        } catch (JMeterEngineException e) {
            LogUtil.error("JMeterEngineException:", e);
            engine.stopTest(true);
        }
    }
}
