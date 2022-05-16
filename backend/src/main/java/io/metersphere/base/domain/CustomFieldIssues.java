package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CustomFieldIssues extends CustomFieldIssuesKey implements Serializable {
    private String value;

    private String textValue;

    private static final long serialVersionUID = 1L;
}