package io.metersphere.request;

import io.metersphere.commons.utils.JSON;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * selectAll：选择的数据是否是全部数据（全部数据是不受分页影响的数据）
 * filters: 数据状态
 * name：如果是全部数据，那么表格如果历经查询，查询参数是什么
 * moduleIds： 哪些模块的数据
 * unSelectIds：是否在页面上有未勾选的数据，有的话他们的ID是哪些。
 * filters/name/moduleIds/unSeelctIds 只在isSelectAllDate为true时需要。为了让程序能明确批量的范围。
 */
@Getter
@Setter
public class BaseQueryRequest {

    private String projectId;

    private String name;

    /**
     * 状态不等于 notEqStatus
     */
    private String notEqStatus;

    private String workspaceId;

    private List<String> ids;

    private List<String> moduleIds;

    private List<String> nodeIds;

    /**
     * 排除哪些id
     */
    private List<String> notInIds;

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
     * 版本 ID
     */
    private String versionId;

    /**
     * 版本来源字段
     */
    private String refId;

    /**
     * 测试计划关联场景过滤掉步骤为0的场景
     */
    private boolean hasStep;

    private String scenarioType;

    public Map<String, List<String>> getFilters() {
        if (MapUtils.isEmpty(filters) || filters.containsKey("isHandleCustomMultiple")) {
            return filters;
        }
        // 处理过滤器中的自定义字段多选值
        filters.forEach((k,v) -> {
            if (k.contains("custom_multiple") && CollectionUtils.isNotEmpty(v)) {
                filters.put(k, Collections.singletonList(JSON.toJSONString(v)));
            }
        });
        filters.put("isHandleCustomMultiple", null);
        return filters;
    }
}
