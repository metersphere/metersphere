package io.metersphere.plugin.api.dto;

import io.metersphere.plugin.api.constants.JMeterStepType;
import lombok.Data;

@Data
public class ScriptDTO {
    /**
     * 脚本唯一标识
     */
    private String id;
    /**
     * 显示名称
     */
    private String name;

    /**
     * 对应JMeter的步骤分类
     * 如：请求的基类
     */
    private JMeterStepType stepType;

    /**
     * 协议分类 如：HTTP，TCP，JDBC，JMS，WEBSOCKET等
     */
    private String protocol;

    /**
     * 表单基本参数
     * 这个可选/不填走默认值
     */
    private String formOption;

    /**
     * 表单脚本内容
     */
    private String formScript;
}
