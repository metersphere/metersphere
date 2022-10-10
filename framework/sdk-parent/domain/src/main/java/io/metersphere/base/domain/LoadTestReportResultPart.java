package io.metersphere.base.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class LoadTestReportResultPart extends LoadTestReportResultPartKey implements Serializable {
    private String reportValue;

    private static final long serialVersionUID = 1L;
}