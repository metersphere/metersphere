package io.metersphere.project.constants;

/**
 * @Author: jianxing
 * @CreateTime: 2024-01-19  18:19
 */
public enum ScriptLanguageType {
    BEANSHELL("beanshell"),
    BEANSHELL_JSR233("beanshell-jsr233"),
    GROOVY("groovy"),
    JAVASCRIPT("javascript"),
    PYTHON("python");

    private String value;

    ScriptLanguageType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
