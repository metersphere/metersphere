package io.metersphere.sdk.dto.environment;

import io.metersphere.sdk.dto.environment.variables.CommonVariables;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class GlobalParams implements Serializable {

    @Schema(description = "请求头")
    private List<KeyValue> headers;
    @Schema(description = "全局变量")
    private List<CommonVariables> commonVariables;

    @Serial
    private static final long serialVersionUID = 1L;
}
