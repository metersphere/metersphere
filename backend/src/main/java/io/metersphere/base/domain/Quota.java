package io.metersphere.base.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class Quota implements Serializable {
    private String id;

    private Integer api;

    private Integer performance;

    private Integer maxThreads;

    private Integer duration;

    private String resourcePool;

    private String workspaceId;

    private Boolean useDefault;

    private Long updateTime;

    private Integer member;

    private Integer project;

    private String projectId;

    private BigDecimal vumTotal;

    private BigDecimal vumUsed;

    private static final long serialVersionUID = 1L;
}