package io.metersphere.track.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class DemandDTO {
    protected String id;
    protected String name;
    protected String platform;
    protected List<? extends DemandDTO> children;
}
