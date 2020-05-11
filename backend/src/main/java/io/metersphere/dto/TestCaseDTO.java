package io.metersphere.dto;

import io.metersphere.base.domain.TestCaseWithBLOBs;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestCaseDTO extends TestCaseWithBLOBs{

    private String maintainerName;

}
