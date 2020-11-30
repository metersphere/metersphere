package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApiDefinitionHistory implements Serializable {
    private String id;

    private String apiDefinitionId;

    private String content;

    private String userId;

    private Long createTime;

    private Long updateTime;

    private static final long serialVersionUID = 1L;
}