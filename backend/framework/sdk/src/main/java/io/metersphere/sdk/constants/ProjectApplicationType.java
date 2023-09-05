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
