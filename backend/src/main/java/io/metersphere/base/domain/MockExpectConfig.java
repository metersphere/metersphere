package io.metersphere.base.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class MockExpectConfig implements Serializable {
    private String id;

    private String mockConfigId;

    private String name;

    private String tags;

    private String status;

    private Long createTime;

    private Long updateTime;

    private String createUserId;

    private static final long serialVersionUID = 1L;
}