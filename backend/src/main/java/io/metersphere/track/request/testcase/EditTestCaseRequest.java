package io.metersphere.track.request.testcase;

import io.metersphere.base.domain.FileMetadata;
import io.metersphere.base.domain.TestCaseWithBLOBs;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EditTestCaseRequest extends TestCaseWithBLOBs {
    private List<FileMetadata> updatedFileList;
}
