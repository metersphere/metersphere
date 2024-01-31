package io.metersphere.api.dto.share;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author: LAN
 * @date: 2023/12/27 14:43
 * @version: 1.0
 */
@Data
public class ShareInfoDTO {
    @Schema(description = "分享id")
    private String id;
    @Schema(description = "分享链接")
    private String shareUrl;
}
