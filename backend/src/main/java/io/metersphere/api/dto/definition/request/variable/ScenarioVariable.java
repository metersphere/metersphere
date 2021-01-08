package io.metersphere.api.dto.definition.request.variable;

import io.metersphere.api.dto.scenario.request.BodyFile;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
public class ScenarioVariable {

    // CONSTANT LIST CSV COUNTER RANDOM
    private String type;
    private String id;
    private String name;
    // 常量值，列表值[] ,计数器输出格式，随机数输出格式
    private String value;
    private String description;
    // csv
    private List<BodyFile> files;
    private String delimiter;
    private String encoding;
    // counter
    private int startNumber;
    private int endNumber;
    private int increment;
    // random
    private String minNumber;
    private String maxNumber;

    public boolean isConstantValid() {
        if ((StringUtils.equals(this.type, EnumVariableType.CONSTANT.name()) || StringUtils.equals(this.type, EnumVariableType.LIST.name())) && StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(value)) {
            return true;
        }
        return false;
    }

    public boolean isCSVValid() {
        if (StringUtils.equals(this.type, EnumVariableType.CSV.name()) && StringUtils.isNotEmpty(name)) {
            return true;
        }
        return false;
    }

    public boolean isCounterValid() {
        if (StringUtils.equals(this.type, EnumVariableType.COUNTER.name()) && StringUtils.isNotEmpty(name)) {
            return true;
        }
        return false;
    }

    public boolean isRandom() {
        if (StringUtils.equals(this.type, EnumVariableType.RANDOM.name()) && StringUtils.isNotEmpty(name)) {
            return true;
        }
        return false;
    }
}
