package io.metersphere.sdk.dto.api.task;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class CollectionReportDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // 集合报告id
    private String reportId;

    // 集合报告名称
    private String reportName;
}
