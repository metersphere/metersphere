package io.metersphere.bug.dto.request;

import com.google.common.base.CaseFormat;
import io.metersphere.bug.dto.BugExportColumn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class BugExportRequest extends BugBatchRequest {

    @Schema(description = "导出的字段", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{bug.system_columns.not_empty}")
    private List<BugExportColumn> exportColumns;

    @Schema(description =  "排序字段（model中的字段 : asc/desc）")
    private Map<@Valid @Pattern(regexp = "^[A-Za-z]+$") String, @Valid @NotBlank String> exportSort;

    public String getSortString() {
        if (MapUtils.isEmpty(exportSort)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : exportSort.entrySet()) {
            String column = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, entry.getKey());
            sb.append(column)
                    .append(StringUtils.SPACE)
                    .append(StringUtils.equalsIgnoreCase(entry.getValue(), "desc") ? "desc" : "asc")
                    .append(",");
        }
        return sb.substring(0, sb.length() - 1);
    }
}
