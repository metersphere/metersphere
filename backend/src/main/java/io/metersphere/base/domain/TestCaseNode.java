package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class TestCaseNode implements Serializable {
    private Integer id;

    private String projectId;

    private String name;

    private Integer pId;

    private Integer level;

    private Long createTime;

    private Long updateTime;

    private static final long serialVersionUID = 1L;
}