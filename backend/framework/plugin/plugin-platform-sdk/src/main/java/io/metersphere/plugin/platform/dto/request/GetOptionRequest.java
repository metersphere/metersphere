package io.metersphere.plugin.platform.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetOptionRequest {
    /**
     * 项目设置的配置项
     */
    private String projectConfig;
    /**
     * 对应插件中获取选项的方法名
     */
    private String optionMethod;
    /**
     * 输入的查询关键字
     */
    private String query;
}
