package io.metersphere.api.dto.definition;

import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.dto.api.request.http.Header;
import io.metersphere.sdk.dto.api.request.http.body.Body;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author: LAN
 * @date: 2023/11/21 18:12
 * @version: 1.0
 */
@Data
public class HttpResponse {

    @Schema(description = "响应编号")
    private Integer id;

    @Schema(description = "响应类型")
    private String type = ModuleConstants.NODE_PROTOCOL_HTTP;

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
