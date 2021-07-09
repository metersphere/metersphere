package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class TestCase implements Serializable {
    private String id;

    private String nodeId;

    private String nodePath;

    private String projectId;

    private String name;

    private String type;

    private String maintainer;

    private String priority;

    private String method;

    private Long createTime;

    private Long updateTime;

    private String testId;

    private Integer sort;

    private Integer num;

    private String otherTestName;

    private String reviewStatus;

    private String tags;

    private String demandId;

    private String demandName;

    private String followPeople;

    private String status;

    private String customNum;

    private String stepModel;

    private String createUser;

    private String originalStatus;

    private Long deleteTime;

    private String deleteUserId;

    private static final long serialVersionUID = 1L;
}