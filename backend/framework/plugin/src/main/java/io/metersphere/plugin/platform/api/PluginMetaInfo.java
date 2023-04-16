package io.metersphere.plugin.platform.api;

/**
 * 插件的基本信息
 * @author jianxing.chen
 */
public interface PluginMetaInfo {

    /**
     * 该插件是否是开源的
     * 默认是
     * @return
     */
    boolean isXpack();

    /**
     * 返回插件的关键字
     * @return
     */
    String getKey();

    /**
     * 返回插件的名称
     * @return
     */
    String getLabel();

    /**
     * 返回前端渲染需要的数据
     * 默认会返回 resources下的 json/frontend.json
     * @return
     */
    String getFrontendMetaData();

    /**
     * 返回插件的版本
     * @return
     */
    String getVersion();

    /**
     * 插件是否支持第三方模板
     * @return
     */
    boolean isThirdPartTemplateSupport();
}
