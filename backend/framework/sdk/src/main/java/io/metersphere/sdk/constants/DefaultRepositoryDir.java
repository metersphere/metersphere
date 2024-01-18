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

    /*------ end: 系统下资源目录 --------*/



    /*------ start: 项目下资源目录 --------*/

    /**
     * 接口用例相关文件的存储目录
     * project/{projectId}/apiCase/{apiCaseId}
     */
    private static final String PROJECT_API_CASE_DIR = PROJECT_DIR + "/api-case/%s";
    private static final String PROJECT_ENV_SSL_DIR = PROJECT_DIR + "/environment/%s";
    private static final String PROJECT_FUNCTIONAL_CASE_DIR = PROJECT_DIR + "/functional-case/%s";
    private static final String PROJECT_FUNCTIONAL_CASE_PREVIEW_DIR = PROJECT_DIR + "/functional-case/preview/%s";
    private static final String PROJECT_FILE_MANAGEMENT_DIR = PROJECT_DIR + "/file-management";
    private static final String PROJECT_FILE_MANAGEMENT_PREVIEW_DIR = PROJECT_DIR + "/file-management/preview";
    /**
     * 接口调试相关文件的存储目录
     * project/{projectId}/api-debug/{apiDebugId}
     */
    private static final String PROJECT_API_DEBUG_DIR = PROJECT_DIR + "/api-debug/%s";
    private static final String PROJECT_API_SCENARIO_DIR = PROJECT_DIR + "/api-scenario/%s";
    private static final String PROJECT_BUG_DIR = PROJECT_DIR + "/bug/%s";

    /**
     * 接口定义相关文件的存储目录
     * project/{projectId}/api-definition/{apiId}
     */
    private static final String PROJECT_API_DEFINITION_DIR = PROJECT_DIR + "/api-definition/%s";

    /*------ end: 项目下资源目录 --------*/


    public static String getApiCaseDir(String projectId, String apiCaseId) {
        return String.format(PROJECT_API_CASE_DIR, projectId, apiCaseId);
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

    public static String getFileManagementDir(String projectId) {
        return String.format(PROJECT_FILE_MANAGEMENT_DIR, projectId);
    }

    public static String getFileManagementPreviewDir(String projectId) {
        return String.format(PROJECT_FILE_MANAGEMENT_PREVIEW_DIR, projectId);
    }

    public static String getBugDir(String projectId, String bugId) {
        return String.format(PROJECT_BUG_DIR, projectId, bugId);
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

    public static String getSystemTempCompressDir() {
        return SYSTEM_TEMP_DIR + "/compress";
    }

    public static String getApiScenarioDir(String projectId, String apiScenarioId) {
        return String.format(PROJECT_API_SCENARIO_DIR, projectId, apiScenarioId);
    }
}
