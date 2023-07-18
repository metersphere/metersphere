package io.metersphere.sdk.dto;

import com.google.common.base.CaseFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

@Data
public class BasePageRequest {
    @Schema(title = "关键字")
    private String keyword;

    @Min(value = 1, message = "当前页码必须大于0")
    @Schema(title = "当前页码")
    private int current;

    @Min(value = 5, message = "每页显示条数必须不小于5")
    @Max(value = 100, message = "每页显示条数不能大于100")
    @Schema(title = "每页显示条数")
    private int pageSize;

    @Schema(title = "排序字段（model中的字段 : asc/desc）")
    private Map<@Valid @Pattern(regexp = "^[A-Za-z]+$") String, @Valid @NotBlank String> sort;

    @Schema(title = "过滤字段")
    private Map<String, List<String>> filter;

    @Schema(title = "高级搜索")
    private Map<String, Object> combine;

    public String getSortString() {
        if (sort == null || sort.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : sort.entrySet()) {
            String column = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, entry.getKey());
            sb.append(column)
                    .append(StringUtils.SPACE)
                    .append(StringUtils.equalsIgnoreCase(entry.getValue(), "DESC") ? "DESC" : "ASC")
                    .append(",");
        }
        return sb.substring(0, sb.length() - 1);
    }
}
