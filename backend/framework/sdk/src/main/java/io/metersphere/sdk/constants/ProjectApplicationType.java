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
     * 自定义测试计划报告保留范围 是否开启标识 type
     */
    APPLICATION_CLEAN_TEST_PLAN_REPORT,

    /**
     * 自定义测试计划报告保留范围 value
     */
    APPLICATION_CLEAN_TEST_PLAN_REPORT_VALUE,
    /**
     * 自定义测试报告有效期 是否开启标识 type
     */
    APPLICATION_TEST_PLAN_SHARE_REPORT,

    /**
     * 自定义测试报告有效期 value
     */
    APPLICATION_TEST_PLAN_SHARE_REPORT_VALUE,


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
