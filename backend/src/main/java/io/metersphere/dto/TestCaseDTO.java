package io.metersphere.dto;

import io.metersphere.base.domain.TestCaseWithBLOBs;
import lombok.Data;

@Data
public class TestCaseDTO extends TestCaseWithBLOBs{

    private String maintainerName;

    public String getMaintainerName() {
        return maintainerName;
    }

    public void setMaintainerName(String maintainerName) {
        this.maintainerName = maintainerName;
    }
}
