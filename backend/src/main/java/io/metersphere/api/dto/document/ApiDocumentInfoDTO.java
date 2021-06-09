package io.metersphere.api.dto.document;

import lombok.Getter;
import lombok.Setter;

/**
 * @author song.tianyang
 * @Date 2021/2/8 4:37 下午
 * @Description
 */
@Getter
@Setter
public class ApiDocumentInfoDTO {
    private String id;
    private String method;
    private boolean selectedFlag;
    private String uri;
    private String name;
    private String status;

    private String requestHead;
    private String urlParams;
    private String requestBodyParamType;
    private String requestBodyFormData;
    private String requestBodyStrutureData;
    private Object requestPreviewData;
    private Object jsonSchemaBody;

    private String responseHead;
    private String responseBody;
    private Object jsonSchemaResponseBody;
    private String responseBodyParamType;
    private String responseBodyFormData;
    private String responseBodyStrutureData;

    private String responseCode;
    private boolean sharePopoverVisible = false;

}
