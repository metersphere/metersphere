package io.metersphere.sdk.dto.api.request.http.auth;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

/**
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
