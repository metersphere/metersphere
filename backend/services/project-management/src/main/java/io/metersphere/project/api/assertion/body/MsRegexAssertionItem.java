package io.metersphere.project.api.assertion.body;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * 正则断言
 * @Author: jianxing
 * @CreateTime: 2023-11-23  14:03
 */
@Data
public class MsRegexAssertionItem extends MsBodyAssertionItem {
    /**
     * 表达式
     */
    private String expression;

    @JsonIgnore
    public boolean isValid() {
        return StringUtils.isNotBlank(expression) && Boolean.TRUE.equals(this.getEnable());
    }
}