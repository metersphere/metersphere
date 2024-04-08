package io.metersphere.project.dto.environment.auth;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-07  11:00
 */
@Data
public class DigestAuth extends HTTPAuth {
    private String userName;
    private String password;

    public boolean isValid() {
        return StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(password);
    }
}
