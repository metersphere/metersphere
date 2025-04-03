package io.metersphere.system.dto.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AdvSettingDTO {

    @Schema(description = "参数类型")
    private String params;

    @Schema(description = "参数名称")
    private String name;

    private Object defaultValue;

    private Boolean enable;

    public AdvSettingDTO(String params, String name, Object defaultValue, Boolean enable) {
        this.params = params;
        this.name = name;
        this.defaultValue = defaultValue;
        this.enable = enable;
    }

    public AdvSettingDTO() {
    }
}
