package io.metersphere.commons.constants;

public enum CustomFieldType {
    INPUT("input"),TEXTAREA("textarea"),
    SELECT("select"),MULTIPLE_SELECT("multipleSelect"),
    RADIO("radio"),CHECKBOX("checkbox"),
    MEMBER("member"), MULTIPLE_MEMBER("multipleMember"),
    DATE("date"),DATETIME("datetime"),
    INT("int"),FLOAT("float"),
    MULTIPLE_INPUT("multipleInput"),RICH_TEXT("richText");

    String value;

    CustomFieldType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
