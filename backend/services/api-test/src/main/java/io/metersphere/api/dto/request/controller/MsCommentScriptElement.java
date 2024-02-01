package io.metersphere.api.dto.request.controller;

import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.api.KeyValueParam;
import lombok.Data;

import java.util.List;


/**
 * 公共脚本组件
 * 主要用于公共脚本测试执行时，生成jmx
 */
@Data
public class MsCommentScriptElement extends AbstractMsTestElement {
    /**
     * 脚本内容
     */
    private String script;
    /**
     * 脚本语言
     * @see io.metersphere.project.constants.ScriptLanguageType
     */
    private String scriptLanguage;
    /**
     * 公共脚本入参
     */
    private List<KeyValueParam> params;
}