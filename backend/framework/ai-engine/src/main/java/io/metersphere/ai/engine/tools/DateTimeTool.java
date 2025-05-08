package io.metersphere.ai.engine.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class DateTimeTool {

    @Tool(name = "getCurrentDateTime", description = "当前所在的时区时间")
    Long getCurrentDateTime() {
        return System.currentTimeMillis();
    }

}
