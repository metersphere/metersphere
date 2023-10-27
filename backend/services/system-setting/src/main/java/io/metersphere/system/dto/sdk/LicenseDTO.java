package io.metersphere.system.dto.sdk;

import lombok.Data;

import java.io.Serializable;

@Data
public class LicenseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String status;

    private LicenseInfoDTO license;

}