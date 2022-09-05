package io.metersphere.commons.constants;

public enum FileModuleTypeConstants {
    MODULE("module"),REPOSITORY("repository");

    private String value;

    FileModuleTypeConstants(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public String getValue() {
        return this.value;
    }
}
