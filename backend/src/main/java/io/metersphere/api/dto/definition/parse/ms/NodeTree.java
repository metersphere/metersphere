package io.metersphere.api.dto.definition.parse.ms;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NodeTree {
    private String id;

    private String newId;

    private String projectId;

    private String name;

    private String parentId;

    private Integer level;

    private Long createTime;

    private Long updateTime;

    private Double pos;

    private Integer caseNum;

    private List<NodeTree> children;

    private String path;
}
