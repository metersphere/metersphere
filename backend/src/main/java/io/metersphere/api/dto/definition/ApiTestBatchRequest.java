package io.metersphere.api.dto.definition;

import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import io.metersphere.controller.request.OrderRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ApiTestBatchRequest extends ApiTestCaseWithBLOBs {
    private List<String> ids;
    private List<OrderRequest> orders;
    private String projectId;

    /**
     * isSelectAllDate：选择的数据是否是全部数据（全部数据是不受分页影响的数据）
     * filters: 数据状态
     * name：如果是全部数据，那么表格如果历经查询，查询参数是什么
     * moduleIds： 哪些模块的数据
     * unSelectIds：是否在页面上有未勾选的数据，有的话他们的ID是哪些。
     * filters/name/moduleIds/unSeelctIds 只在isSelectAllDate为true时需要。为了让程序能明确批量的范围。
     */
    private boolean isSelectAll;

    private Map<String, List<String>> filters;

    private String name;

    private String method;

    private String path;

    private List<String> moduleIds;

    private List<String> unSelectIds;

    private String protocol;

    private String status;

    private String envId;

}
