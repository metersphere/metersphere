package io.metersphere.sdk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
public class OrganizationOptionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(title = "组织ID")
    private String id;

    @Schema(title = "组织编号")
    private Long num;

    @Schema(title = "组织名称")
    private String name;
}
