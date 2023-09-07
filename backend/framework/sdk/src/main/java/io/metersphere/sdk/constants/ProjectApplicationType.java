package io.metersphere.sdk.constants;

/**
 * 应用设置 -相关配置
 */
public enum ProjectApplicationType {
    /**
     * 工作台 我的待办
     */
    APPLICATION_WORKSTATION,


    //测试计划
    /**
     * 测试计划报告保留范围 value
     */
    APPLICATION_CLEAN_TEST_PLAN_REPORT,
    /**
     * 测试报告有效期 value
     */
    APPLICATION_SHARE_TEST_PLAN_REPORT,


    //UI测试
    /**
     * UI报告保留范围 value
     */
    APPLICATION_CLEAN_UI_REPORT,
    /**
     * UI报告有效期 value
     */
    APPLICATION_SHARE_UI_REPORT,


    //性能测试
    /**
     * 性能测试报告保留范围 value
     */
    APPLICATION_CLEAN_PERFORMANCE_TEST_REPORT,
    /**
     * 性能测试报告有效期 value
     */
    APPLICATION_SHARE_PERFORMANCE_TEST_REPORT,
    /**
     * 性能测试脚本审核人
     */
    APPLICATION_PERFORMANCE_TEST_SCRIPT_REVIEWER,


    //接口测试
    /**
     * 接口定义 URL可重复
     */
    APPLICATION_API_URL_REPEATABLE,
    /**
     * 接口测试 报告保留范围
     */
    APPLICATION_CLEAN_API_REPORT,
    /**
     * 接口测试 报告有效期
     */
    APPLICATION_SHARE_API_REPORT,
    /**
     * 接口测试 执行资源池
     */
    APPLICATION_API_RESOURCE_POOL,
    /**
     * 接口测试 脚本审核人
     */
    APPLICATION_API_SCRIPT_REVIEWER,
    /**
     * 接口测试 自定义误报规则
     */
    APPLICATION_API_ERROR_REPORT_RULE,
    /**
     * 接口测试 接口变更同步case
     */
    APPLICATION_API_SYNC_CASE,



    //缺陷管理
    /**
     * 同步缺陷 标识
     */
    APPLICATION_ISSUE,

    /**
     * 缺陷模板
     */
    APPLICATION_ISSUE_TEMPLATE,




}
