package io.metersphere.request.testreview;

import io.metersphere.request.BaseQueryRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QueryCaseReviewRequest extends BaseQueryRequest {
    private List<String> nodePaths;

    private List<String> reviewIds;

    private List<String> projectIds;

    private String node;

    private String nodeId;

    private String projectName;

    private String reviewerId;

    private String versionId;

    private String id;

    private String reviewId;

    private String caseId;

    private String result;

    private String reviewer;

    private Long createTime;

    private Long updateTime;

    private String createUser;

    private Long order;

    private String status;

    private Boolean isDel;

    /**
     * @since 2.10.10 添加模块树条件, 批量移动条件
     */
    private Boolean selectAll;
    private List<String> unSelectIds;
}
