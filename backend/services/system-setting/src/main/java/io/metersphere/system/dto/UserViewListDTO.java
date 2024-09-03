package io.metersphere.system.dto;

import io.metersphere.system.domain.UserView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Author: jianxing
 * @CreateTime: 2024-09-02  10:15
 */
@Data
public class UserViewListDTO extends UserView {
    @Schema(description = "内置视图的 key")
    private String internalViewKey;
}
