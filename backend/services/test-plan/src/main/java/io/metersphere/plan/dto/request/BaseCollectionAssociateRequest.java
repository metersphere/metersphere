package io.metersphere.plan.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class BaseCollectionAssociateRequest {

    @Schema(description = "测试集ID")
    private String collectionId;

    @Schema(description = "关联的用例ID集合")
    private List<String> ids;

}
