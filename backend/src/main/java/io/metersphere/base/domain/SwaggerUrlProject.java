package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class SwaggerUrlProject implements Serializable {
    private String id;

    private String projectId;

    private String swaggerUrl;

    private String moduleId;

    private String modulePath;

    private String modeId;

    private String config;

    private static final long serialVersionUID = 1L;
}