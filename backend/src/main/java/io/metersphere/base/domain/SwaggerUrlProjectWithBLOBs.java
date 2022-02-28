package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SwaggerUrlProjectWithBLOBs extends SwaggerUrlProject implements Serializable {
    private String config;

    private String customFields;

    private static final long serialVersionUID = 1L;
}