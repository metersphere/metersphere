package io.metersphere.sdk.constants;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-17  13:54
 */
public class LocalRepositoryDir {
    /**
     * 本地存储文件的根目录
     */
    private static final String ROOT_DIR = "/tmp/metersphere";
    /**
     * 系统级别资源的根目录
     */
    private static final String SYSTEM_ROOT_DIR = ROOT_DIR + "/" + "system";
    /**
     * 组织级别资源的项目目录
     * organization/{organizationId}
     */
    private static final String ORGANIZATION_DIR = ROOT_DIR + "/organization/%s";
    /**
     * 项目级别资源的项目目录
     * project/{projectId}
     */
    private static final String PROJECT_DIR = ROOT_DIR + "/project/%s";

    /*------ start: 系统下资源目录 ------*/
    /**
     * 插件存储目录
     */
    private static final String SYSTEM_PLUGIN_DIR = SYSTEM_ROOT_DIR + "/plugin";
    private static final String SYSTEM_BODY_ENVIRONMENT_TEM_DIR = SYSTEM_ROOT_DIR + "/body/environment/tmp";
    /*------ end: 系统下资源目录 --------*/

    public static String getPluginDir() {
        return SYSTEM_PLUGIN_DIR;
    }

    public static String getBodyEnvironmentTmpDir() {
        return SYSTEM_BODY_ENVIRONMENT_TEM_DIR;
    }
}
