package io.metersphere.sdk.constants;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
public enum CaseType {

    /**
     * 功能用例
     */
    FUNCTIONAL_CASE("FUNCTIONAL", "test_case", PermissionConstants.FUNCTIONAL_CASE_READ, "functional_case", "functional_case_module", "functional_case.module.default.name", "test_plan_default_functional_collection_name"),
    /**
     * 接口用例
     */
    API_CASE("API", "api_case", PermissionConstants.PROJECT_API_DEFINITION_CASE_READ, "api_test_case", "api_definition_module", "api_unplanned_request", "test_plan_default_api_collection_name"),
    /**
     * 场景用例
     */
    SCENARIO_CASE("SCENARIO", "scenario_case", PermissionConstants.PROJECT_API_SCENARIO_READ, "api_scenario", "api_scenario_module", "api_unplanned_scenario", "test_plan_default_scenario_collection_name");

    private final String key;

    private final String type;

    private final String usePermission;

    private final String caseTable;

    private final String moduleTable;

    private final String unPlanName;

    private final String planDefaultCollection;

    CaseType(String key, String type, String usePermission, String caseTable, String moduleTable, String unPlanName, String planDefaultCollection) {
        this.key = key;
        this.type = type;
        this.usePermission = usePermission;
        this.caseTable = caseTable;
        this.moduleTable = moduleTable;
        this.unPlanName = unPlanName;
        this.planDefaultCollection = planDefaultCollection;
    }

    public static CaseType getType(String key) {
        for (CaseType caseType : CaseType.values()) {
            if (StringUtils.equalsIgnoreCase(caseType.getKey(), key)) {
                return caseType;
            }
        }
        return null;
    }
}
