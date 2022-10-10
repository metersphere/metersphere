package io.metersphere.base.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ReportStatisticsWithBLOBs extends ReportStatistics implements Serializable {
    private String selectOption;

    private String dataOption;

    private static final long serialVersionUID = 1L;
}