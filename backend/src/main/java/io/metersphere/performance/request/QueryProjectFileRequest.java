package io.metersphere.performance.request;

import io.metersphere.controller.request.BaseQueryRequest;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class QueryProjectFileRequest extends BaseQueryRequest {
    private String name;
    private String moduleId;
    private Map<String, String> loadCaseFileIdMap;
    private List<String> types;
}
