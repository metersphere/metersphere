package io.metersphere.dashboard.constants;

public enum DashboardUserLayoutKeys {
    /**
     * default
     */
    CREATE_BY_ME, //我创建的
    PROJECT_VIEW,//项目概览
    PROJECT_MEMBER_VIEW,//项目成员概览
    /**
     * functional
     */
    CASE_COUNT, //用例数量统计
    ASSOCIATE_CASE_COUNT,//关联用例统计
    REVIEW_CASE_COUNT,//用例评审数量统计
    REVIEWING_BY_ME, //待我评审
    /**
     * api
     */
    API_COUNT,//接口数量统计
    API_CASE_COUNT,//接口用例数量统计
    SCENARIO_COUNT,//场景用例数量统计
    API_CHANGE,//接口变更统计
    /**
     * test_plan
     */
    TEST_PLAN_COUNT,//测试计划数量统计
    PLAN_LEGACY_BUG,//计划遗留bug统计
    PROJECT_PLAN_VIEW,//测试计划概览
    /**
     * bug
     */
    BUG_COUNT,//缺陷数量统计
    CREATE_BUG_BY_ME, //我创建的缺陷
    HANDLE_BUG_BY_ME, //待我处理的缺陷
    BUG_HANDLE_USER // 缺陷处理人统计




}
