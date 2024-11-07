package io.metersphere.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NameArrayDTO {

    @Schema(description =  "id")
    private String id;

    @Schema(description =  "名称")
    private String name;

    @Schema(description =  "数量集合")
    private List<Integer> count;

}
