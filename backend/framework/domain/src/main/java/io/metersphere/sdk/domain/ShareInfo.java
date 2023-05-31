package io.metersphere.sdk.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class ShareInfo implements Serializable {
    @Schema(title = "分享ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{share_info.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{share_info.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "创建人")
    private String createUser;

    @Schema(title = "更新时间")
    private Long updateTime;

    @Schema(title = "分享类型single batch")
    private String shareType;

    @Schema(title = "语言")
    private String lang;

    @Schema(title = "分享扩展数据")
    private byte[] customData;

    private static final long serialVersionUID = 1L;
}