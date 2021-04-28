package io.metersphere.controller.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QueryCustomFieldRequest extends BaseQueryRequest {
    private String templateId;
    private String workspaceId;
    private String scene;
    private List<String> templateContainIds;
}
