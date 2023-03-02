package io.metersphere.api.dto.definition;

import lombok.Data;

@Data
public class FakeError {
    private String projectId;
    private boolean higherThanSuccess;
    private boolean higherThanError;
}

