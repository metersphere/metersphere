package io.metersphere.plan.dto.request;

import io.metersphere.plan.dto.TestPlanCollectionAssociateDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class BaseCollectionAssociateRequest {

    @Schema(description = "测试集ID")
    private String collectionId;

    @Schema(description = "模块下的id集合属性")
    private TestPlanCollectionAssociateDTO modules;

}
