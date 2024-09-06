package io.metersphere.system.dto.request;

import io.metersphere.sdk.dto.CombineSearch;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @Author: jianxing
 * @CreateTime: 2024-09-02  10:15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserViewUpdateRequest extends CombineSearch {
    @Schema(description = "视图ID")
    @NotBlank
    private String id;
    @Schema(description = "视图名称")
    @NotBlank
    private String name;
}
