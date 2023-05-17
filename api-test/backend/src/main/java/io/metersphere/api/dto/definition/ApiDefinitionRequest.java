package io.metersphere.api.dto.definition;

import io.metersphere.request.ApiSyncCaseRequest;
import io.metersphere.request.BaseQueryRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ApiDefinitionRequest extends BaseQueryRequest {

    private String id;
    private String excludeId;
    private String moduleId;
    private String module;
    private String protocol;
    private String name;
    private String userId;
    private String planId;
    private boolean recent = false;
    private boolean isSelectThisWeedData = false;
    private long createTime = 0;
    private String status;
    private String apiCoverage;
    private String apiCaseCoverage;
    private String scenarioCoverage;
    private String reviewId;
    private String refId;
    private String versionId;
    private String path;
    private String method;

    //被场景覆盖的接口id集合
    private List<String> coverageIds;

    // 测试计划是否允许重复
    private boolean repeatCase;
    //是否进入待更新列表
    private Boolean toBeUpdated;

    //当前时间减去进入待更新的时间
    private Long toBeUpdateTime;

    //同步配置
    private ApiSyncCaseRequest syncConfig;

    //全选
    private boolean selectAll;

    private Long deleteTime;
    private String deleteUserId;

    /**
     * 是否根据自定义字段进行排序
     */
    private Boolean isCustomSorted = false;

}
