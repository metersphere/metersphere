package io.metersphere.plugin.platform.dto;

import io.metersphere.base.domain.TestCaseWithBLOBs;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestCaseDemandDTO extends TestCaseWithBLOBs {

    /**
     * 修改前的需求ID
     * demandId 字段可获取修改后的ID
     */
    private String originDemandId;
}
