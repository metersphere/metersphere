package io.metersphere.plugin.sdk.spi;

public abstract class AbstractMsPlugin extends MsPlugin {

    private static final String SCRIPT_DIR = "script";

    /**
     * @return 返回默认的前端配置文件的目录
     * 可以重写定制
     */
    @Override
    public String getScriptDir() {
        return SCRIPT_DIR;
    }
}
