package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class LoadTestReportResultRealtimeKey implements Serializable {
    private String reportId;

    private String reportKey;

    private Integer resourceIndex;

    private Integer sort;

    private static final long serialVersionUID = 1L;
}