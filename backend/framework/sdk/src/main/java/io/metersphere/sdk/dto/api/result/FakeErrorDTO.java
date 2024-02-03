package io.metersphere.sdk.dto.api.result;

import lombok.Data;

@Data
public class FakeErrorDTO {
    private String projectId;
    private boolean higherThanSuccess;
    private boolean higherThanError;
}

