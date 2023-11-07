package io.metersphere.sdk.dto.api.request.http;

import lombok.Data;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-07  10:47
 */
@Data
public class MsHTTPConfig {
    private Long connectTimeout;
    private Long responseTimeout;
    private String certificateAlias;
    private Boolean followRedirects = true;
    private Boolean autoRedirects = false;
}
