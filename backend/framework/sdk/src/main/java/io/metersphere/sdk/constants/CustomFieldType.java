package io.metersphere.sdk.constants;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum CustomFieldType {
    INPUT( false),
    TEXTAREA(false),
    SELECT( true),
    MULTIPLE_SELECT(true),
    RADIO(true),
    CHECKBOX(true),
    MEMBER(true),
    MULTIPLE_MEMBER(true),
    DATE(false),
    DATETIME(false),
    INT(false),
    FLOAT(false),
    MULTIPLE_INPUT(false),
    RICH_TEXT(false),
    CASCADING_SELECT(false);

    private final Boolean hasOption;

    CustomFieldType(Boolean hasOption) {
        this.hasOption = hasOption;
    }

    public Boolean getHasOption() {
        return this.hasOption;
    }

    public static Set<String> getHasOptionValueSet() {
        return Arrays.stream(CustomFieldType.values())
                .filter(CustomFieldType::getHasOption)
                .map(CustomFieldType::name)
                .collect(Collectors.toSet());
    }
}
