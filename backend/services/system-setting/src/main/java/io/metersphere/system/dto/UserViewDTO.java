package io.metersphere.system.dto;

import io.metersphere.system.domain.UserView;
import io.metersphere.sdk.dto.CombineCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2024-09-02  10:15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserViewDTO extends UserView {
    @Schema(description = "筛选条件")
    private List<CombineCondition> conditions;
}
