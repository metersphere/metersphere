package io.metersphere.runner;

import io.metersphere.base.domain.FileContent;
import io.metersphere.base.domain.LoadTestWithBLOBs;
import io.metersphere.commons.constants.LoadTestFileType;
import io.metersphere.runner.jmx.JmxEngine;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class EngineFactory {
    public static Engine createEngine(String engineType) {
        final LoadTestFileType type = LoadTestFileType.valueOf(engineType);

        if (type == LoadTestFileType.JMX) {
            return new JmxEngine();
        }
        return null;
    }

    public static EngineContext createContext(LoadTestWithBLOBs loadTest, FileContent fileContent) {
        final EngineContext engineContext = new EngineContext();
        engineContext.setEngineId(loadTest.getId());
        engineContext.setInputStream(new ByteArrayInputStream(fileContent.getFile().getBytes(StandardCharsets.UTF_8)));

        return engineContext;
    }
}
