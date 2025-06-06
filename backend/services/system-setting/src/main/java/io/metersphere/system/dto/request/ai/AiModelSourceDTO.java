package io.metersphere.system.dto.request.ai;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class AiModelSourceDTO {
    private String id;

    @Schema(description = "模型名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{model_source.name.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 255, message = "{model_source.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description = "模型类型（大语言/视觉/音频）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{model_source.type.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 255, message = "{model_source.type.length_range}", groups = {Created.class, Updated.class})
    private String type;

    @Schema(description = "模型供应商", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{model_source.provider.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 255, message = "{model_source.provider.length_range}", groups = {Created.class, Updated.class})
    private String providerName;

    @Schema(description = "模型类型（公有/私有）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{model_source.permission_type.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 255, message = "{model_source.permission_type.length_range}", groups = {Created.class, Updated.class})
    private String permissionType;

    @Schema(description = "模型链接状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{model_source.status.not_blank}", groups = {Created.class, Updated.class})
    private Boolean status;

    @Schema(description = "模型拥有者(system/用户id)")
    private String owner;

    @Schema(description = "模型拥有者类型（个人/企业）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{model_source.owner_type.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 255, message = "{model_source.owner_type.length_range}", groups = {Created.class, Updated.class})
    private String ownerType;

    @Schema(description = "基础名称（deepseek-0.5)")
    @NotBlank(message = "{model_source.base_name.not_blank}", groups = {Created.class, Updated.class})
    private String baseName;

    @Schema(description = "模型key")
    @NotBlank(message = "{model_source.app_key.not_blank}", groups = {Created.class, Updated.class})
    private String appKey;

    @Schema(description = "模型url")
    @NotBlank(message = "{model_source.api_url.not_blank}", groups = {Created.class, Updated.class})
    private String apiUrl;

    @Schema(description = "创建人名称")
    private String createUserName;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "创建人(操作人）")
    private String createUser;

    @Schema(description = "模型参数配置")
    private List<AdvSettingDTO> advSettingDTOList;



}
