package io.metersphere.dto;

import io.metersphere.xpack.license.dto.LicenseInfoDTO;
import lombok.Data;

import java.io.Serializable;

@Data
public class LicenseDTO implements Serializable {

    private String status;

    private LicenseInfoDTO license;

}