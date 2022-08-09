package io.metersphere.performance.request;

import io.metersphere.controller.request.BaseQueryRequest;
import lombok.Data;

import java.util.List;

@Data
public class QueryProjectFileRequest extends BaseQueryRequest {
    private String name;
    private String moduleId;
    private List<String> types;
}
