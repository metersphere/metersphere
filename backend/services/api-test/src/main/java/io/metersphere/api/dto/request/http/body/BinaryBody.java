package io.metersphere.api.dto.request.http.body;

import lombok.Data;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-06  18:25
 */
@Data
public class BinaryBody {
    private BodyFile bodyFile;
    private String description;
}
