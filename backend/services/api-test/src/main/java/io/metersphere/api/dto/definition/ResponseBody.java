package io.metersphere.api.dto.definition;

import io.metersphere.api.dto.request.http.body.JsonBody;
import io.metersphere.api.dto.request.http.body.RawBody;
import io.metersphere.api.dto.request.http.body.XmlBody;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 接口响应体、mock期望响应体中使用到的body
 */
@Data
public class ResponseBody implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank
    @Size(max = 20)
    private String bodyType;

    @Valid
    private JsonBody jsonBody;

    @Valid
    private XmlBody xmlBody;

    @Valid
    private RawBody rawBody;

    @Valid
    private ResponseBinaryBody binaryBody;

}
