package io.metersphere.system.dto.sdk;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class BaseCondition {
    @Schema(description =  "关键字")
    private String keyword;

    @Schema(description = "匹配模式 所有/任一", allowableValues = {"AND", "OR"})
    private String searchMode = "AND";

    @Schema(description =  "过滤字段")
    private Map<String, List<String>> filter;

    @Schema(description =  "高级搜索")
    private Map<String, Object> combine;
}
