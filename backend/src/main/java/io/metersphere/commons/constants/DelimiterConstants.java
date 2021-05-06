package io.metersphere.commons.constants;

public enum DelimiterConstants {
    STEP_DELIMITER("^@~@^"), SEPARATOR("<->");
    private String value;

    DelimiterConstants(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
