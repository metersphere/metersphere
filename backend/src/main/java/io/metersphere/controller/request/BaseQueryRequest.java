package io.metersphere.controller.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class BaseQueryRequest {

    private String projectId;

    private String name;

    private String workspaceId;

    private List<String> ids;

    private List<String> moduleIds;

    private List<String> nodeIds;

    /**
     * 是否选中所有数据
     */
    private boolean selectAll;

    /**
     * 全选之后取消选中的id
     */
    private List<String> unSelectIds;

    /**
     * 排序条件
     */
    private List<OrderRequest> orders;

    /**
     * 过滤条件
     */
    private Map<String, List<String>> filters;

    /**
     * 高级搜索

     */
    private Map<String, Object> combine;

    /**
     * 要查询的字段
     */
    private List<String> selectFields;
}
