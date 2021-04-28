package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TestCaseWithBLOBs extends TestCase implements Serializable {
    private String prerequisite;

    private String remark;

    private String steps;

    private String stepDescription;

    private String expectedResult;

    private String customFields;

    private static final long serialVersionUID = 1L;
}