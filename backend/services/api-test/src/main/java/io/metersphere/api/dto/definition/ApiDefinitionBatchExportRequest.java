package io.metersphere.api.dto.definition;

import com.google.common.base.CaseFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lan
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ApiDefinitionBatchExportRequest extends ApiDefinitionBatchRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "文件id")
    @NotBlank
    private String fileId;

    @Schema(description = "是否同步导出接口用例")
    private boolean exportApiCase;

    @Schema(description = "是否同步导出接口Mock")
    private boolean exportApiMock;

    @Schema(description = "排序字段（model中的字段 : asc/desc）")
    private Map<@Valid @Pattern(regexp = "^[A-Za-z]+$") String, @Valid @NotBlank String> sort;

    public String getSortString() {
        if (MapUtils.isEmpty(sort)) {
            return null;
        }

        String orderStr = sort.entrySet().stream()
                .map(entry -> {
                    String column = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, entry.getKey());
                    String direction = StringUtils.equalsIgnoreCase(entry.getValue(), "DESC") ? "DESC" : "ASC";
                    return column + StringUtils.SPACE + direction;
                })
                .collect(Collectors.joining(","));

        if (checkSqlInjection(orderStr)) {
            throw new IllegalArgumentException("排序字段存在SQL注入风险");
        }

        return orderStr;
    }

    /**
     * 返回 true 表示存在 SQL 注入风险
     */
    public static boolean checkSqlInjection(String script) {
        if (StringUtils.isEmpty(script)) {
            return false;
        }

        // 检测危险SQL模式
        java.util.regex.Pattern dangerousPattern = java.util.regex.Pattern.compile(
                "(;|--|#|'|\"|/\\*|\\*/|\\b(select|insert|update|delete|drop|alter|truncate|exec|union|xp_)\\b)",
                java.util.regex.Pattern.CASE_INSENSITIVE);

        return dangerousPattern.matcher(script).find();
    }

}
