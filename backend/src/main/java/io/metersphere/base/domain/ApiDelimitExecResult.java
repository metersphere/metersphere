package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApiDelimitExecResult implements Serializable {
    private String id;
    private String resourceId;
    private String name;
    private String content;
    private String status;
    private String userId;
    private String startTime;
    private String endTime;

}
