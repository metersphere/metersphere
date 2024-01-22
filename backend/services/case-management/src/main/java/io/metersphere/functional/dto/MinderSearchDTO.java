package io.metersphere.functional.dto;

import io.metersphere.functional.domain.FunctionalCaseBlob;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class MinderSearchDTO {
    private List<BaseTreeNode> baseTreeNodes;
    private Map<String, List<ReviewFunctionalCaseDTO>> moduleCaseMap;
    private Map<String, FunctionalCaseBlob> blobMap;
    private Map<String, List<FunctionalCaseCustomFieldDTO>> customFieldMap;
    private String reviewPassRule;
    private boolean viewResult;
    private boolean viewFlag;
    private BaseTreeNode baseTreeNode;
}
