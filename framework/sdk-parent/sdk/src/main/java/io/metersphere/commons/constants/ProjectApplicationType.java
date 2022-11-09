package io.metersphere.commons.constants;

/**
 * 项目配置
 */
public enum ProjectApplicationType {
    /**
     * 测试计划报告分享链接有效期
     */
    TRACK_SHARE_REPORT_TIME,
    /**
     * 性能测试报告分享链接有效期
     */
    PERFORMANCE_SHARE_REPORT_TIME,
    /**
     * 是否开启测试用例自定义ID
     */
    CASE_CUSTOM_NUM,
    /**
     * 场景自定义ID
     */
    SCENARIO_CUSTOM_NUM,
    /**
     * 接口定义快捷添加按钮
     */
    API_QUICK_MENU,
    /**
     * 接口定义URL可重复
     */
    URL_REPEATABLE,
    /**
     * 公共用例库
     */
    CASE_PUBLIC,
    /**
     * MOCK使用端口
     */
    MOCK_TCP_PORT,
    /**
     * MOCk是否开启
     */
    MOCK_TCP_OPEN,
    /**
     * 是否开启定时清理测试计划报告
     */
    CLEAN_TRACK_REPORT,
    /**
     * 清理时间配置 (Y|M|D)
     */
    CLEAN_TRACK_REPORT_EXPR,
    /**
     * 是否开启定时清理接口测试报告
     */
    CLEAN_API_REPORT,
    /**
     * 清理时间配置 (Y|M|D)
     */
    CLEAN_API_REPORT_EXPR,
    /**
     * 是否开启定时清理接口测试报告
     */
    CLEAN_LOAD_REPORT,
    /**
     * 清理时间配置 (Y|M|D)
     */
    CLEAN_LOAD_REPORT_EXPR,
    /**
     * 是否开启定时清理UI测试报告
     */
    CLEAN_UI_REPORT,
    /**
     * 清理时间配置 (Y|M|D)
     */
    CLEAN_UI_REPORT_EXPR,
    /**
     * 接口分享链接有效期
     */
    API_SHARE_REPORT_TIME,
    /**
     * UI报告分享链接有效期
     */
    UI_SHARE_REPORT_TIME,

    /**
     * 我的工作台-是否开启待更新时间限制
     */
    OPEN_UPDATE_TIME,

    /**
     * 我的工作台-待更新时间限制
     */
    OPEN_UPDATE_RULE_TIME,

    /**
     * 我的工作台-触发待更新规则
     */
    TRIGGER_UPDATE,
    /**
     * 我的工作台-开启待更新规则
     */
    OPEN_UPDATE_RULE,
    /**
     * 开启接口测试默认资源池
     */
    POOL_ENABLE,
    /**
     * 资源池ID
     */
    RESOURCE_POOL_ID,
}
