package io.metersphere.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IssuesStatusCountDao {
    private int count;
    private String statusValue;
    private String platform;
}
