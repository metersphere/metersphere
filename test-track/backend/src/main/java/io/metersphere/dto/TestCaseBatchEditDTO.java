package io.metersphere.dto;

import io.metersphere.base.domain.ProjectVersion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestCaseBatchEditDTO {
    private List<TestCaseDTO> testCaseDTOList;
    private ProjectVersion projectVersion;

    public boolean isNotEmpty() {
        return projectVersion != null && CollectionUtils.isNotEmpty(testCaseDTOList);
    }
}
