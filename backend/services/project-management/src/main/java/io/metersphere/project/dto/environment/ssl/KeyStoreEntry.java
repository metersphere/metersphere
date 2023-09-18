package io.metersphere.project.dto.environment.ssl;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class KeyStoreEntry {
    @Schema(description = "证书条目id")
    private String id;
    @Schema(description = "原有别名")
    private String originalAsName;
    @Schema(description = "新别名")
    private String newAsName;
    @Schema(description = "证书类型")
    private String type;
    @Schema(description = "证书密码")
    private String password;
    @Schema(description = "证书来源")
    private String sourceName;
    @Schema(description = "证书来源id")
    private String sourceId;
    @Schema(description = "是否默认")
    private boolean isDefault;
}
