package io.metersphere.project.dto.environment.auth;

import io.metersphere.sdk.valid.EnumValue;
import lombok.Data;

import java.util.HashMap;

/**
 * http 认证配置
 *
 * @Author: jianxing
 * @CreateTime: 2023-11-07  11:00
 */
@Data
public class HTTPAuthConfig {
    /**
     * 认证方式
     * {@link HTTPAuthType}
     */
    @EnumValue(enumClass = HTTPAuthType.class)
    private String authType = HTTPAuthType.NONE.name();
    private BasicAuth basicAuth = new BasicAuth();
    private DigestAuth digestAuth = new DigestAuth();

    public boolean isHTTPAuthValid() {
        HashMap<String, HTTPAuth> httpAuthHashMap = HashMap.newHashMap(2);
        httpAuthHashMap.put(HTTPAuthType.BASIC.name(), basicAuth);
        httpAuthHashMap.put(HTTPAuthType.DIGEST.name(), digestAuth);
        HTTPAuth httpAuth = httpAuthHashMap.get(authType);
        return httpAuth != null && httpAuth.isValid();
    }

    /**
     * http 认证方式
     */
    public enum HTTPAuthType {
        NONE,
        BASIC,
        DIGEST
    }
}
