package io.metersphere.system.service;


import io.metersphere.system.dto.sdk.LicenseDTO;

public interface LicenseService {

    LicenseDTO refreshLicense();

    LicenseDTO validate();

    LicenseDTO addLicense(String licenseCode, String userId);

    String getCode(String encrypt);
}
