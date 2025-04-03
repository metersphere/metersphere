package io.metersphere.system.dto.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ModelSourceDTO {
    private String id;

    @Schema(description = "模型名称")
    private String name;

    @Schema(description = "基础名称（deepseek-0.5)")
    private String baseName;

    @Schema(description = "模型key")
    private String apiKey;

    @Schema(description = "模型url")
    private String apiUrl;

    @Schema(description = "模型类型（大语言/视觉/音频）")
    private String type;

    @Schema(description = "模型供应商")
    private String provider;

    @Schema(description = "模型图片")
    private String avatar;

    @Schema(description = "模型类型（公有/私有）")
    private String permissionType;

    @Schema(description = "模型链接状态")
    private Boolean status;

    @Schema(description = "模型拥有者")
    private String owner;

    @Schema(description = "模型拥有者类型（个人/企业）")
    private String ownerType;

    private String orgId;


}
