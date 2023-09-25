package io.metersphere.sdk.constants;

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
                    getNewOption("P1", "P1"),
                    getNewOption("P2", "P2"),
                    getNewOption("P3", "P3"),
                    getNewOption("P4", "P4")
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

    private static CustomFieldOption getNewOption(String value, String text) {
        CustomFieldOption customFieldOption = new CustomFieldOption();
        customFieldOption.setValue(value);
        customFieldOption.setText(text);
        customFieldOption.setInternal(true);
        return customFieldOption;
    }
}
