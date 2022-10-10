package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class CustomFieldIssuesKey implements Serializable {
    private String resourceId;

    private String fieldId;

    private static final long serialVersionUID = 1L;
}