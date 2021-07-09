package io.metersphere.base.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class TestResourcePool implements Serializable {
    private String id;

    private String name;

    private String type;

    private String description;

    private String status;

    private Long createTime;

    private Long updateTime;

    private String image;

    private String heap;

    private String gcAlgo;

    private String createUser;

    private Boolean api;

    private Boolean performance;

    private static final long serialVersionUID = 1L;
}