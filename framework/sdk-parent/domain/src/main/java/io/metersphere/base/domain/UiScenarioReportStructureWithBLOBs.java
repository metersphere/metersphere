package io.metersphere.base.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UiScenarioReportStructureWithBLOBs extends UiScenarioReportStructure implements Serializable {
    private byte[] resourceTree;

    private String console;

    private static final long serialVersionUID = 1L;
}