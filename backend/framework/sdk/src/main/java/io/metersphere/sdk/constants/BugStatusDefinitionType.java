package io.metersphere.sdk.constants;

import io.metersphere.sdk.util.Translator;

public enum BugStatusDefinitionType {
    /**
     * 起始状态
     */
    START("status_definition.type.start"),
    /**
     * 结束状态
     */
    END("status_definition.type.end");

    BugStatusDefinitionType(String name) {
        this.name = name;
    }

    private String name;

    public String getName() {
        return Translator.get(name);
    }
}
