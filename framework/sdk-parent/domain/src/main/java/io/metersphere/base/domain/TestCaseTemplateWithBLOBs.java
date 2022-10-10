package io.metersphere.base.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TestCaseTemplateWithBLOBs extends TestCaseTemplate implements Serializable {
    private String prerequisite;

    private String stepDescription;

    private String expectedResult;

    private String actualResult;

    private String steps;

    private static final long serialVersionUID = 1L;
}