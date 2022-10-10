package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class EnterpriseTestReport implements Serializable {
    private String id;

    private String projectId;

    private Long createTime;

    private Long updateTime;

    private String createUser;

    private String updateUser;

    private String name;

    private String status;

    private String sendFreq;

    private String sendCron;

    private Long lastSendTime;

    private static final long serialVersionUID = 1L;
}