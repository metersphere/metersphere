package io.metersphere.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CascadeDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description =  "值/id")
    private String value;
    @Schema(description =  "名称/标签")
    private String label;

}
