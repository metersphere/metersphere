package io.metersphere.api.dto.scenario.timer;

import lombok.Data;

@Data
public class ConstantTimer {
    private String type;
    private String id;
    private boolean enable = true;
    private String delay;
}
