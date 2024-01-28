package io.metersphere.api.dto.request.http.body;

import lombok.Data;

/**
 * xml 请求体
 * @Author: jianxing
 * @CreateTime: 2023-11-06  18:25
 */
@Data
public class XmlBody {
    /**
     *  请求体值
     */
    private String value;
}
