package io.metersphere.system.dto;

import io.metersphere.system.domain.CustomField;
import lombok.Data;

import java.io.Serializable;

@Data
public class CustomFieldDTO extends CustomField implements Serializable {

    private static final long serialVersionUID = 1L;

    private Boolean required;

    private String defaultValue;

    private Object value;
}
