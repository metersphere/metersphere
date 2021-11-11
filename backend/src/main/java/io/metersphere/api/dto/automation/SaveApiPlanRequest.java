package io.metersphere.api.dto.automation;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class SaveApiPlanRequest {
    private List<String> planIds;
    private List<String> apiIds;
    private List<String> scenarioIds;


    /**
     * isSelectAllDate：选择的数据是否是全部数据（全部数据是不受分页影响的数据）
     * filters: 数据状态
     * name：如果是全部数据，那么表格如果历经查询，查询参数是什么
     * moduleIds： 哪些模块的数据
     * unSelectIds：是否在页面上有未勾选的数据，有的话他们的ID是哪些。
     * filters/name/moduleIds/unSeelctIds 只在isSelectAllDate为true时需要。为了让程序能明确批量的范围。
     */
    private boolean isSelectAllDate;

    private Map<String, List<String>> filters;

    private String name;

    private List<String> moduleIds;

    private List<String> unSelectIds;

    private String projectId;

    /**
     * 项目环境对应关系
     */
    private Map<String, String> envMap;

    /**
     * 用例的环境的对应关系
     */
    private Map<String, List<String>> mapping;
    private String environmentType;
    private String envGroupId;

}
