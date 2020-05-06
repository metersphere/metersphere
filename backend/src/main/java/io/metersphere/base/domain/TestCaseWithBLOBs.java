package io.metersphere.base.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TestCaseWithBLOBs extends TestCase implements Serializable {
    private String remark;

    private String steps;

    private static final long serialVersionUID = 1L;
}