package io.metersphere.api.dto;

import io.metersphere.base.domain.CustomFunctionWithBLOBs;
import io.metersphere.request.OrderRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class CustomFunctionRequest extends CustomFunctionWithBLOBs {
    private Map<String, List<String>> filters;
    private List<OrderRequest> orders;

}
