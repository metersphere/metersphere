package io.metersphere.plan.enums;

public enum TestCaseReleevanceType {
    API_CASE("testcase"), SCENARIO("automation"), LOAD_CASE("performance"), UI_AUTOMATION("uiAutomation");
    String value;

    TestCaseReleevanceType(String value) {
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
