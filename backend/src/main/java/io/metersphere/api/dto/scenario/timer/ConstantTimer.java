package io.metersphere.api.dto.scenario.timer;

import lombok.Data;

@Data
public class ConstantTimer {
    private String type;
    private String id;
    private Boolean enable;
    private String delay;
}
