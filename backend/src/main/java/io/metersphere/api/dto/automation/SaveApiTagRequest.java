package io.metersphere.api.dto.automation;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SaveApiTagRequest {
    private String id;

    private String projectId;

    private String userId;

    private String name;
}
