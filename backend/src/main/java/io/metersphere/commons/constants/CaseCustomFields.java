package io.metersphere.commons.constants;

public enum CaseCustomFields {
    PRIORITY("用例等级"),
    MAINTAINER("责任人");

    private String value;

    private CaseCustomFields(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
