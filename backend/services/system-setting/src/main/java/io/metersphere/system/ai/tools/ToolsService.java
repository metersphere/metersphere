package io.metersphere.system.ai.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class ToolsService {

    @Tool(description = "Get the weather in location")
    public String getWeatherInLocation(String location) {
        return switch (location) {
            case "beijing" -> "25";
            case "tianjin" -> "24";
            case "nanjing" -> "23";
            default -> "32";
        };
    }
}
