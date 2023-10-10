package io.metersphere.sdk.constants;

public enum UserRoleEnum {
    GLOBAL("global");
    
    private final String value;

    UserRoleEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
