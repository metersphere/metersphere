package io.metersphere.dto;

import lombok.Data;

@Data
public class FakeErrorDTO {
    private String projectId;
    private boolean higherThanSuccess;
    private boolean higherThanError;
}

