package io.metersphere.sdk.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class FileModuleRepositoryDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String fileModuleId;

    private String platform;

    private String url;

    private String token;

    private String userName;
}