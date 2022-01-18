package io.metersphere.dto;

import io.metersphere.controller.request.OrderRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class BaseConditionDTO {
    private Map<String, String> filters;
    private List<OrderRequest> orders;
    private String projectId;
    private boolean selectAll;
    private List<String> unSelectIds;
}
