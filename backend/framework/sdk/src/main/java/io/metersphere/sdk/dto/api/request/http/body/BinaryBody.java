package io.metersphere.sdk.dto.api.request.http.body;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-06  18:25
 */
@Data
@JsonTypeName("BINARY")
public class BinaryBody extends Body {
    // todo 如果fileName能直接定义到文件，就不需要filePath
    private String filePath;
    private String fileName;
    private String description;
}
