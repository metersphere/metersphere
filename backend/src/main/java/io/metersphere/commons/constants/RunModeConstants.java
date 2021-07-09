package io.metersphere.commons.constants;

public enum RunModeConstants {

    SERIAL("serial"), SET_REPORT("setReport"), PARALLEL("parallel"), HIS_PRO_ID("historyProjectID");

    private String value;

    RunModeConstants(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
