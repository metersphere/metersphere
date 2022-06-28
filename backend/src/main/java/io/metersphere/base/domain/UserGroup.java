package io.metersphere.base.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserGroup implements Serializable {
    private String id;

    private String userId;

    private String groupId;

    private String sourceId;

    private Long createTime;

    private Long updateTime;

    private static final long serialVersionUID = 1L;
}