package io.metersphere.api.dto.export;

import lombok.Data;

/**
 * @author wx
 */
@Data
public class SwaggerInfo {
    private String version;
    private String title;
    private String description;
    private String termsOfService;
}
