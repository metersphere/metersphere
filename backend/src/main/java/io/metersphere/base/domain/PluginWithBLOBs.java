package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PluginWithBLOBs extends Plugin implements Serializable {
    private String formOption;

    private String formScript;

    private static final long serialVersionUID = 1L;
}