package io.metersphere.plan.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Data
public class TestPlanAssociationRequest {
    @NotBlank(message = "{test_plan.id.not_blank}")
    @Schema(description = "测试计划ID")
    private String testPlanId;

    @Schema(description = "选择的id")
    private List<String> selectIds = new ArrayList<>();
    @Schema(description = "不处理的ID")
    private List<String> excludeIds = new ArrayList<>();
    @Schema(description = "选择的模块ID")
    private List<String> selectModuleIds = new ArrayList<>();

    @Schema(description = "排序字段")
    private String orderColumn;
    @Schema(description = "是否是正序")
    private boolean orderByAsc;

    public boolean isEmpty() {
        return CollectionUtils.isEmpty(selectIds) && CollectionUtils.isEmpty(selectModuleIds);
    }

    public String getOrderString() {
        if (StringUtils.isNotBlank(orderColumn)) {
            if (orderByAsc) {
                return orderColumn + " ASC";
            } else {
                return orderColumn + " DESC";
            }
        } else {
            return null;
        }
    }
}
