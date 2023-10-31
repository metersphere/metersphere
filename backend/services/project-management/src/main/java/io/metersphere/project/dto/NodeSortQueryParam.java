package io.metersphere.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodeSortQueryParam {
    private String parentId;
    private String operator;
    private long pos;
}

