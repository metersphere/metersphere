package io.metersphere.system.dto.request;

import io.metersphere.sdk.constants.CustomFieldType;
import io.metersphere.system.domain.CustomFieldOption;

import java.util.Arrays;
import java.util.List;

/**
 * 默认的功能性自定义字段
 * 方便初始化项目模板
 */
public enum DefaultFunctionalCustomField {

    PRIORITY("functional_priority", CustomFieldType.SELECT,
            Arrays.asList(
                    getNewOption("P0", "P0", 5000L),
                    getNewOption("P1", "P1", 10000L),
                    getNewOption("P2", "P2", 15000L),
                    getNewOption("P3", "P3", 20000L)
            )
    );

    private String name;
    private CustomFieldType type;
    private List<CustomFieldOption> options;

    DefaultFunctionalCustomField(String name, CustomFieldType type, List<CustomFieldOption> options) {
        this.name = name;
        this.type = type;
        this.options = options;
    }

    public CustomFieldType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public List<CustomFieldOption> getOptions() {
        return options;
    }

    private static CustomFieldOption getNewOption(String value, String text, Long pos) {
        CustomFieldOption customFieldOption = new CustomFieldOption();
        customFieldOption.setValue(value);
        customFieldOption.setText(text);
        customFieldOption.setPos(pos);
        customFieldOption.setInternal(true);
        return customFieldOption;
    }
}
