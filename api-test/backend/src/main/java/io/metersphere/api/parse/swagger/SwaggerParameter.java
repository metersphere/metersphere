package io.metersphere.api.parse.swagger;

import lombok.Data;

@Data
public class SwaggerParameter {
    private String name;
    private String in;
    private String description;
    private Boolean required;
    private String type;
    private String format;
}
