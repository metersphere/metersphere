package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class CustomFieldTemplate implements Serializable {
    private String id;

    private String fieldId;

    private String templateId;

    private String scene;

    private Boolean required;

    private Integer order;

    private String defaultValue;

    private String customData;

    private String key;

    private static final long serialVersionUID = 1L;
}
