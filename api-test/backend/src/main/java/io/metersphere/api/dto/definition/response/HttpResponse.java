package io.metersphere.api.dto.definition.response;

import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.commons.constants.RequestTypeConstants;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class HttpResponse extends Response {
    // type 必须放最前面，以便能够转换正确的类
    private String type = RequestTypeConstants.HTTP;

    private List<KeyValue> headers;

    private List<KeyValue> statusCode;

    private Body body;

}
