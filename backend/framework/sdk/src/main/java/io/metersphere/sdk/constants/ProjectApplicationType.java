package io.metersphere.sdk.constants;

import java.util.Arrays;

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
    public enum UI {
        UI_CLEAN_REPORT,
        UI_SHARE_REPORT,
        UI_RESOURCE_POOL_ID,
    }


    //性能测试
    public enum LOAD_TEST {
        LOAD_TEST_CLEAN_REPORT,
        LOAD_TEST_SHARE_REPORT,
        LOAD_TEST_SCRIPT_REVIEWER_ENABLE,
        LOAD_TEST_SCRIPT_REVIEWER_ID,
        LOAD_TEST_RESOURCE_POOL_ID
    }


    //接口测试
    public enum API {
        API_URL_REPEATABLE,
        API_CLEAN_REPORT,
        API_SHARE_REPORT,
        API_RESOURCE_POOL_ID,
        API_SCRIPT_REVIEWER_ENABLE,
        API_SCRIPT_REVIEWER_ID,
        API_ERROR_REPORT_RULE,
        API_SYNC_CASE
    }


    //用例管理
    public enum CASE {
        CASE_PUBLIC,
        CASE_RE_REVIEW,
    }

    //用例管理-关联需求
    public enum CASE_RELATED_CONFIG {
        CASE_RELATED,
        CASE_ENABLE,
    }

    public enum PLATFORM_DEMAND_CONFIG {
        DEMAND_PLATFORM_CONFIG
    }

    //缺陷管理
    public enum BUG {
        BUG_SYNC
    }

    public enum PLATFORM_BUG_CONFIG {
        BUG_PLATFORM_CONFIG
    }

    //缺陷管理-同步配置项
    public enum BUG_SYNC_CONFIG {
        CRON_EXPRESSION,
        SYNC_ENABLE,
        MECHANISM,
    }

    // 版本管理-配置项
    public enum VERSION {
        VERSION_ENABLE
    }

    /**
     * 记录项目中配置的默认模板
     */
    public enum DEFAULT_TEMPLATE {
        FUNCTIONAL_DEFAULT_TEMPLATE,
        BUG_DEFAULT_TEMPLATE,
        API_DEFAULT_TEMPLATE,
        UI_DEFAULT_TEMPLATE,
        TEST_PLAN_DEFAULT_TEMPLATE;

        public static DEFAULT_TEMPLATE getByTemplateScene(String scene) {
            return Arrays.stream(DEFAULT_TEMPLATE.values())
                    .filter(e -> e.name().startsWith(scene))
                    .findFirst()
                    .orElse(null);
        }
    }

}
