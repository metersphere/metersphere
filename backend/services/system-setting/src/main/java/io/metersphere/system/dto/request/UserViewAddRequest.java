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
public class UserViewAddRequest extends CombineSearch {
    @Schema(description = "视图的应用范围，一般为项目ID")
    @NotBlank
    private String scopeId;
    @Schema(description = "视图名称")
    @NotBlank
    private String name;
}
