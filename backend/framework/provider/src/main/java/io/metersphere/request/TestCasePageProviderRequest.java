package io.metersphere.request;

import com.google.common.base.CaseFormat;
import io.metersphere.sdk.dto.BaseCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author guoyuqi
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TestCasePageProviderRequest extends BaseCondition implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Min(value = 1, message = "当前页码必须大于0")
    @Schema(description = "当前页码")
    private int current;

    @Min(value = 5, message = "每页显示条数必须不小于5")
    @Max(value = 500, message = "每页显示条数不能大于500")
    @Schema(description = "每页显示条数")
    private int pageSize;

    @Schema(description = "排序字段（model中的字段 : asc/desc）")
    private Map<@Valid @Pattern(regexp = "^[A-Za-z]+$") String, @Valid @NotBlank String> sort;

    @Schema(description = "接口pk")
    private String apiDefinitionId;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String projectId;

    @Schema(description = "接口协议", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> protocols = new ArrayList<>();

    @Schema(description = "模块ID")
    private List<@NotBlank String> moduleIds;

    @Schema(description = "版本fk(只在关联接口的时候用)")
    private String versionId;

    @Schema(description = "版本来源")
    private String refId;

    @Schema(description = "关联关系表里主ID eg:功能用例关联接口用例时为功能用例id, 缺陷关联用例时为缺陷ID")
    @NotBlank(message = "{relate_source_id_not_blank}")
    @Size(min = 1, max = 50, message = "{relate_source_id_length_range}")
    private String sourceId;

    @Schema(description = "关联类型(FUNCTIONAL, API, SCENARIO, UI, PERFORMANCE)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{relate_source_type_not_blank}")
    private String sourceType;


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
