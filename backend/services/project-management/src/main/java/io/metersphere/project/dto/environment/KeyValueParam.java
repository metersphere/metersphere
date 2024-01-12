package io.metersphere.project.dto.environment;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

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
}
