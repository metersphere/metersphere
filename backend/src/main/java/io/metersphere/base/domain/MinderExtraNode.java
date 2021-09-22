package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class MinderExtraNode implements Serializable {
    private String id;

    private String parentId;

    private String groupId;

    private String type;

    private String nodeData;

    private static final long serialVersionUID = 1L;
}