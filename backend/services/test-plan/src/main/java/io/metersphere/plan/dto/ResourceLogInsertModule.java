package io.metersphere.plan.dto;

import io.metersphere.system.dto.LogInsertModule;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResourceLogInsertModule extends LogInsertModule {
    @NotBlank
    private String resourceType;

    public ResourceLogInsertModule(String resourceType, LogInsertModule logInsertModule) {
        this.resourceType = resourceType;
        this.setOperator(logInsertModule.getOperator());
        this.setRequestUrl(logInsertModule.getRequestUrl());
        this.setRequestMethod(logInsertModule.getRequestMethod());
    }
}
