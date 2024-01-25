package io.metersphere.plugin.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author wx
 */
@Setter
@Getter
public class ApiPluginOptionsRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 方法所需的查询参数
     * 例如：
     * 查询的过滤条件
     * 级联的情况，需要表单的其他参数值等
     */
    private Object queryParam;
    /**
     * 需要调用的插件方法
     */
    private String optionMethod;
}
