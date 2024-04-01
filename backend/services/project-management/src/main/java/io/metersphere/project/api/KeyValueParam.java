package io.metersphere.project.api;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * 键值对参数
 * @Author: jianxing
 * @CreateTime: 2023-11-06  17:27
 */
@Data
public class KeyValueParam {
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

    public boolean isNotBlankValue() {
        return StringUtils.isNotBlank(value);
    }
}
