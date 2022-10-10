package io.metersphere.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IssuesDTO {
    private String id;
    private String title;
    private String status;
    private String description;
}
