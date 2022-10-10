package io.metersphere.api.dto.swaggerurl;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SwaggerUrlRequest {
    private String projectId;
    private String swaggerUrl;
    private String moduleId;
}
