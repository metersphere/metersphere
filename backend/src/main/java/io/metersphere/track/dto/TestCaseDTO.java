package io.metersphere.track.dto;

import io.metersphere.base.domain.TestCaseWithBLOBs;
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

    private List<String> caseTags = new ArrayList<>();
}
