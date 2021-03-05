package io.metersphere.performance.request;

import io.metersphere.base.domain.FileMetadata;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class SaveTestPlanRequest extends TestPlanRequest {
    private List<FileMetadata> updatedFileList;
}
