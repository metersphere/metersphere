package io.metersphere.sdk.dto.api.request.http.body;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-06  16:59
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "bodyType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = NoneBody.class),
        @JsonSubTypes.Type(value = FormDataBody.class),
        @JsonSubTypes.Type(value = WWWFormBody.class),
        @JsonSubTypes.Type(value = JsonBody.class),
        @JsonSubTypes.Type(value = XmlBody.class),
        @JsonSubTypes.Type(value = RawBody.class),
        @JsonSubTypes.Type(value = BinaryBody.class)
})
public abstract class Body {
}
