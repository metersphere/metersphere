package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class ServiceIntegration implements Serializable {
    private String id;

    private String platform;

    private String workspaceId;

    private String configuration;

    private static final long serialVersionUID = 1L;
}