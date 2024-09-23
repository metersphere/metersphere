package io.metersphere.api.jmeter;

import io.metersphere.jmeter.LocalRunner;
import io.metersphere.utils.LoggerUtil;
import jakarta.annotation.PreDestroy;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jorphan.collections.HashTree;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@NoArgsConstructor
public class ApiLocalRunner extends LocalRunner {
    private static final Map<String, StandardJMeterEngine> runningTasks = new ConcurrentHashMap<>();
    private HashTree jmxTree;

    public ApiLocalRunner(HashTree jmxTree) {
        this.jmxTree = jmxTree;
    }

    public void run(String report) {
        StandardJMeterEngine engine = new StandardJMeterEngine();
        engine.configure(this.jmxTree);
        runningTasks.put(report, engine);
        try {
            LoggerUtil.info("LocalRunner 开始执行报告", report);
            engine.runTest();
        } catch (Exception e) {
            LoggerUtil.error("运行报告时出错", e);
            engine.stopTest(true);
        } finally {
            runningTasks.remove(report); // Ensure removal on completion
        }
    }

    public static void stop(String report) {
        if (StringUtils.isNotEmpty(report)) {
            StandardJMeterEngine engine = runningTasks.get(report);
            if (engine != null) {
                engine.stopTest(true);
                runningTasks.remove(report);
            }
        }
    }

    public static void clearCache(String report) {
        if (StringUtils.isNotEmpty(report)) {
            runningTasks.remove(report);
        }
    }

    @PreDestroy
    public void destroy() {
    }
}
