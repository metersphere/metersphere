package io.metersphere.sdk.dto.api.request.http.body;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-06  18:25
 */
@Data
@JsonTypeName("RAW")
public class RawBody extends Body {
    private String value;
}
