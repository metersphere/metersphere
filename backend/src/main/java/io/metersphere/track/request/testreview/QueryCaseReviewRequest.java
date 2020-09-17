package io.metersphere.track.request.testreview;

import io.metersphere.base.domain.TestCaseReview;
import io.metersphere.controller.request.OrderRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QueryCaseReviewRequest extends TestCaseReview {
    private List<OrderRequest> orders;
}
