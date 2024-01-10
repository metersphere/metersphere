package io.metersphere.system.dto.request;

import io.metersphere.sdk.constants.CustomFieldType;
import io.metersphere.system.domain.CustomFieldOption;
import io.metersphere.system.uid.IDGenerator;

import java.util.Arrays;
import java.util.List;

/**
 * 默认的功能性自定义字段
 * 方便初始化项目模板
 */
public enum DefaultBugCustomField {

    /**
     * 严重程度
     */
    DEGREE("bug_degree", CustomFieldType.SELECT,
            Arrays.asList(
                    getNewOption(IDGenerator.nextStr(), "提示"),
                    getNewOption(IDGenerator.nextStr(), "一般"),
                    getNewOption(IDGenerator.nextStr(), "严重"),
                    getNewOption(IDGenerator.nextStr(), "致命")
            )
    );

    private final String name;
    private final CustomFieldType type;
    private final List<CustomFieldOption> options;

    DefaultBugCustomField(String name, CustomFieldType type, List<CustomFieldOption> options) {
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
