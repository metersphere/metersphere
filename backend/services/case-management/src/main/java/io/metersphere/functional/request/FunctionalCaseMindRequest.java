package io.metersphere.functional.request;

import com.google.common.base.CaseFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@Data
public class FunctionalCaseMindRequest {
    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.project_id.not_blank}")
    private String projectId;

    @Schema(description = "模块ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String moduleId;

    @Min(value = 1, message = "当前页码必须大于0")
    @Schema(description =  "当前页码")
    private int current;

    @Schema(description =  "排序字段（model中的字段 : asc/desc）")
    private Map<@Valid @Pattern(regexp = "^[A-Za-z]+$") String, @Valid @NotBlank String> sort;


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

    public String getSortString(String defaultColumn, String tableAliseName) {
        if (sort == null || sort.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : sort.entrySet()) {
            String column = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, entry.getKey());
            sb.append(tableAliseName)
                    .append(".")
                    .append(column)
                    .append(StringUtils.SPACE)
                    .append(StringUtils.equalsIgnoreCase(entry.getValue(), "DESC") ? "DESC" : "ASC")
                    .append(",")
                    .append(tableAliseName)
                    .append(".")
                    .append(defaultColumn)
                    .append(StringUtils.SPACE)
                    .append(StringUtils.equalsIgnoreCase(entry.getValue(), "DESC") ? "DESC" : "ASC");
            return sb.toString();
        }
        return null;
    }

    public String getSortString(String defaultColumn) {
        if (sort == null || sort.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : sort.entrySet()) {
            String column = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, entry.getKey());
            sb.append(column)
                    .append(StringUtils.SPACE)
                    .append(StringUtils.equalsIgnoreCase(entry.getValue(), "DESC") ? "DESC" : "ASC")
                    .append(",")
                    .append(defaultColumn)
                    .append(StringUtils.SPACE)
                    .append(StringUtils.equalsIgnoreCase(entry.getValue(), "DESC") ? "DESC" : "ASC");
            return sb.toString();
        }
        return null;
    }
}
