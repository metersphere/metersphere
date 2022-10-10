package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApiCaseExecutionInfo implements Serializable {
    private String id;

    private String sourceId;

    private String result;

    private String triggerMode;

    private Long createTime;

    private static final long serialVersionUID = 1L;
}