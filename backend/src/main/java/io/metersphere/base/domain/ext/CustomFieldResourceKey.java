package io.metersphere.base.domain.ext;

import io.metersphere.base.domain.CustomFieldTestCaseKey;
import lombok.Data;

import java.io.Serializable;

@Data
public class CustomFieldResourceKey extends CustomFieldTestCaseKey implements Serializable {
    private static final long serialVersionUID = 1L;
}
