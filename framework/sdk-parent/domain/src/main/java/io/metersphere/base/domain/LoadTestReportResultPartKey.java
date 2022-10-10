package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoadTestReportResultPartKey implements Serializable {
    private String reportId;

    private String reportKey;

    private Integer resourceIndex;

    private static final long serialVersionUID = 1L;
}