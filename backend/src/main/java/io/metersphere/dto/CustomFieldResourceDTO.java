package io.metersphere.dto;

import io.metersphere.base.domain.ext.CustomFieldResource;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CustomFieldResourceDTO extends CustomFieldResource implements Serializable {
    private String name;
    private static final long serialVersionUID = 1L;
}
