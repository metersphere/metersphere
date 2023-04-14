package io.metersphere.plugin.api.dto;

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
     * 这个参数非常重要，对应继承 MsTestElement的类全名
     * 如：io.xx.MsThriftSample
     */
    private String clazzName;

    /**
     * 插件实现的jmeter基类
     * 如：请求的基类
     */
    private String jmeterClazz;

    /**
     * 表单基本参数
     * 这个可选/不填走默认值
     */
    private String formOption;

    /**
     * 表单脚本内容
     */
    private String formScript;

    public ScriptDTO() {

    }

    public ScriptDTO(String id, String name, String clazzName, String script) {
        this.id = id;
        this.name = name;
        this.clazzName = clazzName;
        this.formScript = script;
    }

    public ScriptDTO(String id, String name, String clazzName, String script, String option) {
        this.id = id;
        this.name = name;
        this.clazzName = clazzName;
        this.formScript = script;
        this.formOption = option;
    }

    public ScriptDTO(String id, String name, String clazzName, String jmeterClazz, String script, String option) {
        this.id = id;
        this.name = name;
        this.clazzName = clazzName;
        this.jmeterClazz = jmeterClazz;
        this.formScript = script;
        this.formOption = option;
    }
}
