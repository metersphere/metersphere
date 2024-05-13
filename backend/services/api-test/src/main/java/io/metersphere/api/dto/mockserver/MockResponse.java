package io.metersphere.api.dto.mockserver;

import io.metersphere.api.dto.definition.ResponseBody;
import io.metersphere.api.dto.request.http.MsHeader;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class MockResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "响应码")
    private int statusCode;

    @Schema(description = "响应请求头")
    private List<MsHeader> headers;

    @Schema(description = "是否使用api响应体")
    private boolean useApiResponse;

    @Schema(description = "接口响应ID（useApiResponse为true时使用）")
    private String apiResponseId;

    @Schema(description = "响应请求体")
    private ResponseBody body;

    @Schema(description = "响应延迟时间（毫秒）")
    private Long delay;
}
