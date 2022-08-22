package io.metersphere.commons.constants;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum CustomFieldType {
    INPUT("input", false),
    TEXTAREA("textarea", false),
    SELECT("select", true),
    MULTIPLE_SELECT("multipleSelect", true),
    RADIO("radio", true),
    CHECKBOX("checkbox", true),
    MEMBER("member", true),
    MULTIPLE_MEMBER("multipleMember", true),
    DATE("date", false),
    DATETIME("datetime", false),
    INT("int", false),
    FLOAT("float", false),
    MULTIPLE_INPUT("multipleInput", false),
    RICH_TEXT("richText", false);

    private String value;
    private Boolean hasOption;

    CustomFieldType(String value, Boolean hasOption) {
        this.value = value;
        this.hasOption = hasOption;
    }

    public Boolean getHasOption() {
        return this.hasOption;
    }

    public static Set<String> getHasOptionValueSet() {
        return Arrays.stream(CustomFieldType.values())
                .filter(CustomFieldType::getHasOption)
                .map(CustomFieldType::getValue)
                .collect(Collectors.toSet());
    }

    public String getValue() {
        return value;
    }
}
