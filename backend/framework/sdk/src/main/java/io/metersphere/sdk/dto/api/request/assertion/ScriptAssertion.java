package io.metersphere.sdk.dto.api.request.assertion;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

/**
 * 变量断言
 * @Author: jianxing
 * @CreateTime: 2023-11-22  15:33
 */
@Data
@JsonTypeName("SCRIPT")
public class ScriptAssertion extends MsAssertion {
    /**
     * 脚本描述
     */
    private String description;
    /**
     * 脚本内容
     */
    private String content;
    /**
     * 是否使用功能脚本
     * 公共脚本和手动录入脚本只能二选一
     */
    private Boolean enableCommonScript;
    /**
     * 引用公共脚本的ID
     */
    private String commonScriptId;
}
