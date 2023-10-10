package io.metersphere.plugin.sdk.spi;

import org.pf4j.Plugin;

/**
 * 插件的基本信息
 *
 * @author jianxing.chen
 */
public abstract class MsPlugin extends Plugin {

    /**
     * @return 返回该插件是否是开源的，默认是
     */
    abstract public boolean isXpack();

    /**
     * @return 返回插件的名称
     * 默认返回 key
     */
    abstract public String getName();

    /**
     * @return 返回该加载前端配置文件的目录，默认是 script
     * 可以重写定制
     */
    abstract public String getScriptDir();
}
