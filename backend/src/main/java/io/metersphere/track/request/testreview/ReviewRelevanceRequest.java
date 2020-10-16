package io.metersphere.track.request.testreview;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ReviewRelevanceRequest {
    private String reviewId;
    private String projectId;
    private List<String> testCaseIds = new ArrayList<>();
}
