package io.metersphere.dto;

import io.metersphere.base.domain.TestCaseNode;
import lombok.Data;

@Data
public class NodeNumDTO extends TestCaseNode {
    private Integer caseNum;
}
