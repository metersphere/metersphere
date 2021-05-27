package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class LoadTestWithBLOBs extends LoadTest implements Serializable {
    private String loadConfiguration;

    private String advancedConfiguration;

    private static final long serialVersionUID = 1L;
}