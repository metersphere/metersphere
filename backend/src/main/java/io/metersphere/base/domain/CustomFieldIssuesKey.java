package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class CustomFieldIssuesKey implements Serializable {
    private String resourceId;

    private String fieldId;

    private static final long serialVersionUID = 1L;
}