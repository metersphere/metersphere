package io.metersphere.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//用于记录模块的DTO
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogInsertModule {
    @NotBlank
    private String operator;
    @NotBlank
    private String requestUrl;
    @NotBlank
    private String requestMethod;
}
