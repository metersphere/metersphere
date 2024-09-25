package io.metersphere.sdk.constants;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-17  14:04
 */
public class DefaultRepositoryDir {
    /**
     * 系统级别资源的根目录
     */
    private static final String SYSTEM_ROOT_DIR = "system";
    /**
     * 组织级别资源的项目目录
     * organization/{organizationId}
     */
    private static final String ORGANIZATION_DIR = "organization/%s";
    /**
     * 项目级别资源的项目目录
     * project/{projectId}
     */
    private static final String PROJECT_DIR = "project/%s";

    /*------ start: 系统下资源目录 ------*/

    /**
     * 插件存储目录
     */
    private static final String SYSTEM_PLUGIN_DIR = SYSTEM_ROOT_DIR + "/plugin";
    /**
     * 系统临时文件的存放目录
     * system/temp
     * 会定时清理
     */
    private static final String SYSTEM_TEMP_DIR = SYSTEM_ROOT_DIR + "/temp";
    private static final String EXPORT_EXCEL_TEMP_DIR = SYSTEM_ROOT_DIR + "/export/excel";
    private static final String EXPORT_API_TEMP_DIR = SYSTEM_ROOT_DIR + "/export/api";

    /*------ end: 系统下资源目录 --------*/

    /*------ start: 组织下资源目录 ------*/

    /**
     * 组织模板富文本图片存储目录
     * 这里省略模板ID，模板的图片不处理删除的情况
     * 因为用例会引用图片，模板删除后图片也需要能访问
     */
    private static final String ORGANIZATION_TEMPLATE_IMG_DIR = ORGANIZATION_DIR + "/template-img";
    /**
     * 组织模板压缩图片存储目录
     * 这里省略模板ID，模板的图片不处理删除的情况
     * 因为用例会引用图片，模板删除后图片也需要能访问
     */
    private static final String ORGANIZATION_TEMPLATE_IMG_PREVIEW_DIR = ORGANIZATION_DIR + "/template-img/preview";

    /*------ end: 组织下资源目录 --------*/

    /*------ start: 项目下资源目录 --------*/

    /**
     * 项目模板富文本图片存储目录
     * 这里省略模板ID，模板的图片不处理删除的情况
     * 因为用例会引用图片，模板删除后图片也需要能访问
     */
    private static final String PROJECT_TEMPLATE_IMG_DIR = PROJECT_DIR + "/template-img";
    /**
     * 项目模板压缩图片存储目录
     * 这里省略模板ID，模板的图片不处理删除的情况
     * 因为用例会引用图片，模板删除后图片也需要能访问
     */
    private static final String PROJECT_TEMPLATE_IMG_PREVIEW_DIR = PROJECT_DIR + "/template-img/preview";

    /**
     * 接口用例相关文件的存储目录
     * project/{projectId}/apiCase/{apiCaseId}
     */
    private static final String PROJECT_API_CASE_DIR = PROJECT_DIR + "/api-case/%s";
    private static final String PROJECT_API_MOCK_DIR = PROJECT_DIR + "/api-mock/%s";
    private static final String PROJECT_ENV_SSL_DIR = PROJECT_DIR + "/environment/%s";
    private static final String PROJECT_FUNCTIONAL_CASE_DIR = PROJECT_DIR + "/functional-case/%s";
    private static final String PROJECT_FUNCTIONAL_CASE_PREVIEW_DIR = PROJECT_DIR + "/functional-case/preview/%s";
    private static final String PROJECT_BUG_PREVIEW_DIR = PROJECT_DIR + "/bug/preview/%s";
    private static final String PROJECT_FILE_MANAGEMENT_DIR = PROJECT_DIR + "/file-management";
    private static final String PROJECT_FILE_MANAGEMENT_PREVIEW_DIR = PROJECT_DIR + "/file-management/preview";
    /**
     * 接口调试相关文件的存储目录
     * project/{projectId}/api-debug/{apiDebugId}
     */
    private static final String PROJECT_API_DEBUG_DIR = PROJECT_DIR + "/api-debug/%s";
    private static final String PROJECT_API_SCENARIO_DIR = PROJECT_DIR + "/api-scenario/%s";
    private static final String PROJECT_API_SCENARIO_STEP_DIR = PROJECT_API_SCENARIO_DIR + "/step/%s";
    private static final String PROJECT_BUG_DIR = PROJECT_DIR + "/bug/%s";
    private static final String PROJECT_PLAN_REPORT_DIR = PROJECT_DIR + "/plan-report/%s";

    /**
     * 接口定义相关文件的存储目录
     * project/{projectId}/api-definition/{apiId}
     */
    private static final String PROJECT_API_DEFINITION_DIR = PROJECT_DIR + "/api-definition/%s";

    /*------ end: 项目下资源目录 --------*/


    public static String getApiCaseDir(String projectId, String apiCaseId) {
        return String.format(PROJECT_API_CASE_DIR, projectId, apiCaseId);
    }

    public static String getApiMockDir(String projectId, String apiMockId) {
        return String.format(PROJECT_API_MOCK_DIR, projectId, apiMockId);
    }

    public static String getEnvSslDir(String projectId, String envId) {
        return String.format(PROJECT_ENV_SSL_DIR, projectId, envId);
    }

    public static String getPluginDir() {
        return SYSTEM_PLUGIN_DIR;
    }

    public static String getSystemRootDir() {
        return SYSTEM_ROOT_DIR;
    }

    public static String getFunctionalCaseDir(String projectId, String functionalCaseId) {
        return String.format(PROJECT_FUNCTIONAL_CASE_DIR, projectId, functionalCaseId);
    }

    public static String getFunctionalCasePreviewDir(String projectId, String functionalCaseId) {
        return String.format(PROJECT_FUNCTIONAL_CASE_PREVIEW_DIR, projectId, functionalCaseId);
    }

    public static String getBugPreviewDir(String projectId, String bugId) {
        return String.format(PROJECT_BUG_PREVIEW_DIR, projectId, bugId);
    }

    public static String getFileManagementDir(String projectId) {
        return String.format(PROJECT_FILE_MANAGEMENT_DIR, projectId);
    }

    public static String getFileManagementPreviewDir(String projectId) {
        return String.format(PROJECT_FILE_MANAGEMENT_PREVIEW_DIR, projectId);
    }

    public static String getBugDir(String projectId, String bugId) {
        return String.format(PROJECT_BUG_DIR, projectId, bugId);
    }

    public static String getPlanReportDir(String projectId, String reportId) {
        return String.format(PROJECT_PLAN_REPORT_DIR, projectId, reportId);
    }

    public static String getApiDebugDir(String projectId, String apiDebugId) {
        return String.format(PROJECT_API_DEBUG_DIR, projectId, apiDebugId);
    }

    public static String getApiDefinitionDir(String projectId, String apiId) {
        return String.format(PROJECT_API_DEFINITION_DIR, projectId, apiId);
    }

    public static String getSystemTempDir() {
        return SYSTEM_TEMP_DIR;
    }

    public static String getExportExcelTempDir() {
        return EXPORT_EXCEL_TEMP_DIR;
    }

    public static String getExportApiTempDir() {
        return EXPORT_API_TEMP_DIR;
    }
    public static String getSystemTempCompressDir() {
        return SYSTEM_TEMP_DIR + "/compress";
    }

    public static String getApiScenarioDir(String projectId, String apiScenarioId) {
        return String.format(PROJECT_API_SCENARIO_DIR, projectId, apiScenarioId);
    }

    public static String getApiScenarioStepDir(String projectId, String apiScenarioId, String stepId) {
        return String.format(PROJECT_API_SCENARIO_STEP_DIR, projectId, apiScenarioId, stepId);
    }

    public static String getProjectDir(String projectId) {
        return String.format(PROJECT_DIR, projectId);
    }

    public static String getOrgTemplateImgDir(String orgId) {
        return String.format(ORGANIZATION_TEMPLATE_IMG_DIR, orgId);
    }

    public static String getOrgTemplateImgPreviewDir(String orgId) {
        return String.format(ORGANIZATION_TEMPLATE_IMG_PREVIEW_DIR, orgId);
    }

    public static String getProjectTemplateImgDir(String projectId) {
        return String.format(PROJECT_TEMPLATE_IMG_DIR, projectId);
    }

    public static String getProjectTemplateImgPreviewDir(String projectId) {
        return String.format(PROJECT_TEMPLATE_IMG_PREVIEW_DIR, projectId);
    }
}
