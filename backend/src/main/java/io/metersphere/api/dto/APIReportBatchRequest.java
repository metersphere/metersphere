package io.metersphere.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class APIReportBatchRequest extends QueryAPIReportRequest {

    private List<String> ids;

    /**
     * isSelectAllDate：选择的数据是否是全部数据（全部数据是不受分页影响的数据）
     * filters: 数据状态
     * name：如果是全部数据，那么表格如果历经查询，查询参数是什么
     * moduleIds： 哪些模块的数据
     * unSelectIds：是否在页面上有未勾选的数据，有的话他们的ID是哪些。
     * filters/name/moduleIds/unSeelctIds 只在isSelectAllDate为true时需要。为了让程序能明确批量的范围。
     */
    private boolean isSelectAllDate;

    private List<String> unSelectIds;
}
