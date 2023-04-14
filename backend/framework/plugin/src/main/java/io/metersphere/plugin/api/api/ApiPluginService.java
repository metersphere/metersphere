package io.metersphere.plugin.api.api;

import io.metersphere.plugin.api.dto.ApiPluginDTO;

public abstract class ApiPluginService {

    /**
     * 初始化页面脚本
     * 上传JAR到MeterSphere平台后会自动调用
     */
    public abstract ApiPluginDTO init();

    /**
     * 自定义方法，用于在自定义插件中初始化数据
     * @return 返回要处理的JSON数据
     */
    public abstract String custom(String... args);
}
