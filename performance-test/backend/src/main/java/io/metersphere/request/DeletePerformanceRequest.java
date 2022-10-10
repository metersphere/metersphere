package io.metersphere.request;

import lombok.Data;

import java.util.List;

@Data
public class DeletePerformanceRequest extends BaseQueryRequest {
    private List<String> ids;
    private String projectId;
}
