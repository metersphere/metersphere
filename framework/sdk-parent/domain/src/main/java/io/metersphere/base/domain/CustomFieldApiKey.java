package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class CustomFieldApiKey implements Serializable {
    private static final long serialVersionUID = 1L;
    private String resourceId;
    private String fieldId;
}