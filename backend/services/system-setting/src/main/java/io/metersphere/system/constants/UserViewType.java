package io.metersphere.system.constants;

import io.metersphere.sdk.constants.ValueEnum;
import io.metersphere.sdk.exception.MSException;

import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2024-09-02  10:47
 * <p>
 * 视图的类型
 * 例如：功能用例视图
 */
public enum UserViewType implements ValueEnum {

    FUNCTIONAL_CASE("functional-case",
            List.of(InternalUserView.ALL_DATA, InternalUserView.MY_FOLLOW, InternalUserView.MY_CREATE)),
    REVIEW_FUNCTIONAL_CASE("review-functional-case",
            List.of(InternalUserView.ALL_DATA, InternalUserView.MY_REVIEW, InternalUserView.MY_CREATE)),
    CASE_REVIEW("case-review",
                List.of(InternalUserView.ALL_DATA, InternalUserView.MY_FOLLOW, InternalUserView.MY_CREATE)),
    API_DEFINITION("api-definition",
            List.of(InternalUserView.ALL_DATA, InternalUserView.MY_FOLLOW, InternalUserView.MY_CREATE)),
    API_CASE("api-case",
            List.of(InternalUserView.ALL_DATA, InternalUserView.MY_FOLLOW, InternalUserView.MY_CREATE)),
    API_MOCK("api-mock",
            List.of(InternalUserView.ALL_DATA, InternalUserView.MY_CREATE)),
    API_SCENARIO("api-scenario",
            List.of(InternalUserView.ALL_DATA, InternalUserView.MY_FOLLOW, InternalUserView.MY_CREATE)),
    BUG("bug",
            List.of(InternalUserView.ALL_DATA, InternalUserView.MY_FOLLOW, InternalUserView.MY_CREATE)),
    /**
     * 计划详情用例&&缺陷列表
     */
    PLAN_FUNCTIONAL_CASE("plan-functional-case",
            List.of(InternalUserView.ALL_DATA, InternalUserView.MY_CREATE)),
    PLAN_API_CASE("plan-api-case",
            List.of(InternalUserView.ALL_DATA, InternalUserView.MY_CREATE)),
    PLAN_API_SCENARIO("plan-api-scenario",
            List.of(InternalUserView.ALL_DATA, InternalUserView.MY_CREATE)),
    PLAN_BUG("plan-bug",
            List.of(InternalUserView.ALL_DATA, InternalUserView.MY_CREATE)),
    /**
     * 计划关联用例&&缺陷弹窗
     */
    PLAN_FUNCTIONAL_CASE_DRAWER("plan-functional-case-drawer",
            List.of(InternalUserView.ALL_DATA, InternalUserView.MY_CREATE)),
    PLAN_API_DEFINITION_DRAWER("plan-api-definition-drawer",
            List.of(InternalUserView.ALL_DATA, InternalUserView.MY_CREATE)),
    PLAN_API_CASE_DRAWER("plan-api-case-drawer",
            List.of(InternalUserView.ALL_DATA, InternalUserView.MY_CREATE)),
    PLAN_API_SCENARIO_DRAWER("plan-api-scenario-drawer",
            List.of(InternalUserView.ALL_DATA, InternalUserView.MY_CREATE)),
    PLAN_BUG_DRAWER("plan-bug-drawer",
            List.of(InternalUserView.ALL_DATA, InternalUserView.MY_CREATE)),

    /**
     * 测试计划列表
     */
    TEST_PLAN_ALL("test-plan-all",
            List.of(InternalUserView.ALL_DATA, InternalUserView.ARCHIVED, InternalUserView.MY_CREATE)),
    TEST_PLAN("test-plan",
            List.of(InternalUserView.ALL_DATA, InternalUserView.ARCHIVED, InternalUserView.MY_FOLLOW, InternalUserView.MY_CREATE)),
    TEST_PLAN_GROUP("test-plan-group",
            List.of(InternalUserView.ALL_DATA, InternalUserView.ARCHIVED, InternalUserView.MY_CREATE)),

    /**
     * 测试计划报告
     */
    TEST_PLAN_REPORT("test-plan-report",
            List.of(InternalUserView.ALL_DATA, InternalUserView.MY_CREATE)),

    /**
     * 接口报告
     */
    API_REPORT("api-report",
            List.of(InternalUserView.ALL_DATA, InternalUserView.MY_CREATE));

    private String value;
    private List<InternalUserView> internalViews;

    UserViewType(String value, List<InternalUserView> internalViews) {
        this.value = value;
        this.internalViews = internalViews;
    }

    @Override
    public Object getValue() {
        return this.value;
    }

    public List<InternalUserView> getInternalViews() {
        return internalViews;
    }

    public static UserViewType getByValue(String value) {
        for (UserViewType userViewType : UserViewType.values()) {
            if (userViewType.value.equals(value)) {
                return userViewType;
            }
        }
        throw new MSException("No such view type");
    }
}
