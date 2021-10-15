package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class TestCaseReview implements Serializable {
    private String id;

    private String name;

    private String creator;

    private String status;

    private Long createTime;

    private Long updateTime;

    private Long endTime;

    private String projectId;

    private String tags;

    private String createUser;

    private String followPeople;

    private String description;

    private static final long serialVersionUID = 1L;
}