package io.metersphere.sdk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class OrgIdNameDTO {

    @Schema(title = "关联的组织id")
    private String id;

    @Schema(title = "关联的组织名称")
    private String name;

}
