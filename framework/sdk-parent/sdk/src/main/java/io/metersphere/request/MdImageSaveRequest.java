package io.metersphere.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MdImageSaveRequest {
    private String projectId;
    private String resourceId;
    private List<String> fileNames;
}
