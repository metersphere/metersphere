package io.metersphere.functional.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class FunctionalCaseCollectionMindRequest extends FunctionalCasePlanMindRequest {

    @Schema(description = "测试集ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String collectionId;



}
