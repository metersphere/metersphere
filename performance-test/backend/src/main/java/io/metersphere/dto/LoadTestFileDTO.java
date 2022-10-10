package io.metersphere.dto;

import io.metersphere.base.domain.LoadTest;
import io.metersphere.base.domain.LoadTestFile;
import lombok.Data;

@Data
public class LoadTestFileDTO extends LoadTestFile {
    private LoadTest loadTest;
}