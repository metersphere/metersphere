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
    /**
     * 系统临时文件的存放目录
     * system/temp
     * 会定时清理
     */
    private static final String SYSTEM_TEMP_DIR = SYSTEM_ROOT_DIR + "/temp";
    /**
     * 系统缓存文件存放目录
     * 目前仅执行机缓存执行文件使用到
     * system/cache
     */
    private static final String SYSTEM_CACHE_DIR = SYSTEM_ROOT_DIR + "/cache";

    /**
     * 缺陷的文件临时目录
     */
    private static final String BUG_TMP_DIR = ROOT_DIR + "/bug";
    /*------ end: 系统下资源目录 --------*/

    public static String getPluginDir() {
        return SYSTEM_PLUGIN_DIR;
    }

    public static String getSystemTempDir() {
        return SYSTEM_TEMP_DIR;
    }
    public static String getSystemCacheDir() {
        return SYSTEM_CACHE_DIR;
    }

    public static String getFuncJarDir() {
        return ROOT_DIR + "/api/func-jar";
    }

    public static String getBugTmpDir() {
        return BUG_TMP_DIR;
    }
}
