package io.metersphere.sdk.constants;

/**
 * 应用设置 -相关配置
 */
public class ProjectApplicationType {

    //工作台
    public enum WORKSTATION {
        WORKSTATION_SYNC_RULE
    }

    //测试计划
    public enum TEST_PLAN {
        TEST_PLAN_CLEAN_REPORT,
        TEST_PLAN_SHARE_REPORT

    }



    //UI测试
    public enum UI{
        UI_CLEAN_REPORT,
        UI_SHARE_REPORT,
        UI_RESOURCE_POOL,
    }



    //性能测试
    public enum PERFORMANCE_TEST{
       PERFORMANCE_TEST_CLEAN_REPORT,
       PERFORMANCE_TEST_SHARE_REPORT,
       PERFORMANCE_TEST_SCRIPT_REVIEWER
    }


    //接口测试
    public enum API{
        API_URL_REPEATABLE,
        API_CLEAN_REPORT,
        API_SHARE_REPORT,
        API_RESOURCE_POOL,
        API_SCRIPT_REVIEWER,
        API_ERROR_REPORT_RULE,
        API_SYNC_CASE
    }


    //用例管理
    public enum CASE{
        CASE_PUBLIC,
        CASE_RE_REVIEW,
    }

    //用例管理-关联需求
    public enum CASE_RELATED_CONFIG{
        CASE_RELATED,
        CASE_ENABLE,
    }


    //缺陷管理
    public enum ISSUE{
        ISSUE_SYNC
    }


    //缺陷管理-同步配置项
    public enum ISSUE_SYNC_CONFIG{
        CRON_EXPRESSION,
        SYNC_ENABLE,
        MECHANISM,
    }

}
