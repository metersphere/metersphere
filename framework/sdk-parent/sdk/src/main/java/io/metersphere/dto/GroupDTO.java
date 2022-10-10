package io.metersphere.dto;

import io.metersphere.base.domain.Group;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupDTO extends Group {
    private String scopeName;
    private Long memberSize;
}
