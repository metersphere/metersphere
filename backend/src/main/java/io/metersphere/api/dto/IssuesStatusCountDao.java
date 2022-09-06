package io.metersphere.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IssuesStatusCountDao {
    private int statusCount;
    private String statusValue;
    private String platform;
}
