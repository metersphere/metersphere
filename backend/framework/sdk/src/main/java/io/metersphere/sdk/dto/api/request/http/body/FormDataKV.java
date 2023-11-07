package io.metersphere.sdk.dto.api.request.http.body;

import io.metersphere.sdk.dto.api.request.http.KeyValueParam;
import lombok.Data;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-06  18:11
 */
@Data
public class FormDataKV extends KeyValueParam {
    private String paramType;
    private Boolean required = false;
    private Integer minLength;
    private Integer maxLength;
    private String contentType;
    private Boolean encode = false;
}
