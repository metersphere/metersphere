package io.metersphere.request.testreview;

import lombok.Data;

import java.util.List;

@Data
public class ReviewBatchMoveRequest {

    private List<String> ids;
    private QueryCaseReviewRequest condition;
    private String projectId;
    private String nodeId;
    private String nodePath;
}
