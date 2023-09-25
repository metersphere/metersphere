package io.metersphere.sdk.constants;

import io.metersphere.sdk.dto.request.CustomFieldOptionRequest;

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
    private List<CustomFieldOptionRequest> options;

    DefaultFunctionalCustomField(String name, CustomFieldType type, List<CustomFieldOptionRequest> options) {
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

    public List<CustomFieldOptionRequest> getOptions() {
        return options;
    }

    private static CustomFieldOptionRequest getNewOption(String value, String text) {
        CustomFieldOptionRequest customFieldOption = new CustomFieldOptionRequest();
        customFieldOption.setValue(value);
        customFieldOption.setText(text);
        return customFieldOption;
    }
}
