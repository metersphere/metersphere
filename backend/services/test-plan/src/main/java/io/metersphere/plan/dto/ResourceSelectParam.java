package io.metersphere.plan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceSelectParam {
    private String testPlanId;
    private List<String> selectIds;
    private List<String> moduleIds;
    private boolean repeatCase;
    private String orderString;
}
