package io.metersphere.api.dto.definition;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class ListRequestDTO {
    private String name;
    private String path;
    private String method;
    private String protocol;

    @NotBlank
    private String projectId;

    private String OrderColumn;

    @Min(value = 1, message = "当前页码必须大于0")
    private int current;
    @Min(value = 5, message = "每页显示条数必须不小于5")
    private int pageSize;
}
