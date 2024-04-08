package io.metersphere.project.dto.environment.auth;

import lombok.Data;

/**
 * http 认证配置
 * @Author: jianxing
 * @CreateTime: 2023-11-07  11:00
 */
@Data
public abstract class HTTPAuth {
    public abstract boolean isValid();
}
