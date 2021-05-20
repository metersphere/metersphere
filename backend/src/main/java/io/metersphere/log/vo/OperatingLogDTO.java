package io.metersphere.log.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class OperatingLogDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;

    private String projectName;

    private String operUser;

    private String userName;

    private String sourceId;

    private String operType;

    private String operModule;

    private String operTitle;

    private String createUser;

    private Long operTime;

    private String operContent;

    private OperatingLogDetails details;
}