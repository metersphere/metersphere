package io.metersphere.track.request.testreview;

import io.metersphere.track.request.testcase.QueryTestCaseRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ReviewRelevanceRequest {
    /**
     * 评审ID
     */
    private String reviewId;
    
    /**
     * 当选择关联全部用例时把加载条件送到后台，从后台查询
     */

    private QueryTestCaseRequest request;
    /**
     * 具体选择要关联的用例
     */
    private List<String> testCaseIds = new ArrayList<>();

    private  Boolean checked;
}
