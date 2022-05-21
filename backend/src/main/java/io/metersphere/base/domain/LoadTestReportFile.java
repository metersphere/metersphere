package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class LoadTestReportFile implements Serializable {
    private String reportId;

    private String fileId;

    private Integer sort;

    private static final long serialVersionUID = 1L;
}