package io.metersphere.request.issues;

import io.metersphere.request.OrderRequest;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * @author songcc
 * 缺陷导出参数
 */
@Data
public class IssueExportRequest {
    private String projectId;
    private String workspaceId;
    private String userId;
    private Boolean isSelectAll;
    private List<String> exportIds;
    private List<OrderRequest> orders;
    private Map<String, List<IssueExportHeader>> exportFields;

    @Getter
    @Setter
    public static class IssueExportHeader {
        private String id;
        private String name;
    }
}
