package io.metersphere.plugin.platform.spi;

import io.metersphere.plugin.sdk.spi.AbstractMsPlugin;

public abstract class AbstractPlatformPlugin extends AbstractMsPlugin {
    private static final String DEFAULT_INTEGRATION_SCRIPT_ID = "integration";
    private static final String DEFAULT_PROJECT_SCRIPT_ID = "project";
    private static final String DEFAULT_ACCOUNT_SCRIPT_ID = "account";
    private static final String PROJECT_BUG_SCRIPT_ID = "project_bug";
    private static final String PROJECT_DEMAND_SCRIPT_ID = "project_demand";
    private static final String PROJECT_BUG_TEMPLATE_INJECT_FIELD = "inject_field";

    /**
     * 返回插件的描述信息
     * @return
     */
    public abstract String getDescription();

    /**
     * 返回插件的logo路径，比如：/static/jira.png
     * @return
     */
    public abstract String getLogo();

    /**
     * 返回服务集成脚本的ID，默认为 integration
     * @return
     */
    public String getIntegrationScriptId() {
        return DEFAULT_INTEGRATION_SCRIPT_ID;
    }

    /**
     * 返回项目配置脚本的ID，默认为 project
     * @return
     */
    public String getProjectScriptId() {
        return DEFAULT_PROJECT_SCRIPT_ID;
    }

    /**
     * 返回个人账号脚本的ID，默认为 account
     * @return
     */
    public String getAccountScriptId() {
        return DEFAULT_ACCOUNT_SCRIPT_ID;
    }

    public String getProjectBugScriptId() {
        return PROJECT_BUG_SCRIPT_ID;
    }

    public String getProjectDemandScriptId() {
        return PROJECT_DEMAND_SCRIPT_ID;
    }

    public String getProjectBugTemplateInjectField() {
        return PROJECT_BUG_TEMPLATE_INJECT_FIELD;
    }
}
