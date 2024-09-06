package io.metersphere.request;

import com.google.common.base.CaseFormat;
import io.metersphere.sdk.dto.BaseCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @author wx
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AssociateBugPageRequest extends BaseCondition {

    @Schema(description = "用例id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String caseId;

    @Schema(description = "关联测试计划用例ID 如果是测试计划关联用例的缺陷，这个参数必填", requiredMode = Schema.RequiredMode.REQUIRED)
    private String testPlanCaseId;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.project_id.not_blank}")
    private String projectId;


    @Min(value = 1, message = "当前页码必须大于0")
    @Schema(description = "当前页码")
    private int current;

    @Min(value = 5, message = "每页显示条数必须不小于5")
    @Max(value = 500, message = "每页显示条数不能大于500")
    @Schema(description = "每页显示条数")
    private int pageSize;

    @Schema(description = "排序字段（model中的字段 : asc/desc）")
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
}
