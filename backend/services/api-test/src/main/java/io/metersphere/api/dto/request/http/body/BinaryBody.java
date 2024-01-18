package io.metersphere.api.dto.request.http.body;

import io.metersphere.api.dto.ApiFile;
import lombok.Data;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-06  18:25
 */
@Data
public class BinaryBody {
    private ApiFile bodyFile;
    private String description;
}
