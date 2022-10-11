package io.metersphere.dto;

import io.metersphere.base.domain.TestCaseWithBLOBs;
import io.metersphere.xpack.track.dto.IssuesDao;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TestCaseDTO extends TestCaseWithBLOBs {

    private String maintainerName;
    private String apiName;
    private String lastResultId;
    private String projectName;
    private String createName;
    private String versionName;
    private List<CustomFieldDao> fields;
    private List<String> caseTags = new ArrayList<>();
    private List<IssuesDao> issueList = new ArrayList<>();
}
