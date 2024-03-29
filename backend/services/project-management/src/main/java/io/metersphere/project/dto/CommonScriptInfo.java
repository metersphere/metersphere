package io.metersphere.project.dto;

import io.metersphere.project.api.KeyValueParam;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2024-02-23  13:34
 */
@Data
public class CommonScriptInfo {
    /**
     * 公共脚本ID
     */
    @Size(min = 1, max = 50)
    private String id;
    /**
     * 脚本是否删除
     */
    private Boolean deleted = false;
    /**
     * 公共脚本的名字
     * 页面展示需要
     */
    private String name;
    /**
     * 公共脚本的内容
     * 执行时设置
     */
    private String script;
    /**
     * 脚本语言
     */
    private String scriptLanguage;
    /**
     * 公共脚本入参
     */
    @Valid
    private List<KeyValueParam> params;
}
