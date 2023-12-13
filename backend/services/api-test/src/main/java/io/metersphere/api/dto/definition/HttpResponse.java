package io.metersphere.api.dto.definition;

import io.metersphere.api.dto.request.http.Header;
import io.metersphere.api.dto.request.http.body.Body;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author: LAN
 * @date: 2023/11/21 18:12
 * @version: 1.0
 */
@Data
public class HttpResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "响应名称")
    private String name;

    /**
     * 请求头
     */
    @Schema(description = "响应请求头")
    private List<Header> headers;

    /**
     * 请求体
     */
    @Schema(description = "响应请求体")
    private Body body;

    /**
     * 请求方法
     */
    @Schema(description = "响应请求方法")
    private String statusCode;

    /**
     * 默认标识
     */
    @Schema(description = "默认响应标识")
    private Boolean defaultFlag;
}
