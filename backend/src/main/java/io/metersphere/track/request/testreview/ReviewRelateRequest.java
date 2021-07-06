package io.metersphere.track.request.testreview;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewRelateRequest {
    private String type;
    private String projectId;
    private String workspaceId;
}
