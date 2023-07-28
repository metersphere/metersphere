package io.metersphere.plugin.sdk.api;

import java.util.List;

/**
 * 插件的基本信息
 *
 * @author jianxing.chen
 */
public interface MsPlugin {

    /**
     * @return 返回该插件是否是开源的，默认是
     */
    boolean isXpack();

    /**
     * @return 返回插件的类型
     * 目前支持接口插件和平台(API、PLATFORM)
     */
    String getType();

    /**
     * @return 返回插件的关键字，例如 Jira
     */
    String getKey();

    /**
     * @return 返回插件的名称
     * 默认返回 key
     */
    String getName();

    /**
     * @return 返回插件的ID
     * 默认是 key + 版本号
     */
    String getPluginId();

    /**
     * @return 返回前端渲染需要的数据
     * 默认会返回 resources下的 script 下的 json 文件
     */
    List<String> getFrontendScript();

    /**
     * @return 返回插件的版本
     */
    String getVersion();
}
