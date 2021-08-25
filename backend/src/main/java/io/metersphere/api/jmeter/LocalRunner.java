package io.metersphere.api.jmeter;

import io.metersphere.commons.utils.LogUtil;
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
            engine.runTest();
            MessageCache.runningEngine.put(report, engine);
        } catch (JMeterEngineException e) {
            engine.stopTest(true);
        }
    }

    public void stop(String report) {
        try {
            StandardJMeterEngine engine = MessageCache.runningEngine.get(report);
            if (engine != null) {
                engine.stopTest();
                MessageCache.runningEngine.remove(report);
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }
}
