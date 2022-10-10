package io.metersphere.reportstatistics.dto.request;

import io.metersphere.base.domain.EnterpriseTestReportWithBLOBs;
import io.metersphere.request.OrderRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class EnterpriseTestReportRequest extends EnterpriseTestReportWithBLOBs {
    private List<OrderRequest> orders;
    private List<String> ids;
    private Map<String, List<String>> filters;
    private boolean selectAll;
    private List<String> unSelectIds;
}
