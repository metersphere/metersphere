package io.metersphere.sdk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.Map;

@Data
public class BasePageRequest {
    @Schema(title = "关键字")
    private String keyword;

    @Min(value = 1, message = "当前页码必须大于0")
    @Schema(title = "当前页码")
    private int current;

    @Min(value = 5, message = "每页显示条数必须不小于5")
    @Schema(title = "每页显示条数")
    private int pageSize;

    @Schema(title = "排序字段（字段:asc/desc）")
    private Map<String, String> sort;
    
    @Schema(title = "过滤字段")
    private Map<String, Object> filter;
}
