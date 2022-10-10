package io.metersphere.request.testreview;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestReviewRelevanceRequest {
    private String reviewId;
    private String name;
    private List<String> projectIds;
}
