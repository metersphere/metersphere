package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoadTestReportFile implements Serializable {
    private String reportId;

    private String fileId;

    private Integer sort;

    private static final long serialVersionUID = 1L;
}