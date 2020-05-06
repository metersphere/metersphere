package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoadTestReportLog implements Serializable {
    private Long id;

    private String reportId;

    private String resourceId;

    private String content;

    private static final long serialVersionUID = 1L;
}