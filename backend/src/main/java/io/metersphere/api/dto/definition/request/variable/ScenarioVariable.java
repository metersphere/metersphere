package io.metersphere.api.dto.definition.request.variable;

import io.metersphere.api.dto.scenario.request.BodyFile;
import lombok.Data;

import java.util.List;

@Data
public class ScenarioVariable {

    // CONSTANT LIST CSV COUNTER RANDOM
    private String type;
    private String name;
    // 常量值，列表值[] ,计数器输出格式，随机数输出格式
    private String value;
    private String description;
    // csv
    private List<BodyFile> files;
    private String splits;
    private String encoding;
    // counter
    private int startNumber;
    private int endNumber;
    private int increment;
    // random
    private int minNumber;
    private int maxNumber;

}
