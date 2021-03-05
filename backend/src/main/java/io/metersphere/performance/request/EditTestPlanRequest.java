package io.metersphere.performance.request;

import io.metersphere.base.domain.FileMetadata;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EditTestPlanRequest extends TestPlanRequest {
    private List<FileMetadata> updatedFileList;
}
