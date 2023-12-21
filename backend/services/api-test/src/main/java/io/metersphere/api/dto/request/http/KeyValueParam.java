package io.metersphere.api.dto.request.http;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-06  17:27
 */
@Data
public class KeyValueParam {
    /**
     * 参数ID
     */
    private String id;
    /**
     * 键
     */
    private String key;
    /**
     * 值
     */
    private String value;

    public boolean isValid() {
        return StringUtils.isNotBlank(key);
    }
}
