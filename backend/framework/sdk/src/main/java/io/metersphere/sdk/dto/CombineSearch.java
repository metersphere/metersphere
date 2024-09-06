package io.metersphere.sdk.dto;

import io.metersphere.sdk.valid.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2024-08-28  17:31
 */
@Data
public class CombineSearch {
    @Schema(description = "匹配模式 所有/任一", allowableValues = {"AND", "OR"})
    @EnumValue(enumClass = SearchMode.class)
    private String searchMode = SearchMode.AND.name();

    @Schema(description = "筛选条件")
    @Valid
    private List<CombineCondition> conditions;

    public String getSearchMode() {
        return StringUtils.isBlank(searchMode) ? SearchMode.AND.name() : searchMode;
    }

    public enum SearchMode {
        /**
         * 所有
         */
        AND,
        /**
         * 任一
         */
        OR
    }
}
