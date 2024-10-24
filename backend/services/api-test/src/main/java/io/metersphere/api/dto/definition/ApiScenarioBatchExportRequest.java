package io.metersphere.api.dto.definition;

import com.google.common.base.CaseFormat;
import io.metersphere.api.dto.scenario.ApiScenarioBatchRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * @author lan
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ApiScenarioBatchExportRequest extends ApiScenarioBatchRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "文件id")
    @NotBlank
    private String fileId;

    @Schema(description = "排序字段（model中的字段 : asc/desc）")
    private Map<@Valid @Pattern(regexp = "^[A-Za-z]+$") String, @Valid @NotBlank String> sort;

    private boolean exportAllRelatedData;
    
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
