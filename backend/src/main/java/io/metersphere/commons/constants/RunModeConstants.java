package io.metersphere.commons.constants;

public enum RunModeConstants {

    SERIAL("serial"), SET_REPORT("setReport"), PARALLEL("parallel");

    private String value;

    RunModeConstants(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
