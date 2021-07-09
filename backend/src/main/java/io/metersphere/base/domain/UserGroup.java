package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class UserGroup implements Serializable {
    private String id;

    private String userId;

    private String groupId;

    private String sourceId;

    private Long createTime;

    private Long updateTime;

    private static final long serialVersionUID = 1L;
}