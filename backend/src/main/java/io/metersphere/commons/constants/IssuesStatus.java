package io.metersphere.commons.constants;

public enum IssuesStatus {
    NEW("new"), CLOSED("closed"), RESOLVED("resolved"), DELETE("delete");
    private String value;
    IssuesStatus(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
