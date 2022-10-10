package io.metersphere.api.parse.api.swagger;

import lombok.Data;

@Data
public class SwaggerInfo {
    private String version;
    private String title;
    private String description;
    private String termsOfService;
}
