package io.metersphere.system.dto.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AdvSettingDTO {

    @Schema(description = "参数类型")
    private String name;

    @Schema(description = "参数名称")
    private String label;

    @Schema(description = "参数值")
    private Object value;

    @Schema(description = "参数最大值")
    private Object maxValue;

    @Schema(description = "参数最小值")
    private Object minValue;

    @Schema(description = "是否启用")
    private Boolean enable;

    public AdvSettingDTO(String name, String label, Object value, Boolean enable) {
        this.name = name;
        this.label = label;
        this.value = value;
        this.enable = enable;
    }

    public AdvSettingDTO() {
    }
}
