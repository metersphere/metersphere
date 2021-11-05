package io.metersphere.track.request.report;

import io.metersphere.controller.request.OrderRequest;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * @author song.tianyang
 * @Date 2021/1/8 4:36 下午
 * @Description
 */
@Getter
@Setter

public class QueryTestPlanReportRequest {
    private String name;
    private String testPlanName;
    private String creator;
    private String workspaceId;
    private String projectId;

    private Map<String, Object> combine;

    private List<OrderRequest> orders;
    private Map<String, List<String>> filters;

    /**
     * 批量操作的参数，用于判断是前台表格的当前页数据还是全库数据
     */
    private boolean isSelectAllDate;
    private List<String> unSelectIds;
    private List<String> dataIds;
}
