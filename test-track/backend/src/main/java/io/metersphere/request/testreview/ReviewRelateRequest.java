package io.metersphere.request.testreview;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReviewRelateRequest {
    private String type;
    private String projectId;
    private String workspaceId;
    private List<String> reviewIds;
}
