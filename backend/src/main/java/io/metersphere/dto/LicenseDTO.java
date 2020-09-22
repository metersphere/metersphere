package io.metersphere.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class LicenseDTO implements Serializable {

    private String status;

    private LicenseInfoDTO license;

}