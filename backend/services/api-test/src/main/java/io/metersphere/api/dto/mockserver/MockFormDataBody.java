package io.metersphere.api.dto.mockserver;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class MockFormDataBody {
    @Schema(description = "是否是全部匹配 （false为任意匹配）")
    private boolean isMatchAll;
    @Schema(description = "匹配规则")
    private List<FormKeyValueInfo> matchRules;
}
