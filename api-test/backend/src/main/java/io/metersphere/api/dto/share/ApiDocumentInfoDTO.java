package io.metersphere.api.dto.share;

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

    private String tags;
    private String modules;
    private String createUser;
    private String responsibler;
    private String desc;

    private String requestHead;
    private String urlParams;
    private String restParams;
    private String requestBodyParamType;
    private String requestBodyFormData;
    private String requestBodyStructureData;
    private Object requestPreviewData;
    private JSONSchemaBodyDTO jsonSchemaBody;

    private String responseHead;
    private String responseBody;
    private Object jsonSchemaResponseBody;
    private String responseBodyParamType;
    private String responseBodyFormData;
    private String responseBodyStructureData;

    private String responseCode;
    private boolean sharePopoverVisible = false;

    private String refId;
    private String versionId;

    private String remark;
}
