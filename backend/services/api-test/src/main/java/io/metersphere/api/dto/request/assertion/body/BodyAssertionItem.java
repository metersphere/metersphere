package io.metersphere.api.dto.request.assertion.body;

import lombok.Data;

/**
 * body 断言基类
 * @Author: jianxing
 * @CreateTime: 2023-11-23  14:25
 */
@Data
public abstract class BodyAssertionItem {
    private Boolean enable = true;
}
