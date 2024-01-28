package io.metersphere.api.dto.request.http.body;

import lombok.Data;

/**
 * raw 请求体
 * @Author: jianxing
 * @CreateTime: 2023-11-06  18:25
 */
@Data
public class RawBody {
    /**
     *  请求体值
     */
    private String value;
}
