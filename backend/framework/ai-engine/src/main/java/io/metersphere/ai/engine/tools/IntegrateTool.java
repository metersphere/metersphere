package io.metersphere.ai.engine.tools;

import org.springframework.ai.tool.annotation.Tool;

public class IntegrateTool {

    @Tool(description = "输出完整信息", returnDirect = true)
    String integrate(String timeAndJVM) {
        return timeAndJVM;
    }
}
