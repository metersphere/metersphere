package io.metersphere.base.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CustomFieldApi extends CustomFieldApiKey implements Serializable {
    private static final long serialVersionUID = 1L;
    private String value;
    private String textValue;
}