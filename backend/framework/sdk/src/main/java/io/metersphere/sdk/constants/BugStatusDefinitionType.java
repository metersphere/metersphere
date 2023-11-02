package io.metersphere.sdk.constants;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum BugStatusDefinitionType {
    /**
     * 起始状态
     */
    START("status_definition.type.start", true),
    /**
     * 结束状态
     */
    END("status_definition.type.end", false);

    BugStatusDefinitionType(String name, Boolean isSingleChoice) {
        this.name = name;
        this.isSingleChoice = isSingleChoice;
    }

    /**
     * 状态名
     */
    private String name;
    /**
     * 是否是单选
     */
    private Boolean isSingleChoice;

    public static BugStatusDefinitionType getStatusDefinitionType(String type) {
        return Arrays.stream(BugStatusDefinitionType.values()).filter(item -> item.name().equals(type))
                .findFirst().orElse(null);
    }
}
