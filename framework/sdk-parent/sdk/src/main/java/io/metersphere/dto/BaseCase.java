package io.metersphere.dto;

import lombok.Data;

@Data
public class BaseCase {
    private String id;
    private String name;
    private String projectId;
    private String versionName;
    private String type;
}
