package io.metersphere.track.dto;

import io.metersphere.base.domain.IssuesDao;
import io.metersphere.base.domain.TestCaseWithBLOBs;
import io.metersphere.dto.CustomFieldDao;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TestCaseDTO extends TestCaseWithBLOBs {

    private String maintainerName;
    private String apiName;
    private String performName;
    private String lastResultId;
    private String projectName;
    private String createName;
    private String lastExecuteResult;
    private String versionName;
    private List<CustomFieldDao> fields;
    private List<String> caseTags = new ArrayList<>();
    private List<IssuesDao> issueList = new ArrayList<>();
}
