package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class ApiTestCaseFollow implements Serializable {
    private String caseId;

    private String followId;

    private static final long serialVersionUID = 1L;
}