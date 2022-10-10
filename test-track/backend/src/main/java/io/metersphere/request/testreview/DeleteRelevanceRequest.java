package io.metersphere.request.testreview;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteRelevanceRequest {
    private String id;
    private String reviewId;
}
