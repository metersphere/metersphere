package io.metersphere.system.dto;

import io.metersphere.system.domain.UserView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2024-09-02  10:15
 */
@Data
public class UserViewListGroupedDTO {
    @Schema(description = "系统视图")
    private List<UserView> internalViews;
    @Schema(description = "自定义视图")
    private List<UserView> customViews;
}
