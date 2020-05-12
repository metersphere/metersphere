package io.metersphere.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogDetailDTO {
    private String id;
    private String resourceName;
    private String content;
}
