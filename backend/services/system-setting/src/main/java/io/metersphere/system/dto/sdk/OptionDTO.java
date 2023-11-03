package io.metersphere.system.dto.sdk;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OptionDTO implements Serializable {
    @Schema(description =  "选项ID")
    private String id;
    @Schema(description =  "选项名称")
    private String name;
}
