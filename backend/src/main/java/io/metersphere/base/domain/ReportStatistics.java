package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class ReportStatistics implements Serializable {
    private String id;

    private String name;

    private String projectId;

    private String createUser;

    private String updateUser;

    private String reportType;

    private Long createTime;

    private Long updateTime;

    private static final long serialVersionUID = 1L;
}