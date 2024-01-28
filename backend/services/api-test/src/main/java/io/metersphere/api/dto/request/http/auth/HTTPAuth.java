package io.metersphere.api.dto.request.http.auth;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

/**
 * http 认证配置
 * <pre>
 * 该参数传参时，需要传入 authType 字段，用于区分是哪种认证方式
 * authType 取值为:
 *   BASIC ({@link BasicAuth})
 *   DIGEST ({@link DigestAuth})
 *   NONE ({@link NoAuth})
 * </pre>
 * @Author: jianxing
 * @CreateTime: 2023-11-07  11:00
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "authType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = NoAuth.class),
        @JsonSubTypes.Type(value = BasicAuth.class),
        @JsonSubTypes.Type(value = DigestAuth.class),
})
public abstract class HTTPAuth {
}
