package io.metersphere.performance.dto;

import io.metersphere.base.domain.FileMetadata;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoadTestFileDTO extends FileMetadata {
    private String testId;
    private String testName;
}
