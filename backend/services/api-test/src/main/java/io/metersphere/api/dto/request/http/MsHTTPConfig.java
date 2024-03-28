package io.metersphere.api.dto.request.http;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * http 的其他配置项
 *
 * @Author: jianxing
 * @CreateTime: 2023-11-07  10:47
 */
@Data
public class MsHTTPConfig {
    /**
     * 连接超时
     */
    private Long connectTimeout = 60000L;
    /**
     * 响应超时
     */
    private Long responseTimeout = 60000L;
    /**
     * 证书别名
     */
    @Size(max = 200)
    private String certificateAlias;
    /**
     * 是否跟随重定向
     * 默认 true
     */
    private Boolean followRedirects = true;
    /**
     * 是否自动重定向
     * 默认 false
     */
    private Boolean autoRedirects = false;
}
