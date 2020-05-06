package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoadTestReportDetail implements Serializable {
    private String reportId;

    private String content;

    private static final long serialVersionUID = 1L;
}