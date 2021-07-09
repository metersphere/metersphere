package io.metersphere.dto;

import io.metersphere.base.domain.CustomField;
import lombok.Data;

@Data
public class CustomFieldDao extends CustomField {
    private Boolean required;

    private Integer order;

    private String defaultValue;

    private String customData;

    private String key;
}
