package io.metersphere.sdk.dto.api.result;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class MsRegexDTO implements Serializable {
    // 项目ID
    private String projectId;

    // 误报名称
    private String name;

    // 匹配类型/文本内容
    private String type;

    // 响应内容类型/header/data/body
    private String respType;

    // 操作类型/大于/等于/小于
    private String relation;

    // 表达式
    private String expression;

    private Boolean pass;

    @Serial
    private static final long serialVersionUID = 1L;

}
