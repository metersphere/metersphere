package io.metersphere.sdk.constants;

import io.metersphere.sdk.util.Translator;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
public enum CaseType {

    /**
     * 功能用例
     */
    FUNCTIONAL_CASE("FUNCTIONAL", "test_case"),
    /**
     * 接口用例
     */
    API_CASE("API", "api_case"),
    /**
     * 性能用例
     */
    SCENARIO_CASE("SCENARIO", "scenario_case"),
    /**
     * UI用例
     */
    UI_CASE("UI", "ui_case"),
    /**
     * 性能用例
     */
    PERFORMANCE_CASE("PERFORMANCE", "performance_case");

    private final String key;

    private final String value;

    CaseType(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getValue() {
        return Translator.get(value);
    }

    public static String getValue(String key) {
        for (CaseType caseType : CaseType.values()) {
            if (StringUtils.equals(caseType.getKey(), key)) {
                return caseType.getValue();
            }
        }
        return null;
    }
}
