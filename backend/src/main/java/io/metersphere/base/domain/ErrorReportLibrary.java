package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class ErrorReportLibrary implements Serializable {
    private String id;

    private String projectId;

    private Long createTime;

    private Long updateTime;

    private String createUser;

    private String updateUser;

    private String errorCode;

    private String matchType;

    private Boolean status;

    private static final long serialVersionUID = 1L;
}