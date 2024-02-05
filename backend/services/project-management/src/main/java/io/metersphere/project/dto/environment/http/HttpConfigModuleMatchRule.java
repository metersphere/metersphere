package io.metersphere.project.dto.environment.http;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2024-02-05  18:53
 */
@Data
public class HttpConfigModuleMatchRule implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "选中模块")
    @Valid
    private List<SelectModule> modules = new ArrayList<>(0);

}
