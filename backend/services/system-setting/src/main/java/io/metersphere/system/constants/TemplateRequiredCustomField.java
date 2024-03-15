package io.metersphere.system.constants;

/**
 * @Author: jianxing
 * @CreateTime: 2024-03-14  16:33
 */
public enum TemplateRequiredCustomField {
    BUG_DEGREE("functional_priority");

    private String name;

    TemplateRequiredCustomField(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
