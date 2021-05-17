package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OperatingLogWithBLOBs extends OperatingLog implements Serializable {
    private String operContent;

    private String operParams;

    private static final long serialVersionUID = 1L;
}