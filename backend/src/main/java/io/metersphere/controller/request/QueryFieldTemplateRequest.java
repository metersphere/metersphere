package io.metersphere.controller.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QueryFieldTemplateRequest extends BaseQueryRequest {
    private String templateId;
    private String scene;
}
