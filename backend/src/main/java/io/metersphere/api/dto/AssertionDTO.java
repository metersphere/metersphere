package io.metersphere.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssertionDTO {
    private String content;
    private String message;
    private String name;
    private boolean pass;
}
