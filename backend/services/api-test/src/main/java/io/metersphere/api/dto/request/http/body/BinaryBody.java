package io.metersphere.api.dto.request.http.body;

import io.metersphere.api.dto.ApiFile;
import lombok.Data;

/**
 * binary 请求体
 * @Author: jianxing
 * @CreateTime: 2023-11-06  18:25
 */
@Data
public class BinaryBody extends ApiFile{
    /**
     *  描述
     */
    private String description;
}
